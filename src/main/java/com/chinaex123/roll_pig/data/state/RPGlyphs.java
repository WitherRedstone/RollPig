package com.chinaex123.roll_pig.data.state;

import com.chinaex123.roll_pig.RollPig;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public final class RPGlyphs {

    public static final ResourceLocation FONT = ResourceLocation.fromNamespaceAndPath(RollPig.MODID, "pig_icons");
    private static final String MANIFEST = "/assets/roll_pig/pig_glyphs.json";

    private static volatile Map<String, Integer> cache;

    public static MutableComponent icon(String pigId) {
        int codepoint = codepoint(pigId);
        if (codepoint < 0) {
            return Component.empty();
        }
        return Component.literal(new String(Character.toChars(codepoint)))
                .withStyle(style -> style.withFont(FONT));
    }

    public static int codepoint(String pigId) {
        if (pigId == null) {
            return -1;
        }
        return load().getOrDefault(pigId, -1);
    }

    private static Map<String, Integer> load() {
        Map<String, Integer> glyphs = cache;
        if (glyphs != null) {
            return glyphs;
        }
        synchronized (RPGlyphs.class) {
            if (cache == null) {
                cache = read();
            }
            return cache;
        }
    }

    private static Map<String, Integer> read() {
        Map<String, Integer> glyphs = new HashMap<>();
        try (InputStream stream = RPGlyphs.class.getResourceAsStream(MANIFEST)) {
            if (stream == null) {
                RollPig.LOGGER.warn("找不到头像字体对照表 {}", MANIFEST);
                return Map.of();
            }
            JsonObject root = JsonParser.parseReader(
                    new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
                glyphs.put(entry.getKey(), entry.getValue().getAsInt());
            }
        } catch (Exception exception) {
            RollPig.LOGGER.warn("读取头像字体对照表失败", exception);
            return Map.of();
        }
        return Map.copyOf(glyphs);
    }
}
