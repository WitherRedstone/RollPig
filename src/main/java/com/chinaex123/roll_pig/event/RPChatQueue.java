package com.chinaex123.roll_pig.event;

import com.chinaex123.roll_pig.RollPig;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

@EventBusSubscriber(modid = RollPig.MODID)
public final class RPChatQueue {

    private RPChatQueue() {
    }

    public static final int DELAY_TICKS = 40;

    private static final List<Pending> QUEUE = new ArrayList<>();
    private static MinecraftServer owner;

    private record Pending(long dueTime, Component message, UUID target) {
    }

    /** 到点之后广播给所有人。 */
    public static void broadcastLater(MinecraftServer server, Component message, int delayTicks) {
        long due = server.overworld().getGameTime() + Math.max(0, delayTicks);
        QUEUE.add(new Pending(due, message, null));
    }

    /** 到点之后只给某一位玩家。玩家中途下线就不发了。 */
    public static void sendLater(ServerPlayer player, Component message, int delayTicks) {
        MinecraftServer server = player.getServer();
        if (server == null) {
            return;
        }
        long due = server.overworld().getGameTime() + Math.max(0, delayTicks);
        QUEUE.add(new Pending(due, message, player.getUUID()));
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
            if (pending.target() == null) {
                server.getPlayerList().broadcastSystemMessage(pending.message(), false);
            } else {
                ServerPlayer player = server.getPlayerList().getPlayer(pending.target());
                if (player != null) {
                    player.displayClientMessage(pending.message(), false);
                }
            }
        }
    }
}
