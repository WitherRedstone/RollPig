package com.chinaex123.roll_pig.network;

import com.chinaex123.roll_pig.RollPig;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Map;

public record PigPenPayload(int mode, boolean canRoll, String highlight, Map<String, Integer> counts)
        implements CustomPacketPayload {

    /** 右键物品：直接打开浏览界面。 */
    public static final int MODE_BROWSE = 0;
    /** 指令抽取：先播图腾动画，再打开界面并选中结果。 */
    public static final int MODE_RESULT = 1;
    /** 在界面里点按钮抽取：就地刷新已打开的界面。 */
    public static final int MODE_UPDATE = 2;
    /** 天亮刷新：界面开着就原地更新，没开就丢掉。 */
    public static final int MODE_SYNC = 3;

    public static final CustomPacketPayload.Type<PigPenPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(RollPig.MODID, "pig_pen"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PigPenPayload> STREAM_CODEC =
            StreamCodec.of(PigPenPayload::encode, PigPenPayload::decode);

    private static void encode(RegistryFriendlyByteBuf buffer, PigPenPayload payload) {
        buffer.writeVarInt(payload.mode);
        buffer.writeBoolean(payload.canRoll);
        buffer.writeUtf(payload.highlight, 128);
        buffer.writeVarInt(payload.counts.size());
        for (Map.Entry<String, Integer> entry : payload.counts.entrySet()) {
            buffer.writeUtf(entry.getKey(), 128);
            buffer.writeVarInt(entry.getValue());
        }
    }

    private static PigPenPayload decode(RegistryFriendlyByteBuf buffer) {
        int mode = buffer.readVarInt();
        boolean canRoll = buffer.readBoolean();
        String highlight = buffer.readUtf(128);
        int size = buffer.readVarInt();
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (int i = 0; i < size; i++) {
            String id = buffer.readUtf(128);
            counts.put(id, buffer.readVarInt());
        }
        return new PigPenPayload(mode, canRoll, highlight, Map.copyOf(counts));
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
