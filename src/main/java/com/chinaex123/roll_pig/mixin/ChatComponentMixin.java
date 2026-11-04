package com.chinaex123.roll_pig.mixin;

import com.chinaex123.roll_pig.client.chat.PigChatRenderer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatComponent.class)
public abstract class ChatComponentMixin {

    @Shadow
    public abstract double getScale();

    @Redirect(
            method = "render(Lnet/minecraft/client/gui/GuiGraphics;IIIZ)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/util/FormattedCharSequence;III)I"
            ))
    private static int rollpig$drawChatImage(GuiGraphics graphics, Font font, FormattedCharSequence text, int x, int y, int color) {
        PigChatRenderer.drawChatImage(graphics, font, text, x, y, color);
        return graphics.drawString(font, text, x, y, color);
    }

    @Inject(method = "render(Lnet/minecraft/client/gui/GuiGraphics;IIIZ)V", at = @At("TAIL"))
    private void rollpig$flushInlineImages(GuiGraphics graphics, int tickCount, int mouseX, int mouseY,
                                           boolean focused, CallbackInfo callback) {
        PigChatRenderer.flushInline(graphics, (float) this.getScale());
    }
}
