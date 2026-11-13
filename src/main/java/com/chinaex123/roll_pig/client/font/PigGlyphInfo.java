package com.chinaex123.roll_pig.client.font;

import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.font.SheetGlyphInfo;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.gui.font.glyphs.EmptyGlyph;

import java.util.function.Function;

public record PigGlyphInfo(float advance) implements GlyphInfo {

    @Override
    public float getAdvance() {
        return this.advance;
    }

    @Override
    public float getBoldOffset() {
        return 0.0F;
    }

    @Override
    public float getShadowOffset() {
        return 0.0F;
    }

    @Override
    public BakedGlyph bake(Function<SheetGlyphInfo, BakedGlyph> glyphProvider) {
        return EmptyGlyph.INSTANCE;
    }
}
