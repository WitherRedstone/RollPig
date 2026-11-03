package com.chinaex123.roll_pig.client.gui;

import com.chinaex123.roll_pig.client.RPClientTicker;
import com.chinaex123.roll_pig.util.Pig;
import com.chinaex123.roll_pig.data.state.PigRegistry;
import com.chinaex123.roll_pig.network.PigPenPayload;
import com.chinaex123.roll_pig.network.RPNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

import java.util.List;
import java.util.Map;

public class PigPenScreen extends Screen {

    // ---- 配色 ----
    private static final int DIM = 0xB4000000;
    private static final int PANEL = 0xFF16181F;
    private static final int BORDER = 0xFF3B4254;
    private static final int BORDER_SOFT = 0xFF272C38;
    private static final int HILIGHT = 0xFF4E566B;
    private static final int ACCENT = 0xFFE0453E;
    private static final int TEXT = 0xFFE8E8E8;
    private static final int MUTED = 0xFF8B909C;
    private static final int GOLD = 0xFFD8B15A;
    private static final int ROW_SELECTED = 0xFF2A3042;
    private static final int ROW_HOVER = 0x18FFFFFF;
    private static final int BUTTON_BG = 0xFF241A1A;
    private static final int BUTTON_BG_HOVER = 0xFF3A2020;
    private static final int BUTTON_BG_OFF = 0xFF1D1C20;

    // ---- 尺寸 ----
    private static final int PANEL_W = 336;
    private static final int PANEL_H = 218;
    private static final int ROW_H = 26;
    private static final int LIST_W = 128;
    private static final int PORTRAIT = 48;
    private static final int AVATAR = 16;
    private static final int BUTTON_H = 16;

    private final List<Pig> pigs = PigRegistry.all();

    private boolean canRoll;
    private boolean pending;
    private String highlight = "";
    private Map<String, Integer> counts = Map.of();

    private int page;
    private int selected;

    // ---- 概述滚动 ----
    private static final int LINE_H = 10;
    private int analysisScroll;
    private int analysisTop;
    private int analysisMaxLines = 1;
    private int analysisMaxScroll;
    private List<FormattedCharSequence> analysisCache = List.of();
    private String analysisCacheKey = "";

    // ---- 布局 ----
    private int left;
    private int top;
    private int listX;
    private int detailX;
    private int detailW;
    private int contentTop;
    private int rows;
    private int footerY;
    private int bodyBottom;

    public PigPenScreen(PigPenPayload payload) {
        super(Component.translatable("gui.roll_pig.title"));
        apply(payload);
    }

    public void refresh(PigPenPayload payload) {
        apply(payload);
        if (this.rows > 0) {
            clampPage();
        }
    }

    private void apply(PigPenPayload payload) {
        this.canRoll = payload.canRoll();
        this.highlight = payload.highlight();
        this.counts = payload.counts();
        this.pending = false;
        int index = indexOf(this.highlight);
        if (index >= 0) {
            this.selected = index;
        }
    }

    private int indexOf(String pigId) {
        if (pigId == null || pigId.isEmpty()) {
            return -1;
        }
        for (int i = 0; i < this.pigs.size(); i++) {
            if (this.pigs.get(i).id().equals(pigId)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    protected void init() {
        this.left = (this.width - PANEL_W) / 2;
        this.top = Math.max(4, (this.height - PANEL_H) / 2);
        this.listX = this.left + 12;
        this.detailX = this.listX + LIST_W + 16;
        this.detailW = this.left + PANEL_W - 10 - this.detailX;
        this.contentTop = this.top + 46;
        this.footerY = this.top + PANEL_H - 18;
        this.bodyBottom = this.footerY - 8;
        this.rows = Math.max(1, (this.bodyBottom - (this.contentTop + 16)) / ROW_H);
        clampPage();
    }

    private void clampPage() {
        int pages = pageCount();
        this.page = Mth.clamp(this.page, 0, pages - 1);
        if (this.selected >= this.rows * pages) {
            this.selected = Math.max(0, this.rows * pages - 1);
        }
        if (this.selected < 0) {
            this.selected = 0;
        }
        if (indexOf(this.highlight) >= 0 && this.rows > 0) {
            this.page = Mth.clamp(this.selected / this.rows, 0, pages - 1);
        }
    }

    private int pageCount() {
        return Math.max(1, (this.pigs.size() + this.rows - 1) / this.rows);
    }

    private int totalCount() {
        int sum = 0;
        for (int value : this.counts.values()) {
            sum += value;
        }
        return sum;
    }

    private String pigName(String pigId) {
        for (Pig pig : this.pigs) {
            if (pig.id().equals(pigId)) {
                return pig.name();
            }
        }
        return pigId;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fill(0, 0, this.width, this.height, DIM);
        drawFrame(guiGraphics);
        drawHeader(guiGraphics, mouseX, mouseY);
        drawList(guiGraphics, mouseX, mouseY);
        drawDetail(guiGraphics, mouseX, mouseY);
        drawFooter(guiGraphics, mouseX, mouseY);
    }

    private void drawFrame(GuiGraphics guiGraphics) {
        guiGraphics.fill(this.left - 2, this.top - 2, this.left + PANEL_W + 2, this.top + PANEL_H + 2, 0x50000000);
        guiGraphics.fill(this.left - 1, this.top - 1, this.left + PANEL_W + 1, this.top + PANEL_H + 1, BORDER);
        guiGraphics.fill(this.left, this.top, this.left + PANEL_W, this.top + PANEL_H, PANEL);
        guiGraphics.fill(this.left + 1, this.top + 1, this.left + PANEL_W - 1, this.top + 2, HILIGHT);
        guiGraphics.fill(this.left + 1, this.top + 26, this.left + PANEL_W - 1, this.top + 27, BORDER_SOFT);
        guiGraphics.fill(this.left + 1, this.footerY - 6, this.left + PANEL_W - 1, this.footerY - 5, BORDER_SOFT);
        int dividerX = this.listX + LIST_W + 8;
        guiGraphics.fill(dividerX, this.contentTop, dividerX + 1, this.bodyBottom, BORDER_SOFT);
    }

    private void drawHeader(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // 左上角红色小标识
        guiGraphics.fill(this.left + 10, this.top + 9, this.left + 18, this.top + 17, ACCENT);
        guiGraphics.fill(this.left + 10, this.top + 9, this.left + 18, this.top + 10, 0x66FFFFFF);
        guiGraphics.fill(this.left + 11, this.top + 11, this.left + 13, this.top + 13, 0x55000000);

        text(guiGraphics, Component.translatable("gui.roll_pig.title"), this.left + 23, this.top + 9, TEXT);

        Component total = Component.translatable("gui.roll_pig.total", totalCount());
        text(guiGraphics, total, this.left + PANEL_W / 2 - this.font.width(total) / 2, this.top + 9, GOLD);

        // 关闭按钮
        int closeX = this.left + PANEL_W - 22;
        int closeY = this.top + 5;
        boolean hoverClose = inside(mouseX, mouseY, closeX, closeY, 14, 14);
        guiGraphics.fill(closeX, closeY, closeX + 14, closeY + 14, hoverClose ? 0x30FFFFFF : 0x18FFFFFF);
        text(guiGraphics, Component.literal("x"), closeX + 5, closeY + 3, hoverClose ? TEXT : MUTED);

        // 副标题
        Component subtitle;
        int subtitleColor;
        if (!this.highlight.isEmpty()) {
            subtitle = Component.translatable("gui.roll_pig.subtitle_result", pigName(this.highlight));
            subtitleColor = GOLD;
        } else {
            subtitle = Component.translatable(this.canRoll
                    ? "gui.roll_pig.subtitle"
                    : "gui.roll_pig.subtitle_used");
            subtitleColor = MUTED;
        }
        text(guiGraphics, subtitle, this.left + PANEL_W / 2 - this.font.width(subtitle) / 2, this.top + 29, subtitleColor);
    }

    private void drawList(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        text(guiGraphics, Component.translatable("gui.roll_pig.list"), this.listX, this.contentTop + 2, MUTED);
        guiGraphics.fill(this.listX, this.contentTop + 13, this.listX + LIST_W, this.contentTop + 14, BORDER_SOFT);

        if (this.pigs.isEmpty()) {
            text(guiGraphics, Component.translatable("gui.roll_pig.empty"), this.listX, this.contentTop + 22, MUTED);
            return;
        }

        int start = this.page * this.rows;
        for (int i = 0; i < this.rows; i++) {
            int index = start + i;
            if (index >= this.pigs.size()) {
                break;
            }
            int rowY = this.contentTop + 16 + i * ROW_H;
            int rowH = ROW_H - 4;
            boolean chosen = index == this.selected;
            boolean hovered = inside(mouseX, mouseY, this.listX, rowY, LIST_W, rowH);

            if (chosen) {
                guiGraphics.fill(this.listX, rowY, this.listX + LIST_W, rowY + rowH, ROW_SELECTED);
                guiGraphics.fill(this.listX, rowY, this.listX + 2, rowY + rowH, ACCENT);
            } else if (hovered) {
                guiGraphics.fill(this.listX, rowY, this.listX + LIST_W, rowY + rowH, ROW_HOVER);
            }

            Pig pig = this.pigs.get(index);
            drawPig(guiGraphics, pig.id(), this.listX + 5, rowY + 4, AVATAR);

            String name = fit(pig.name(), LIST_W - 5 - AVATAR - 4 - 26);
            text(guiGraphics, Component.literal(name), this.listX + 25, rowY + 7, chosen ? TEXT : MUTED);

            Component count = Component.literal("x" + this.counts.getOrDefault(pig.id(), 0));
            text(guiGraphics, count, this.listX + LIST_W - 4 - this.font.width(count), rowY + 7,
                    chosen ? GOLD : MUTED);
        }
    }

    private void drawDetail(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        text(guiGraphics, Component.translatable("gui.roll_pig.detail"), this.detailX, this.contentTop + 2, MUTED);
        guiGraphics.fill(this.detailX, this.contentTop + 13, this.detailX + this.detailW, this.contentTop + 14, BORDER_SOFT);

        if (this.pigs.isEmpty()) {
            text(guiGraphics, Component.translatable("gui.roll_pig.empty"), this.detailX, this.contentTop + 22, MUTED);
            return;
        }

        drawRollButton(guiGraphics, mouseX, mouseY);

        int index = Mth.clamp(this.selected, 0, this.pigs.size() - 1);
        Pig pig = this.pigs.get(index);

        int portraitX = this.detailX;
        int portraitY = this.contentTop + 20;
        guiGraphics.fill(portraitX - 1, portraitY - 1, portraitX + PORTRAIT + 1, portraitY + PORTRAIT + 1, BORDER_SOFT);
        drawPig(guiGraphics, pig.id(), portraitX, portraitY, PORTRAIT);

        int textX = portraitX + PORTRAIT + 8;
        int textWidth = Math.max(20, this.detailX + this.detailW - textX);
        text(guiGraphics, Component.literal(fit(pig.name(), textWidth)), textX, portraitY + 2, TEXT);
        text(guiGraphics, Component.literal(fit(pig.description(), textWidth)), textX, portraitY + 15, MUTED);

        Component count = Component.translatable("gui.roll_pig.count", this.counts.getOrDefault(pig.id(), 0));
        text(guiGraphics, count, textX, portraitY + 28, MUTED);

        int wrapTop = portraitY + PORTRAIT + 10;
        int wrapHeight = Math.max(LINE_H, this.bodyBottom - 22 - wrapTop);
        int maxLines = Math.max(1, wrapHeight / LINE_H);
        List<FormattedCharSequence> lines = analysisLines(pig);

        this.analysisTop = wrapTop;
        this.analysisMaxLines = maxLines;
        this.analysisMaxScroll = Math.max(0, lines.size() - maxLines);
        this.analysisScroll = Mth.clamp(this.analysisScroll, 0, this.analysisMaxScroll);

        int viewBottom = wrapTop + maxLines * LINE_H;
        guiGraphics.enableScissor(this.detailX, wrapTop, this.detailX + this.detailW, viewBottom);
        for (int i = 0; i < maxLines; i++) {
            int lineIndex = this.analysisScroll + i;
            if (lineIndex >= lines.size()) {
                break;
            }
            guiGraphics.drawString(this.font, lines.get(lineIndex), this.detailX, wrapTop + i * LINE_H, TEXT, false);
        }
        guiGraphics.disableScissor();

        if (this.analysisMaxScroll > 0) {
            drawScrollbar(guiGraphics, wrapTop, maxLines * LINE_H);
        }
    }

    private List<FormattedCharSequence> analysisLines(Pig pig) {
        String key = pig.id() + "#" + this.detailW;
        if (!key.equals(this.analysisCacheKey)) {
            this.analysisCacheKey = key;
            this.analysisCache = this.font.split(Component.literal(pig.analysis()), this.detailW - 4);
            this.analysisScroll = 0;
        }
        return this.analysisCache;
    }

    private void drawScrollbar(GuiGraphics guiGraphics, int top, int height) {
        int x = this.detailX + this.detailW - 2;
        guiGraphics.fill(x, top, x + 2, top + height, 0x30FFFFFF);
        int total = this.analysisMaxLines + this.analysisMaxScroll;
        int thumb = Math.max(8, height * this.analysisMaxLines / Math.max(1, total));
        int travel = height - thumb;
        int offset = this.analysisMaxScroll == 0 ? 0 : travel * this.analysisScroll / this.analysisMaxScroll;
        guiGraphics.fill(x, top + offset, x + 2, top + offset + thumb, MUTED);
    }

    private void playClick() {
        Minecraft.getInstance().getSoundManager()
                .play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    private int rollButtonWidth() {
        return this.font.width(Component.translatable("gui.roll_pig.roll")) + 16;
    }

    private int rollButtonX() {
        return this.detailX + this.detailW - rollButtonWidth();
    }

    private int rollButtonY() {
        return this.bodyBottom - BUTTON_H - 4;
    }

    private void drawRollButton(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        boolean enabled = this.canRoll && !this.pending;
        int width = rollButtonWidth();
        int x = rollButtonX();
        int y = rollButtonY();
        boolean hovered = enabled && inside(mouseX, mouseY, x, y, width, BUTTON_H);

        guiGraphics.fill(x, y, x + width, y + BUTTON_H, !enabled ? BUTTON_BG_OFF : (hovered ? BUTTON_BG_HOVER : BUTTON_BG));
        guiGraphics.fill(x, y, x + width, y + 1, enabled ? ACCENT : 0xFF463A40);

        Component label = Component.translatable(enabled ? "gui.roll_pig.roll" : "gui.roll_pig.roll_used");
        text(guiGraphics, label, x + (width - this.font.width(label)) / 2, y + 4, enabled ? TEXT : MUTED);
    }

    private void drawFooter(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int pages = pageCount();
        Component label = Component.translatable("gui.roll_pig.page", this.page + 1, pages);
        text(guiGraphics, label, this.left + PANEL_W / 2 - this.font.width(label) / 2, this.footerY + 1, MUTED);

        if (pages <= 1) {
            return;
        }
        int prevX = prevArrowX();
        int nextX = nextArrowX();
        int y = this.footerY - 2;
        boolean hoverPrev = this.page > 0 && inside(mouseX, mouseY, prevX, y, 12, 12);
        boolean hoverNext = this.page < pages - 1 && inside(mouseX, mouseY, nextX, y, 12, 12);
        text(guiGraphics, Component.literal("<"), prevX + 2, y + 2, this.page > 0 ? (hoverPrev ? TEXT : MUTED) : 0xFF3C414D);
        text(guiGraphics, Component.literal(">"), nextX + 2, y + 2, this.page < pages - 1 ? (hoverNext ? TEXT : MUTED) : 0xFF3C414D);
    }

    private int prevArrowX() {
        Component label = Component.translatable("gui.roll_pig.page", this.page + 1, pageCount());
        return this.left + PANEL_W / 2 - this.font.width(label) / 2 - 14;
    }

    private int nextArrowX() {
        Component label = Component.translatable("gui.roll_pig.page", this.page + 1, pageCount());
        return this.left + PANEL_W / 2 + this.font.width(label) / 2 + 4;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            if (inside(mouseX, mouseY, this.left + PANEL_W - 22, this.top + 5, 14, 14)) {
                playClick();
                onClose();
                return true;
            }
            int row = rowAt(mouseX, mouseY);
            if (row >= 0) {
                this.selected = this.page * this.rows + row;
                this.analysisScroll = 0;
                return true;
            }
            if (this.canRoll && !this.pending
                    && inside(mouseX, mouseY, rollButtonX(), rollButtonY(), rollButtonWidth(), BUTTON_H)) {
                this.pending = true;
                // 兜底：万一服务端没回包（例如这一天的次数刚好用光），别让按钮永远卡在禁用状态。
                RPClientTicker.schedule(40, () -> this.pending = false);
                RPNetwork.requestRoll();
                return true;
            }
            if (this.page > 0 && inside(mouseX, mouseY, prevArrowX(), this.footerY - 2, 12, 12)) {
                this.page--;
                this.analysisScroll = 0;
                return true;
            }
            if (this.page < pageCount() - 1 && inside(mouseX, mouseY, nextArrowX(), this.footerY - 2, 12, 12)) {
                this.page++;
                this.analysisScroll = 0;
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (scrollY == 0.0D) {
            return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }
        // 光标在详情文字上：滚动概述
        if (this.analysisMaxScroll > 0
                && mouseX >= this.detailX && mouseX <= this.detailX + this.detailW
                && mouseY >= this.analysisTop && mouseY <= this.bodyBottom) {
            int delta = scrollY > 0.0D ? -1 : 1;
            this.analysisScroll = Mth.clamp(this.analysisScroll + delta, 0, this.analysisMaxScroll);
            return true;
        }
        // 光标在面板里：翻页
        if (mouseX >= this.left && mouseX <= this.left + PANEL_W
                && mouseY >= this.contentTop && mouseY <= this.bodyBottom) {
            if (scrollY > 0.0D) {
                this.page = Math.max(0, this.page - 1);
            } else {
                this.page = Math.min(pageCount() - 1, this.page + 1);
            }
            this.analysisScroll = 0;
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    private int rowAt(double mouseX, double mouseY) {
        for (int i = 0; i < this.rows; i++) {
            int index = this.page * this.rows + i;
            if (index >= this.pigs.size()) {
                break;
            }
            int rowY = this.contentTop + 16 + i * ROW_H;
            if (inside(mouseX, mouseY, this.listX, rowY, LIST_W, ROW_H - 4)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void text(GuiGraphics guiGraphics, Component text, int x, int y, int color) {
        guiGraphics.drawString(this.font, text, x, y, color, false);
    }

    private void drawPig(GuiGraphics guiGraphics, String pigId, int x, int y, int size) {
        PigTextures.Sprite sprite = PigTextures.get(pigId);
        if (sprite == null) {
            guiGraphics.fill(x, y, x + size, y + size, 0xFF2A2F3A);
            Component mark = Component.literal("?");
            text(guiGraphics, mark, x + size / 2 - this.font.width(mark) / 2, y + size / 2 - 4, MUTED);
            return;
        }
        // 纹理始终是素材原生尺寸，这里只决定显示多大
        sprite.draw(guiGraphics, x, y, size);
    }

    private String fit(String value, int maxWidth) {
        if (value == null) {
            return "";
        }
        if (maxWidth <= 0 || this.font.width(value) <= maxWidth) {
            return value;
        }
        String ellipsis = "...";
        int room = maxWidth - this.font.width(ellipsis);
        if (room <= 0) {
            return ellipsis;
        }
        return this.font.plainSubstrByWidth(value, room) + ellipsis;
    }

    private static boolean inside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }
}
