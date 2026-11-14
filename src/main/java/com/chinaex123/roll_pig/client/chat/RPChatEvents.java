package com.chinaex123.roll_pig.client.chat;

import com.chinaex123.roll_pig.RollPig;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientChatReceivedEvent;

import java.util.UUID;

@EventBusSubscriber(modid = RollPig.MODID, value = Dist.CLIENT)
public final class RPChatEvents {

    private RPChatEvents() {
    }

    @SubscribeEvent
    public static void onChatReceived(ClientChatReceivedEvent event) {
        Component message = event.getMessage();
        Component decorated = PigChatTokens.decorate(message, isLocalPlayer(event.getSender()));
        if (decorated != message) {
            event.setMessage(decorated);
        }
    }

    private static boolean isLocalPlayer(UUID sender) {
        return Minecraft.getInstance().player != null && Minecraft.getInstance().player.getUUID().equals(sender);
    }
}
