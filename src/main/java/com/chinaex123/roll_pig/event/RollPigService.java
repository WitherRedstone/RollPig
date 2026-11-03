package com.chinaex123.roll_pig.event;

import com.chinaex123.roll_pig.api.RollPigEvent;
import com.chinaex123.roll_pig.api.RollResult;
import com.chinaex123.roll_pig.config.RPConfig;
import com.chinaex123.roll_pig.util.Pig;
import com.chinaex123.roll_pig.data.state.PigRegistry;
import com.chinaex123.roll_pig.data.state.RollPigState;
import com.chinaex123.roll_pig.init.RPAttachments;
import com.chinaex123.roll_pig.network.PigPenPayload;
import com.chinaex123.roll_pig.network.RPNetwork;
import com.chinaex123.roll_pig.util.RPChatCard;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;

import java.util.List;

public final class RollPigService {

    public static RollResult attempt(ServerPlayer player) {
        return attempt(player, false);
    }

    public static RollResult attempt(ServerPlayer player, boolean force) {
        long cycle = RollPigState.cycleOf(player.level().getDayTime());
        RollPigState state = RPAttachments.get(player);

        if (!force && !state.canRoll(cycle)) {
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
        RollPigState next = force
                ? state.withCount(pig.id(), state.countOf(pig.id()) + 1)
                : state.withRoll(pig.id(), cycle);
        RPAttachments.set(player, next);

        NeoForge.EVENT_BUS.post(new RollPigEvent.Post(player, pig, next));
        announce(player, pig, next);
        return RollResult.success(pig);
    }

    private static void announce(ServerPlayer player, Pig pig, RollPigState state) {
        if (!RPConfig.BROADCAST_ROLL.get()) {
            return;
        }
        MinecraftServer server = player.getServer();
        if (server == null) {
            return;
        }
        RPChatQueue.broadcastLater(server, RPChatCard.build(player, pig, state), RPChatQueue.DELAY_TICKS);
    }

    public static RollResult attemptAndNotify(ServerPlayer player, int mode) {
        return attemptAndNotify(player, mode, false);
    }

    public static RollResult attemptAndNotify(ServerPlayer player, int mode, boolean force) {
        RollResult result = attempt(player, force);
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

    public static RollResult forceRoll(ServerPlayer player, int mode) {
        return attemptAndNotify(player, mode, true);
    }

    public static void handleRequest(ServerPlayer player) {
        if (player.containerMenu != player.inventoryMenu) {
            player.displayClientMessage(
                    Component.translatable("message.roll_pig.close_container")
                            .withStyle(ChatFormatting.RED), false);
            return;
        }
        attemptAndNotify(player, PigPenPayload.MODE_UPDATE);
    }
}
