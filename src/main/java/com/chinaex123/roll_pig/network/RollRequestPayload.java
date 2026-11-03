package com.chinaex123.roll_pig.network;

import com.chinaex123.roll_pig.RollPig;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record RollRequestPayload() implements CustomPacketPayload {

    public static final RollRequestPayload INSTANCE = new RollRequestPayload();

    public static final CustomPacketPayload.Type<RollRequestPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(RollPig.MODID, "roll_request"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RollRequestPayload> STREAM_CODEC =
            StreamCodec.of(RollRequestPayload::encode, RollRequestPayload::decode);

    private static void encode(RegistryFriendlyByteBuf buffer, RollRequestPayload payload) { }

    private static RollRequestPayload decode(RegistryFriendlyByteBuf buffer) {
        return INSTANCE;
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
