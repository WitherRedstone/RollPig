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
                RollPig.LOGGER.warn("找不到猪猪图片 {}：如果这是自定义猪，图片应该放在 {}",
                        pigId, PigRegistry.customImageFile(pigId).toAbsolutePath());
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
            RollPig.LOGGER.warn("无法加载猪猪图片 {}：只支持真正的 PNG 文件，改扩展名不管用（自定义图应放在 {}）",
                    pigId, PigRegistry.customImageFile(pigId).toAbsolutePath(), exception);
            return null;
        }
    }

    public record Sprite(ResourceLocation texture, int width, int height) {

        /** 按原图比例缩放到 size×size 的方框里居中画，非正方形图片不会被拉扁。 */
        public void draw(GuiGraphics guiGraphics, int x, int y, int size) {
            float scale = Math.min((float) size / this.width, (float) size / this.height);
            int drawWidth = Math.max(1, Math.round(this.width * scale));
            int drawHeight = Math.max(1, Math.round(this.height * scale));
            int offsetX = x + (size - drawWidth) / 2;
            int offsetY = y + (size - drawHeight) / 2;
            guiGraphics.blit(this.texture, offsetX, offsetY, drawWidth, drawHeight,
                    0.0F, 0.0F, this.width, this.height, this.width, this.height);
        }

        public void close() {
            Minecraft.getInstance().getTextureManager().release(this.texture);
        }
    }
}
