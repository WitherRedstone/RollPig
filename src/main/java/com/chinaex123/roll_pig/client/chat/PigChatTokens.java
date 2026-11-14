package com.chinaex123.roll_pig.client.chat;

import com.chinaex123.roll_pig.client.ClientPigData;
import com.chinaex123.roll_pig.data.state.PigRegistry;
import com.chinaex123.roll_pig.util.Pig;
import com.chinaex123.roll_pig.util.PigGlyphs;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class PigChatTokens {

    private static final Pattern TOKEN = Pattern.compile("\\[([^\\[\\]\\s]{1,32})]");

    public static Component decorate(Component message) {
        return decorate(message, false);
    }

    public static Component decorate(Component message, boolean requireUnlocked) {
        boolean[] changed = {false};
        MutableComponent out = Component.empty().setStyle(message.getStyle());
        message.visit((style, text) -> {
            out.append(replace(text, style, changed, requireUnlocked));
            return Optional.empty();
        }, Style.EMPTY);

        if (!changed[0]) {
            return message;
        }
        return out;
    }

    private static Component replace(String text, Style style, boolean[] changed, boolean requireUnlocked) {
        Matcher matcher = TOKEN.matcher(text);
        if (!matcher.find()) {
            return Component.literal(text).withStyle(style);
        }
        MutableComponent part = Component.empty().withStyle(style);
        int last = 0;
        do {
            Pig pig = resolve(matcher.group(1), requireUnlocked);
            if (pig == null) {
                continue;
            }
            if (matcher.start() > last) {
                part.append(Component.literal(text.substring(last, matcher.start())));
            }
            part.append(image(pig));
            last = matcher.end();
            changed[0] = true;
        } while (matcher.find());

        if (last < text.length()) {
            part.append(Component.literal(text.substring(last)));
        }
        return part;
    }

    private static Pig resolve(String token, boolean requireUnlocked) {
        for (Pig pig : PigRegistry.all()) {
            if (pig.id().equals(token) && (!requireUnlocked || ClientPigData.countOf(pig.id()) > 0)) {
                return pig;
            }
        }
        for (Pig pig : PigRegistry.all()) {
            if (pig.name().equals(token) && (!requireUnlocked || ClientPigData.countOf(pig.id()) > 0)) {
                return pig;
            }
        }
        return null;
    }

    private static MutableComponent image(Pig pig) {
        MutableComponent glyph = PigGlyphs.sticker(pig.id());
        if (glyph.getString().isEmpty()) {
            return Component.literal("[" + pig.name() + "]");
        }
        return glyph.withStyle(style -> style
                .withInsertion(pig.id())
                .withHoverEvent(new HoverEvent.ShowText(Component.literal(pig.name()))));
    }
}
