package com.chinaex123.roll_pig.event;

import com.chinaex123.roll_pig.RollPig;
import com.chinaex123.roll_pig.config.RPConfig;
import com.chinaex123.roll_pig.data.state.RollPigState;
import com.chinaex123.roll_pig.init.RPAttachments;
import com.chinaex123.roll_pig.network.PigPenPayload;
import com.chinaex123.roll_pig.network.RPNetwork;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = RollPig.MODID)
public final class RPDayWatcher {

    private static MinecraftServer watchedServer;
    private static long lastCycle = Long.MIN_VALUE;

    private RPDayWatcher() {
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        ServerLevel overworld = server.overworld();

        long cycle = RollPigState.cycleOf(overworld.getDayTime());
        if (server != watchedServer) {
            watchedServer = server;
            lastCycle = cycle;
            return;
        }
        if (cycle == lastCycle) {
            return;
        }

        lastCycle = cycle;
        boolean announce = RPConfig.ANNOUNCE_NEW_DAY.get();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            RollPigState state = RPAttachments.get(player);
            RPNetwork.sendTo(player, new PigPenPayload(PigPenPayload.MODE_SYNC,
                    state.canRoll(cycle), "", state.counts()));
            if (announce) {
                player.displayClientMessage(
                        Component.translatable("message.roll_pig.new_day").withStyle(ChatFormatting.GOLD), false);
            }
        }
    }
}
