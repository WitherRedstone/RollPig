package com.chinaex123.roll_pig.client;

import com.chinaex123.roll_pig.client.gui.PigPenScreen;
import com.chinaex123.roll_pig.init.RPItems;
import com.chinaex123.roll_pig.network.PigPenPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;

public final class RPClientPayloadHandler {

    private static final int TOTEM_TICKS = 30;

    private RPClientPayloadHandler() {
    }

    public static void handle(PigPenPayload payload) {
        Minecraft minecraft = Minecraft.getInstance();

        // 聊天框里的猪圈面板要用到「已解锁了哪些猪」，顺手记下来
        ClientPigData.update(payload.counts());

        switch (payload.mode()) {
            case PigPenPayload.MODE_BROWSE -> minecraft.setScreen(new PigPenScreen(payload));
            case PigPenPayload.MODE_RESULT -> {
                playTotemAnimation();
                RPClientTicker.schedule(TOTEM_TICKS, () -> minecraft.setScreen(new PigPenScreen(payload)));
            }
            case PigPenPayload.MODE_UPDATE -> {
                if (minecraft.screen instanceof PigPenScreen screen) {
                    screen.refresh(payload);
                } else {
                    minecraft.setScreen(new PigPenScreen(payload));
                }
            }
            // 天亮刷新：只在界面开着时原地更新，没开就悄悄丢掉
            case PigPenPayload.MODE_SYNC -> {
                if (minecraft.screen instanceof PigPenScreen screen) {
                    screen.refresh(payload);
                }
            }
            default -> {
            }
        }
    }

    private static void playTotemAnimation() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            return;
        }
        minecraft.gameRenderer.displayItemActivation(new ItemStack(RPItems.PIGPEN.get()));
        minecraft.level.playLocalSound(minecraft.player.getX(), minecraft.player.getY(), minecraft.player.getZ(),
                SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0F, 1.0F, false);
    }
}
