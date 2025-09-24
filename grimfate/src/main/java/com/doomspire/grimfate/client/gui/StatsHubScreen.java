package com.doomspire.grimfate.client.gui;

import com.doomspire.grimcore.attach.PlayerStatsAttachment;
import com.doomspire.grimcore.stat.Attributes;
import com.doomspire.grimcore.stat.ModAttachments;
import com.doomspire.grimfate.network.ModNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
// import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class StatsHubScreen extends Screen {
    private static final int PAD = 8;
    private static final int ROW_STEP = 18;
    private static final int ROW_HEIGHT = 18;

    private final List<Row> rows = new ArrayList<>();
    private int unspent = 0;

    // Если позже захочешь PNG-фон — раскомментируй:
//    private static final ResourceLocation BG = ResourceLocation.fromNamespaceAndPath("grimfate", "textures/gui/stats_bg.png");

    public StatsHubScreen() {
        super(Component.translatable("screen.grimfate.stats_hub"));
    }

    @Override
    protected void init() {
        rows.clear();
        PlayerStatsAttachment att = getClientAtt();
        if (att == null) return;

        this.unspent = att.getUnspentPoints();

        int y = this.height / 6;
        for (Attributes attr : Attributes.values()) {
            String id = attr.name();
            Component label = Component.translatable("attr.grimfate." + id.toLowerCase());
            int allocated = att.getAttribute(attr);

            Row r = new Row(id, label, allocated, y);
            rows.add(r);

            this.addRenderableWidget(
                    Button.builder(Component.literal("+"), b -> onAddPoint(id))
                            .bounds(this.width / 2 + 60, y, 18, 16)
                            .build()
            );

            y += ROW_STEP;
        }
    }

    private void onAddPoint(String attributeId) {
        if (unspent <= 0) return;
        ModNetworking.sendAllocatePoint(attributeId);
        unspent--;
        for (Row r : rows) {
            if (r.id.equals(attributeId)) { r.allocated++; break; }
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTicks) {
        // 1) Рисуем собственный фон (без блюра)
        // Вариант A: плёнка
        g.fill(0, 0, this.width, this.height, 0xC0000000);

        // Вариант B: картинка (PNG). Просто раскомментируй строки ниже и положи файл в assets/grimfate/textures/gui/stats_bg.png
//        int bgW = 256, bgH = 180; // реальные размеры текстуры
//        int x = (this.width - bgW) / 2;
//        int y = (this.height - bgH) / 2;
//        g.blit(BG, x, y, 0, 0, bgW, bgH, bgW, bgH);

        // 2) Даём кнопкам отрисоваться (они будут чёткими)
        super.render(g, mouseX, mouseY, partialTicks);

        // 3) После этого рисуем весь наш текст — он тоже останется чётким
        Component title = this.title.copy()
                .append("  ")
                .append(Component.translatable("screen.grimfate.unspent", unspent));
        g.drawString(this.font, title, PAD, PAD, 0xFFFFFF, false);

        int xName = this.width / 2 - 120;
        int xVal  = this.width / 2 + 40;

        for (Row r : rows) {
            g.drawString(this.font, r.label, xName, r.y + 4, 0xE0E0E0, false);
            g.drawString(this.font, Component.literal(String.valueOf(r.allocated)), xVal, r.y + 4, 0xA0FFA0, false);

            if (isMouseOverRow(mouseX, mouseY, r)) {
                g.renderTooltip(this.font, Component.translatable("attr.tip." + r.id.toLowerCase()), mouseX, mouseY);
            }
        }
    }

    private boolean isMouseOverRow(int mx, int my, Row r) {
        int x0 = this.width / 2 - 130, y0 = r.y, w = 220, h = ROW_HEIGHT;
        return mx >= x0 && my >= y0 && mx < x0 + w && my < y0 + h;
    }

    private PlayerStatsAttachment getClientAtt() {
        var p = Minecraft.getInstance().player;
        return p != null ? p.getData(ModAttachments.PLAYER_STATS.get()) : null;
    }

    /** Вызывается из S2C-обработчика для мягкого обновления UI. */
    public void applyServerResult(String attrId, int newAllocated, int newUnspent) {
        this.unspent = newUnspent;
        for (Row r : rows) {
            if (r.id.equals(attrId)) { r.allocated = newAllocated; break; }
        }
    }

    private static final class Row {
        final String id;
        final Component label;
        final int y;
        int allocated;
        Row(String id, Component label, int allocated, int y) {
            this.id = id; this.label = label; this.allocated = allocated; this.y = y;
        }
    }
}
