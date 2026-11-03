package com.chinaex123.roll_pig.client.gui;

import com.chinaex123.roll_pig.RollPig;
import com.chinaex123.roll_pig.data.state.PigRegistry;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public final class PigTextures {

    private static final Map<String, Sprite> CACHE = new HashMap<>();

    private PigTextures() {
    }

    public static Sprite get(String pigId) {
        if (CACHE.containsKey(pigId)) {
            return CACHE.get(pigId);
        }
        Sprite sprite = load(pigId);
        CACHE.put(pigId, sprite);
        return sprite;
    }

    public static void clear() {
        for (Sprite sprite : CACHE.values()) {
            if (sprite != null) {
                sprite.close();
            }
        }
        CACHE.clear();
    }

    private static Sprite load(String pigId) {
        try (InputStream stream = PigRegistry.openImage(pigId)) {
            if (stream == null) {
                return null;
            }
            NativeImage image = NativeImage.read(stream);
            int width = image.getWidth();
            int height = image.getHeight();
            DynamicTexture texture = new DynamicTexture(image);
            texture.setFilter(true, false);
            ResourceLocation location = PigRegistry.imageLocation(pigId);
            Minecraft.getInstance().getTextureManager().register(location, texture);
            return new Sprite(location, width, height);
        } catch (Exception exception) {
            RollPig.LOGGER.warn("无法加载猪猪图片: {}", pigId, exception);
            return null;
        }
    }

    public record Sprite(ResourceLocation texture, int width, int height) {

        public void draw(GuiGraphics guiGraphics, int x, int y, int size) {
            guiGraphics.blit(this.texture, x, y, size, size, 0.0F, 0.0F, this.width, this.height, this.width, this.height);
        }

        public void close() {
            Minecraft.getInstance().getTextureManager().release(this.texture);
        }
    }
}
