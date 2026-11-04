package com.chinaex123.roll_pig.event;

import com.chinaex123.roll_pig.RollPig;
import com.chinaex123.roll_pig.data.state.RollPigState;
import com.chinaex123.roll_pig.init.RPAttachments;
import com.chinaex123.roll_pig.network.PigPenPayload;
import com.chinaex123.roll_pig.network.RPNetwork;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = RollPig.MODID)
public final class RPPlayerSync {

    private RPPlayerSync() {
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        Level level = player.serverLevel();
        long cycle = RollPigState.cycleOf(level.getDayTime());
        RollPigState state = RPAttachments.get(player);
        RPNetwork.sendTo(player, new PigPenPayload(PigPenPayload.MODE_SYNC, state.canRoll(cycle), "", state.counts()));
    }
}
