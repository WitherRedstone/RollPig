package com.chinaex123.roll_pig.event.logic;

import com.chinaex123.roll_pig.api.RollPigEvent;
import com.chinaex123.roll_pig.api.RollResult;
import com.chinaex123.roll_pig.data.state.Pig;
import com.chinaex123.roll_pig.data.state.PigRegistry;
import com.chinaex123.roll_pig.data.state.RollPigState;
import com.chinaex123.roll_pig.init.RPAttachments;
import com.chinaex123.roll_pig.network.PigPenPayload;
import com.chinaex123.roll_pig.network.RPNetwork;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;

import java.util.List;

public final class RollPigService {

    private RollPigService() { }

    public static RollResult attempt(ServerPlayer player) {
        long cycle = RollPigState.cycleOf(player.level().getDayTime());
        RollPigState state = RPAttachments.get(player);

        if (!state.canRoll(cycle)) {
            return RollResult.failure(RollResult.Reason.ALREADY_ROLLED);
        }

        List<Pig> pigs = PigRegistry.all();
        if (pigs.isEmpty()) {
            return RollResult.failure(RollResult.Reason.NO_PIGS);
        }

        // 给别的模组一个拦截的机会
        RollPigEvent.Pre event = new RollPigEvent.Pre(player);
        NeoForge.EVENT_BUS.post(event);
        if (event.isCanceled()) {
            return RollResult.failure(RollResult.Reason.CANCELLED);
        }

        // 每只猪概率相同，可以重复抽到
        Pig pig = pigs.get(player.getRandom().nextInt(pigs.size()));
        RollPigState next = state.withRoll(pig.id(), cycle);
        RPAttachments.set(player, next);

        NeoForge.EVENT_BUS.post(new RollPigEvent.Post(player, pig, next));
        return RollResult.success(pig);
    }

    public static RollResult attemptAndNotify(ServerPlayer player, int mode) {
        RollResult result = attempt(player);
        if (!result.success()) {
            player.displayClientMessage(result.message(), false);
            return result;
        }
        long cycle = RollPigState.cycleOf(player.level().getDayTime());
        RollPigState next = RPAttachments.get(player);
        RPNetwork.sendTo(player, new PigPenPayload(mode, next.canRoll(cycle), result.pig().id(), next.counts()));
        return result;
    }

    public static boolean tryRoll(ServerPlayer player, int mode) {
        return attemptAndNotify(player, mode).success();
    }

    public static void handleRequest(ServerPlayer player) {
        if (player.containerMenu != player.inventoryMenu) {
            player.displayClientMessage(
                    Component.translatable("message.roll_pig.close_container")
                            .withStyle(net.minecraft.ChatFormatting.RED), false);
            return;
        }
        attemptAndNotify(player, PigPenPayload.MODE_UPDATE);
    }
}
