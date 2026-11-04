package com.chinaex123.roll_pig.data.state;

import com.chinaex123.roll_pig.RollPig;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.loading.FMLPaths;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public final class RPGlyphs {

    public static final ResourceLocation FONT = ResourceLocation.fromNamespaceAndPath(RollPig.MODID, "pig_icons");

    private static final String BUILTIN_MANIFEST = "/assets/roll_pig/pig_glyphs.json";

    /** 扩展对照表：整合包作者重新生成图集后，把新的 id → 码位 写到这里即可。 */
    public static final String CUSTOM_MANIFEST = "roll_pig/pig_glyphs.json";

    /** 图集里留了一格通用头像，给没有专属图标的猪猪用。 */
    private static final String FALLBACK_ID = "__custom__";

    private static volatile Map<String, Integer> cache;

    public static MutableComponent icon(String pigId) {
        int codepoint = codepoint(pigId);
        if (codepoint < 0) {
            codepoint = codepoint(FALLBACK_ID);
        }
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

    /** 扩展对照表的位置：config/roll_pig/pig_glyphs.json。 */
    public static Path customManifestPath() {
        return FMLPaths.CONFIGDIR.get().resolve(CUSTOM_MANIFEST);
    }

    public static void reload() {
        synchronized (RPGlyphs.class) {
            cache = null;
        }
    }

    /** 启动时预热一次，顺便把「扩展对照表加载了几条」打进日志，方便排查。 */
    public static void warmup() {
        mapSize();
    }

    public static int mapSize() {
        return load().size();
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
        readInto(glyphs, builtinStream(), BUILTIN_MANIFEST);

        Path custom = customManifestPath();
        if (Files.isRegularFile(custom)) {
            int before = glyphs.size();
            try (InputStream stream = Files.newInputStream(custom)) {
                readInto(glyphs, stream, custom.toString());
            } catch (Exception exception) {
                RollPig.LOGGER.warn("读取扩展头像对照表失败: {}", custom, exception);
            }
            RollPig.LOGGER.info("扩展头像对照表: {} 条（新增 {}）", glyphs.size(), glyphs.size() - before);
        }
        return Map.copyOf(glyphs);
    }

    private static InputStream builtinStream() {
        return RPGlyphs.class.getResourceAsStream(BUILTIN_MANIFEST);
    }

    private static void readInto(Map<String, Integer> glyphs, InputStream stream, String source) {
        if (stream == null) {
            RollPig.LOGGER.warn("找不到头像字体对照表 {}", source);
            return;
        }
        try (InputStream in = stream) {
            String text = new String(in.readAllBytes(), StandardCharsets.UTF_8).replace("\uFEFF", "");
            JsonObject root = JsonParser.parseString(text).getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
                try {
                    glyphs.put(entry.getKey(), entry.getValue().getAsInt());
                } catch (Exception ignored) {
                    // 单条坏数据不拖垮整张表
                }
            }
        } catch (Exception exception) {
            RollPig.LOGGER.warn("读取头像字体对照表失败: {}", source, exception);
        }
    }
}
