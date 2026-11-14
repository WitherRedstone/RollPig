package com.chinaex123.roll_pig.client.font;

import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.GlyphRenderTypes;
import net.minecraft.client.gui.font.TextRenderable;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import org.joml.Matrix4fc;
import org.jspecify.annotations.Nullable;

public final class PigBakedGlyph implements BakedGlyph {

    private final Identifier texture;
    private final GlyphInfo info;
    private final GlyphRenderTypes renderTypes;
    private final float size;
    private final float bottom;

    public PigBakedGlyph(Identifier texture, int size, float bottom) {
        this.texture = texture;
        this.info = GlyphInfo.simple(size);
        this.renderTypes = GlyphRenderTypes.createForColorTexture(texture);
        this.size = size;
        this.bottom = bottom;
    }

    @Override
    public GlyphInfo info() {
        return this.info;
    }

    @Override
    public TextRenderable.@Nullable Styled createGlyph(float x, float y, int color, int shadowColor, Style style,
                                                       float boldOffset, float shadowOffset) {
        float left = x;
        float right = x + this.size;
        float bottomY = y + this.bottom;
        float top = bottomY - this.size;
        return new Instance(this.texture, this.renderTypes, left, top, right, bottomY, color, style);
    }

    private record Instance(Identifier texture, GlyphRenderTypes renderTypes,
                            float left, float top, float right, float bottom, int color, Style style)
            implements TextRenderable.Styled {

        @Override
        public Style style() {
            return this.style;
        }

        @Override
        public void render(Matrix4fc pose, VertexConsumer consumer, int packedLight, boolean noDepth) {
            float z = noDepth ? 0.0F : 0.001F;
            consumer.addVertex(pose, this.left, this.bottom, z).setColor(this.color).setUv(0.0F, 1.0F).setLight(packedLight);
            consumer.addVertex(pose, this.right, this.bottom, z).setColor(this.color).setUv(1.0F, 1.0F).setLight(packedLight);
            consumer.addVertex(pose, this.right, this.top, z).setColor(this.color).setUv(1.0F, 0.0F).setLight(packedLight);
            consumer.addVertex(pose, this.left, this.top, z).setColor(this.color).setUv(0.0F, 0.0F).setLight(packedLight);
        }

        @Override
        public RenderType renderType(Font.DisplayMode displayMode, boolean blur) {
            return this.renderTypes.select(displayMode, blur);
        }

        @Override
        @Deprecated
        public RenderType renderType(Font.DisplayMode displayMode) {
            return this.renderTypes.select(displayMode);
        }

        @Override
        public GpuTextureView textureView() {
            return Minecraft.getInstance().getTextureManager().getTexture(this.texture).getTextureView();
        }

        @Override
        public RenderPipeline guiPipeline() {
            return this.renderTypes.guiPipeline();
        }

        @Override
        public float left() {
            return this.left;
        }

        @Override
        public float top() {
            return this.top;
        }

        @Override
        public float right() {
            return this.right;
        }

        @Override
        public float bottom() {
            return this.bottom;
        }
    }
}