package com.chinaex123.roll_pig.mixin.client;

import com.chinaex123.roll_pig.client.gui.PigTextures;
import com.chinaex123.roll_pig.util.PigGlyphs;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.GlyphRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Style;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.client.gui.Font$StringRenderOutput")
public abstract class PigFontRenderMixin {

    @Shadow
    @Final
    MultiBufferSource bufferSource;

    @Shadow
    @Final
    private boolean dropShadow;

    @Shadow
    @Final
    private float a;

    @Shadow
    @Final
    private Matrix4f pose;

    @Shadow
    @Final
    private Font.DisplayMode mode;

    @Shadow
    @Final
    private int packedLightCoords;

    @Shadow
    float x;

    @Shadow
    float y;

    @Inject(method = "accept(ILnet/minecraft/network/chat/Style;I)Z", at = @At("HEAD"), cancellable = true)
    private void rollpig$drawPig(int positionInCurrentSequence, Style style, int codePoint,
                                 CallbackInfoReturnable<Boolean> callback) {
        PigGlyphs.Glyph glyph = PigGlyphs.get(codePoint);
        if (glyph == null) {
            return;
        }
        // 阴影通道只前进不画图，避免同一张图被画两遍、边缘发灰。
        if (!this.dropShadow) {
            this.rollpig$drawGlyph(glyph);
        }
        this.x += glyph.size();
        callback.setReturnValue(true);
    }

    @Unique
    private void rollpig$drawGlyph(PigGlyphs.Glyph glyph) {
        PigTextures.Sprite sprite = PigTextures.get(glyph.pigId());
        if (sprite == null) {
            return;
        }
        float size = glyph.size();
        // 非正方形图片按比例缩放并水平居中，和 GUI 里的画法保持一致。
        float scale = Math.min(size / sprite.width(), size / sprite.height());
        float width = sprite.width() * scale;
        float height = sprite.height() * scale;
        float left = this.x + (size - width) * 0.5F;
        float right = left + width;
        float bottom = this.y + glyph.bottom();
        float top = bottom - height;

        RenderType renderType = GlyphRenderTypes.createForColorTexture(sprite.texture()).select(this.mode);
        VertexConsumer buffer = this.bufferSource.getBuffer(renderType);
        float alpha = this.a;
        buffer.addVertex(this.pose, left, top, 0.0F).setColor(1.0F, 1.0F, 1.0F, alpha).setUv(0.0F, 0.0F)
                .setLight(this.packedLightCoords);
        buffer.addVertex(this.pose, left, bottom, 0.0F).setColor(1.0F, 1.0F, 1.0F, alpha).setUv(0.0F, 1.0F)
                .setLight(this.packedLightCoords);
        buffer.addVertex(this.pose, right, bottom, 0.0F).setColor(1.0F, 1.0F, 1.0F, alpha).setUv(1.0F, 1.0F)
                .setLight(this.packedLightCoords);
        buffer.addVertex(this.pose, right, top, 0.0F).setColor(1.0F, 1.0F, 1.0F, alpha).setUv(1.0F, 0.0F)
                .setLight(this.packedLightCoords);
    }
}
