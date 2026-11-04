package com.chinaex123.roll_pig.mixin;

import com.chinaex123.roll_pig.client.chat.PigChatInput;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin implements PigChatInput {

    @Shadow
    protected EditBox input;

    @Inject(method = "init()V", at = @At("TAIL"))
    private void rollpig$reserveStickerButton(CallbackInfo callback) {
        if (this.input != null) {
            this.input.setWidth(this.input.getWidth() - 18);
        }
    }

    @Unique
    @Override
    public void rollpig$insertToken(String token) {
        if (this.input != null) {
            this.input.insertText(token);
        }
    }
}
