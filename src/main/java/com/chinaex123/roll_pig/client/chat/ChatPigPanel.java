package com.chinaex123.roll_pig.client.chat;

import com.chinaex123.roll_pig.RollPig;
import com.chinaex123.roll_pig.client.ClientPigData;
import com.chinaex123.roll_pig.client.gui.PigTextures;
import com.chinaex123.roll_pig.util.Pig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

import java.util.List;

@EventBusSubscriber(modid = RollPig.MODID, value = Dist.CLIENT)
public final class ChatPigPanel {
    private static final ResourceLocation ICON = ResourceLocation.fromNamespaceAndPath(RollPig.MODID, "textures/item/pigpen.png");

    // ---- 配色 ----
    private static final int BG = 0xFF14161D;
    private static final int BORDER = 0xFF343B4B;
    private static final int BORDER_HI = 0xFF4E566B;
    private static final int GOLD = 0xFFD8B15A;
    private static final int TEXT = 0xFFE8E8E8;
    private static final int MUTED = 0xFF8B909C;
    private static final int HOVER = 0x33E0453E;
    private static final int ICON_HOVER = 0x40FFFFFF;

    // ---- 尺寸 ----
    private static final int ICON_SIZE = 12;
    private static final int COLS = 8;
    private static final int ROWS = 4;
    private static final int CELL = 24;
    private static final int PAD = 6;
    private static final int HEADER = 15;
    private static final int FOOTER = 12;
    private static final int PER_PAGE = COLS * ROWS;
    private static final int PANEL_W = COLS * CELL + PAD * 2;
    private static final int PANEL_H = HEADER + ROWS * CELL + FOOTER + PAD;

    private static boolean open;
    private static int page;

    private ChatPigPanel() {
    }

    @SubscribeEvent
    public static void onRender(ScreenEvent.Render.Post event) {
        if (!(event.getScreen() instanceof ChatScreen screen)) {
            return;
        }
        if (Minecraft.getInstance().options.hideGui) {
            return;
        }
        GuiGraphics graphics = event.getGuiGraphics();
        drawIcon(graphics, screen, event.getMouseX(), event.getMouseY());
        if (open) {
            drawPanel(graphics, screen, event.getMouseX(), event.getMouseY());
        }
    }

    private static void drawIcon(GuiGraphics graphics, Screen screen, int mouseX, int mouseY) {
        int x = iconX(screen);
        int y = iconY(screen);
        boolean hovered = inside(mouseX, mouseY, x - 2, y, ICON_SIZE + 4, ICON_SIZE);
        if (open || hovered) {
            graphics.fill(x - 2, y, x + ICON_SIZE + 2, y + ICON_SIZE, ICON_HOVER);
        }
        graphics.blit(ICON, x, y, ICON_SIZE, ICON_SIZE, 0.0F, 0.0F, 16, 16, 16, 16);
        if (hovered && !open) {
            graphics.renderTooltip(fontOf(screen),
                    Component.translatable("gui.roll_pig.panel_tip"), mouseX, mouseY);
        }
    }

    private static void drawPanel(GuiGraphics graphics, Screen screen, int mouseX, int mouseY) {
        Font font = fontOf(screen);
        List<Pig> pigs = ClientPigData.unlocked();
        int x = panelX(screen);
        int y = panelY(screen);

        graphics.fill(x - 1, y - 1, x + PANEL_W + 1, y + PANEL_H + 1, 0xB0000000);
        graphics.fill(x, y, x + PANEL_W, y + PANEL_H, BG);
        graphics.fill(x, y, x + PANEL_W, y + 1, BORDER_HI);
        graphics.fill(x, y + PANEL_H - 1, x + PANEL_W, y + PANEL_H, BORDER);
        graphics.fill(x, y + HEADER - 1, x + PANEL_W, y + HEADER, BORDER);

        graphics.drawString(font, Component.translatable("gui.roll_pig.panel_title"),
                x + PAD, y + 4, GOLD, false);
        Component count = Component.translatable("gui.roll_pig.panel_count", ClientPigData.unlockedCount());
        graphics.drawString(font, count, x + PANEL_W - PAD - font.width(count), y + 4, MUTED, false);

        if (pigs.isEmpty()) {
            Component empty = Component.translatable("gui.roll_pig.panel_empty");
            graphics.drawString(font, empty,
                    x + (PANEL_W - font.width(empty)) / 2,
                    y + HEADER + ROWS * CELL / 2 - 4, MUTED, false);
        } else {
            Pig hovered = null;
            int start = page * PER_PAGE;
            for (int i = 0; i < PER_PAGE; i++) {
                int index = start + i;
                if (index >= pigs.size()) {
                    break;
                }
                int cellX = x + PAD + (i % COLS) * CELL;
                int cellY = y + HEADER + (i / COLS) * CELL;
                boolean hover = inside(mouseX, mouseY, cellX, cellY, CELL, CELL);
                if (hover) {
                    graphics.fill(cellX, cellY, cellX + CELL, cellY + CELL, HOVER);
                }
                Pig pig = pigs.get(index);
                drawPig(graphics, pig.id(), cellX + 2, cellY + 2, CELL - 4);
                if (hover) {
                    hovered = pig;
                }
            }
            if (hovered != null) {
                graphics.renderTooltip(font, Component.literal(hovered.name()), mouseX, mouseY);
            }
        }

        drawFooter(graphics, font, screen, pigs.size(), mouseX, mouseY);
    }

    private static void drawFooter(GuiGraphics graphics, Font font, Screen screen, int size, int mouseX, int mouseY) {
        int pages = pageCount(size);
        int y = footerY(screen);
        int x = panelX(screen);

        if (pages > 1) {
            boolean prevHover = inside(mouseX, mouseY, x + PAD, y - 1, 10, 10);
            boolean nextHover = inside(mouseX, mouseY, x + PANEL_W - PAD - 10, y - 1, 10, 10);
            graphics.drawString(font, Component.literal("<"), x + PAD,
                    y, page > 0 ? (prevHover ? TEXT : MUTED) : 0xFF3C414D, false);
            graphics.drawString(font, Component.literal(">"), x + PANEL_W - PAD - 6,
                    y, page < pages - 1 ? (nextHover ? TEXT : MUTED) : 0xFF3C414D, false);
        }

        Component label = Component.translatable("gui.roll_pig.panel_page", Math.min(page + 1, pages), pages);
        graphics.drawString(font, label, x + (PANEL_W - font.width(label)) / 2, y, MUTED, false);
    }

    @SubscribeEvent
    public static void onClick(ScreenEvent.MouseButtonPressed.Pre event) {
        if (!(event.getScreen() instanceof ChatScreen screen) || event.getButton() != 0) {
            return;
        }
        double mouseX = event.getMouseX();
        double mouseY = event.getMouseY();

        if (open) {
            List<Pig> pigs = ClientPigData.unlocked();
            int hit = cellAt(screen, pigs, mouseX, mouseY);
            if (hit >= 0) {
                insertPig(screen, pigs.get(hit));
                event.setCanceled(true);
                return;
            }
            if (hitArrow(screen, pigs.size(), -1, mouseX, mouseY)) {
                page = Math.max(0, page - 1);
                playClick();
                event.setCanceled(true);
                return;
            }
            if (hitArrow(screen, pigs.size(), 1, mouseX, mouseY)) {
                page = Math.min(pageCount(pigs.size()) - 1, page + 1);
                playClick();
                event.setCanceled(true);
                return;
            }
            if (inside(mouseX, mouseY, panelX(screen), panelY(screen), PANEL_W, PANEL_H)) {
                event.setCanceled(true);
                return;
            }
            open = false;
        }

        if (inside(mouseX, mouseY, iconX(screen) - 2, iconY(screen), ICON_SIZE + 4, ICON_SIZE)) {
            open = true;
            page = 0;
            playClick();
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onScroll(ScreenEvent.MouseScrolled.Pre event) {
        if (!open || !(event.getScreen() instanceof ChatScreen screen)) {
            return;
        }
        if (!inside(event.getMouseX(), event.getMouseY(), panelX(screen), panelY(screen), PANEL_W, PANEL_H)) {
            return;
        }
        int pages = pageCount(ClientPigData.unlocked().size());
        if (pages <= 1) {
            return;
        }
        page = Mth.clamp(page + (event.getScrollDeltaY() > 0.0D ? -1 : 1), 0, pages - 1);
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onKey(ScreenEvent.KeyPressed.Pre event) {
        if (!open) {
            return;
        }
        if (event.getKeyCode() == 256) {
            open = false;
            event.setCanceled(true);
        } else if (event.getKeyCode() == 257) {
            open = false;
        }
    }

    @SubscribeEvent
    public static void onClosing(ScreenEvent.Closing event) {
        if (event.getScreen() instanceof ChatScreen) {
            open = false;
        }
    }

    private static void insertPig(Screen screen, Pig pig) {
        if (screen instanceof PigChatInput input) {
            input.rollpig$insertToken("[" + pig.name() + "]");
        }
        playClick();
    }

    private static void playClick() {
        Minecraft.getInstance().getSoundManager()
                .play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    private static void drawPig(GuiGraphics graphics, String pigId, int x, int y, int size) {
        PigTextures.Sprite sprite = PigTextures.get(pigId);
        if (sprite == null) {
            graphics.fill(x, y, x + size, y + size, 0xFF2A2F3A);
            return;
        }
        sprite.draw(graphics, x, y, size);
    }

    private static int iconX(Screen screen) {
        return screen.width - 16;
    }

    private static int iconY(Screen screen) {
        return screen.height - 14;
    }

    private static int panelX(Screen screen) {
        return screen.width - 4 - PANEL_W;
    }

    private static int panelY(Screen screen) {
        return Math.max(2, screen.height - 18 - PANEL_H);
    }

    private static int footerY(Screen screen) {
        return panelY(screen) + HEADER + ROWS * CELL + 2;
    }

    private static int pageCount(int size) {
        return Math.max(1, (size + PER_PAGE - 1) / PER_PAGE);
    }

    private static int cellAt(Screen screen, List<Pig> pigs, double mouseX, double mouseY) {
        int x = panelX(screen);
        int y = panelY(screen);
        int start = page * PER_PAGE;
        for (int i = 0; i < PER_PAGE; i++) {
            int index = start + i;
            if (index >= pigs.size()) {
                break;
            }
            int cellX = x + PAD + (i % COLS) * CELL;
            int cellY = y + HEADER + (i / COLS) * CELL;
            if (inside(mouseX, mouseY, cellX, cellY, CELL, CELL)) {
                return index;
            }
        }
        return -1;
    }

    private static boolean hitArrow(Screen screen, int size, int direction, double mouseX, double mouseY) {
        if (pageCount(size) <= 1) {
            return false;
        }
        int y = footerY(screen) - 1;
        if (direction < 0) {
            return page > 0 && inside(mouseX, mouseY, panelX(screen) + PAD, y, 10, 10);
        }
        return page < pageCount(size) - 1
                && inside(mouseX, mouseY, panelX(screen) + PANEL_W - PAD - 10, y, 10, 10);
    }

    private static Font fontOf(Screen screen) {
        return Minecraft.getInstance().font;
    }

    private static boolean inside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }
}
