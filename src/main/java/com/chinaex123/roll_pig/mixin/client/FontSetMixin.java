package com.chinaex123.roll_pig.mixin.client;

import com.chinaex123.roll_pig.client.font.PigGlyphInfo;
import com.chinaex123.roll_pig.util.PigGlyphs;
import com.mojang.blaze3d.font.GlyphInfo;
import net.minecraft.client.gui.font.FontSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FontSet.class)
public class FontSetMixin {

    @Inject(method = "getGlyphInfo(IZ)Lcom/mojang/blaze3d/font/GlyphInfo;", at = @At("HEAD"), cancellable = true)
    private void rollpig$pigGlyph(int codePoint, boolean filterFishy, CallbackInfoReturnable<GlyphInfo> callback) {
        PigGlyphs.Glyph glyph = PigGlyphs.get(codePoint);
        if (glyph != null) {
            callback.setReturnValue(new PigGlyphInfo(glyph.size()));
        }
    }
}
