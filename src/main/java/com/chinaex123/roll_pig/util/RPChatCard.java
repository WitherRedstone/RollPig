package com.chinaex123.roll_pig.util;

import com.chinaex123.roll_pig.data.state.RPGlyphs;
import com.chinaex123.roll_pig.data.state.RollPigState;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

public final class RPChatCard {

    private static final String INDENT = "         ";
    private static final String DIVIDER = "=======================================";

    public static Component build(ServerPlayer player, Pig pig, RollPigState state) {
        MutableComponent card = Component.empty();

        card.append(divider());
        card.append("\n");

        card.append(Component.translatable("message.roll_pig.card.title", player.getDisplayName())
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        card.append("\n");

        card.append(RPGlyphs.icon(pig.id()));
        card.append("\n");

        card.append(Component.literal(INDENT));
        card.append(Component.literal(pig.name())
                .withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD));
        card.append("\n");

        card.append(Component.literal(INDENT));
        card.append(Component.translatable("message.roll_pig.card.description",
                        Component.literal(pig.description()).withStyle(ChatFormatting.AQUA))
                .withStyle(ChatFormatting.WHITE));
        card.append("\n");

        card.append(Component.literal(INDENT));
        card.append(Component.translatable("message.roll_pig.card.collected", state.countOf(pig.id()))
                .withStyle(ChatFormatting.DARK_GRAY));

        card.append("\n\n");
        card.append(Component.translatable("message.roll_pig.card.overview")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        card.append("\n");
        card.append(Component.literal(pig.analysis()).withStyle(ChatFormatting.GRAY));

        card.append("\n");
        card.append(divider());

        return card;
    }

    private static Component divider() {
        return Component.literal(DIVIDER).withStyle(ChatFormatting.DARK_GRAY);
    }
}
