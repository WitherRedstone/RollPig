package com.chinaex123.roll_pig.event;

import com.chinaex123.roll_pig.RollPig;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@EventBusSubscriber(modid = RollPig.MODID)
public final class RPChatQueue {

    private RPChatQueue() {
    }

    public static final int DELAY_TICKS = 40;

    private static final List<Pending> QUEUE = new ArrayList<>();
    private static MinecraftServer owner;

    private record Pending(long dueTime, Component message) {
    }

    public static void broadcastLater(MinecraftServer server, Component message, int delayTicks) {
        long due = server.overworld().getGameTime() + Math.max(0, delayTicks);
        QUEUE.add(new Pending(due, message));
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        if (QUEUE.isEmpty()) {
            owner = server;
            return;
        }
        if (server != owner) {
            owner = server;
            QUEUE.clear();
            return;
        }
        long now = server.overworld().getGameTime();
        Iterator<Pending> iterator = QUEUE.iterator();
        while (iterator.hasNext()) {
            Pending pending = iterator.next();
            if (pending.dueTime() > now) {
                continue;
            }
            iterator.remove();
            server.getPlayerList().broadcastSystemMessage(pending.message(), false);
        }
    }
}
