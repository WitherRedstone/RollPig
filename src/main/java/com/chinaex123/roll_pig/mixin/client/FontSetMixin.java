package com.chinaex123.roll_pig.mixin.client;

import com.chinaex123.roll_pig.client.font.PigBakedGlyph;
import com.chinaex123.roll_pig.client.gui.PigTextures;
import com.chinaex123.roll_pig.util.PigGlyphs;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.client.gui.font.FontSet$Source")
public class FontSetMixin {

    @Inject(method = "getGlyph(I)Lnet/minecraft/client/gui/font/glyphs/BakedGlyph;", at = @At("HEAD"), cancellable = true)
    private void rollpig$pigGlyph(int codePoint, CallbackInfoReturnable<BakedGlyph> callback) {
        PigGlyphs.Glyph glyph = PigGlyphs.get(codePoint);
        if (glyph == null) {
            return;
        }
        PigTextures.Sprite sprite = PigTextures.get(glyph.pigId());
        if (sprite == null) {
            return;
        }
        callback.setReturnValue(new PigBakedGlyph(sprite.texture(), glyph.size(), glyph.bottom()));
    }
}
