package com.chinaex123.roll_pig.client.chat;

import com.chinaex123.roll_pig.client.gui.PigTextures;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public final class PigChatRenderer {

    private static final List<Pending> PENDING = new ArrayList<>();

    private PigChatRenderer() {
    }

    public static void drawChatImage(GuiGraphics graphics, Font font, FormattedCharSequence line, int x, int y, int color) {
        List<Scan> hits = scan(font, line);
        if (hits.isEmpty()) {
            return;
        }

        int lineHeight = lineHeight();
        int baseline = baselineOffset();
        int top = y - baseline - lineHeight;
        int imageTop = top + (lineHeight - PigChatTokens.IMAGE_SIZE) / 2;

        for (Scan scan : hits) {
            PENDING.add(new Pending(scan.pigId(), x + scan.offset(), imageTop, color >>> 24));
        }
    }

    public static void flushInline(GuiGraphics graphics, float scale) {
        if (PENDING.isEmpty()) {
            return;
        }
        graphics.pose().pushPose();
        graphics.pose().scale(scale, scale, 1.0F);
        graphics.pose().translate(4.0F, 0.0F, 0.0F);
        graphics.pose().translate(0.0F, 0.0F, 60.0F);
        for (Pending pending : PENDING) {
            PigTextures.Sprite sprite = PigTextures.get(pending.pigId());
            if (sprite != null) {
                draw(graphics, sprite, pending.x(), pending.y(), PigChatTokens.IMAGE_SIZE, pending.alpha());
            }
        }
        graphics.pose().popPose();
        PENDING.clear();
    }

    private static void draw(GuiGraphics graphics, PigTextures.Sprite sprite, int x, int y, int size, int alpha) {
        if (size <= 0 || alpha <= 2) {
            return;
        }
        if (alpha >= 255) {
            sprite.draw(graphics, x, y, size);
            return;
        }
        graphics.flush();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha / 255.0F);
        sprite.draw(graphics, x, y, size);
        graphics.flush();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private static List<Scan> scan(Font font, FormattedCharSequence line) {
        List<Scan> hits = new ArrayList<>();
        List<FormattedCharSequence> prefix = new ArrayList<>();
        line.accept((index, style, codePoint) -> {
            if (codePoint == PigChatTokens.ANCHOR) {
                String pigId = style.getInsertion();
                if (pigId == null || pigId.isEmpty()) {
                    pigId = pigIdFromHover(style.getHoverEvent());
                }
                if (pigId != null && !pigId.isEmpty()) {
                    int offset = prefix.isEmpty() ? 0 : font.width(FormattedCharSequence.composite(prefix));
                    hits.add(new Scan(pigId, offset));
                }
            }
            prefix.add(FormattedCharSequence.codepoint(codePoint, style));
            return true;
        });
        return hits;
    }

    private static String pigIdFromHover(HoverEvent hover) {
        if (hover == null) {
            return null;
        }
        Component content = hover.getValue(HoverEvent.Action.SHOW_TEXT);
        return content == null ? null : content.getString();
    }

    private static int lineHeight() {
        double spacing = Minecraft.getInstance().options.chatLineSpacing().get();
        return (int) (9.0D * (spacing + 1.0D));
    }

    private static int baselineOffset() {
        double spacing = Minecraft.getInstance().options.chatLineSpacing().get();
        return (int) Math.round(-8.0D * (spacing + 1.0D) + 4.0D * spacing);
    }

    private record Scan(String pigId, int offset) {
    }

    private record Pending(String pigId, int x, int y, int alpha) {
    }
}
