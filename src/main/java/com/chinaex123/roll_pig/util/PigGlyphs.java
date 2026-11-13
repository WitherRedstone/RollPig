package com.chinaex123.roll_pig.util;

import com.chinaex123.roll_pig.data.state.PigRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class PigGlyphs {

    /** 行内小图标的码位起点。 */
    public static final int STICKER_BASE = 0xE000;

    /** 行内小图标的边长（像素）。 */
    public static final int STICKER_SIZE = 12;

    /** 聊天卡片头像的码位起点。 */
    public static final int CARD_BASE = 0xF000;

    /** 聊天卡片头像的边长（像素）。 */
    public static final int CARD_SIZE = 36;

    /** 每段码位最多容纳的猪猪数量。 */
    private static final int RANGE = 0x1000;

    /** 行内小图标在 9px 行高里的垂直位置：底边落在行顶下方 10.5px，视觉上居中。 */
    private static final float STICKER_BOTTOM = 10.5F;

    /** 卡片头像的底边落在基线上。 */
    private static final float CARD_BOTTOM = 8.0F;

    private static volatile Snapshot snapshot;

    public record Glyph(String pigId, int size, float bottom) {
    }

    private record Snapshot(Map<Integer, Glyph> byCode, Map<String, Integer> sticker, Map<String, Integer> card) {
    }

    public static Glyph get(int codePoint) {
        return data().byCode().get(codePoint);
    }

    public static MutableComponent sticker(String pigId) {
        return literal(data().sticker().get(pigId));
    }

    public static MutableComponent card(String pigId) {
        return literal(data().card().get(pigId));
    }

    public static int stickerCode(String pigId) {
        Integer code = data().sticker().get(pigId);
        return code == null ? -1 : code;
    }

    public static int cardCode(String pigId) {
        Integer code = data().card().get(pigId);
        return code == null ? -1 : code;
    }

    public static void invalidate() {
        snapshot = null;
    }

    private static MutableComponent literal(Integer codePoint) {
        if (codePoint == null) {
            return Component.empty();
        }
        return Component.literal(new String(Character.toChars(codePoint)));
    }

    private static Snapshot data() {
        Snapshot current = snapshot;
        if (current != null) {
            return current;
        }
        synchronized (PigGlyphs.class) {
            if (snapshot == null) {
                snapshot = build();
            }
            return snapshot;
        }
    }

    private static Snapshot build() {
        List<Pig> pigs = new ArrayList<>(PigRegistry.all());
        Map<Integer, Glyph> byCode = new HashMap<>(pigs.size() * 2);
        Map<String, Integer> sticker = new HashMap<>(pigs.size() * 2);
        Map<String, Integer> card = new HashMap<>(pigs.size() * 2);
        for (int i = 0; i < pigs.size(); i++) {
            if (i >= RANGE) {
                break;
            }
            String id = pigs.get(i).id();
            int stickerCode = STICKER_BASE + i;
            int cardCode = CARD_BASE + i;
            byCode.put(stickerCode, new Glyph(id, STICKER_SIZE, STICKER_BOTTOM));
            byCode.put(cardCode, new Glyph(id, CARD_SIZE, CARD_BOTTOM));
            sticker.put(id, stickerCode);
            card.put(id, cardCode);
        }
        return new Snapshot(Map.copyOf(byCode), Map.copyOf(sticker), Map.copyOf(card));
    }
}
