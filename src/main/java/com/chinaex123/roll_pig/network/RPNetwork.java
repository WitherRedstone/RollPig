package com.chinaex123.roll_pig.network;

import com.chinaex123.roll_pig.RollPig;
import com.chinaex123.roll_pig.client.RPClientPayloadHandler;
import com.chinaex123.roll_pig.event.RollPigService;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = RollPig.MODID)
public final class RPNetwork {

    private RPNetwork() {
    }

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");

        registrar.playToClient(PigPenPayload.TYPE, PigPenPayload.STREAM_CODEC,
                (payload, context) -> RPClientPayloadHandler.handle(payload));

        registrar.playToServer(RollRequestPayload.TYPE, RollRequestPayload.STREAM_CODEC,
                (payload, context) -> RollPigService.handleRequest((ServerPlayer) context.player()));
    }

    public static void sendTo(ServerPlayer player, PigPenPayload payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }

    public static void requestRoll() {
        PacketDistributor.sendToServer(RollRequestPayload.INSTANCE);
    }
}
