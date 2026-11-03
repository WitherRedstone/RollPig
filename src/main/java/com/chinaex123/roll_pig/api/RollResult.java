package com.chinaex123.roll_pig.api;

import com.chinaex123.roll_pig.data.state.Pig;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public record RollResult(boolean success, Pig pig, Reason reason) {

    /** 抽取失败的原因。 */
    public enum Reason {
        /** 抽到了。 */
        SUCCESS,
        /** 这个周期已经抽过了，要等下次天亮。 */
        ALREADY_ROLLED,
        /** 一只猪都没有（资源缺失）。 */
        NO_PIGS,
        /** 被 {@link RollPigEvent.Pre} 的监听者取消了。 */
        CANCELLED
    }

    public static RollResult success(Pig pig) {
        return new RollResult(true, pig, Reason.SUCCESS);
    }

    public static RollResult failure(Reason reason) {
        return new RollResult(false, null, reason);
    }

    public Component message() {
        return switch (this.reason) {
            case SUCCESS -> Component.translatable("message.roll_pig.rolled", this.pig.name())
                    .withStyle(ChatFormatting.GOLD);
            case ALREADY_ROLLED -> Component.translatable("message.roll_pig.no_chance")
                    .withStyle(ChatFormatting.RED);
            case NO_PIGS -> Component.translatable("message.roll_pig.no_pigs")
                    .withStyle(ChatFormatting.RED);
            case CANCELLED -> Component.translatable("message.roll_pig.cancelled")
                    .withStyle(ChatFormatting.RED);
        };
    }
}
