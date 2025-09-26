package com.doomspire.grimfate.client.gui;

import com.doomspire.grimcore.attach.PlayerStatsAttachment;
import com.doomspire.grimcore.stat.Attributes;
import com.doomspire.grimcore.stat.ModAttachments;
import com.doomspire.grimcore.stat.PlayerProgress;
import com.doomspire.grimfate.core.Grimfate;
import com.doomspire.grimfate.network.ModNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class InventoryWithStatsScreen extends InventoryScreen {
    private static final int PANEL_W = 240;
    private static final int PANEL_H = 360;   // без скролла — делаем выше
    private static final int INV_SHIFT = 140; // инвентарь уезжает влево не полностью

    private static final ResourceLocation PANEL_BG =
            ResourceLocation.fromNamespaceAndPath(Grimfate.MODID, "textures/gui/stats/stats_panel_bg.png");
    private static final ResourceLocation CLOSE_TEX =
            ResourceLocation.fromNamespaceAndPath(Grimfate.MODID, "textures/gui/stats/close_btn.png");

    private int panelX, panelY;
    private int closeX, closeY;

    private boolean isMouseDown = false;

    // «+» кнопки для атрибутов
    private java.util.List<PlusBtn> plusButtons = java.util.Collections.emptyList();

    public InventoryWithStatsScreen(Player player) { super(player); }

    @Override
    protected void init() {
        super.init();

        // сдвигаем инвентарь влево
        this.leftPos -= INV_SHIFT;

        // панель прижата к правому краю инвентаря
        panelX = this.leftPos + this.imageWidth;
        panelY = this.topPos;

        // закрыть (12×12)
        closeX = panelX + PANEL_W - 12 - 4;
        closeY = panelY + 4;

        // строки атрибутов: аббревиатуры + «+»
        plusButtons = new java.util.ArrayList<>();
        int y = panelY + 30;
        int xPlus = panelX + PANEL_W - 10 - 12; // 12px ширина «+»

        AttrRow[] rows = new AttrRow[] {
                new AttrRow(Attributes.STRENGTH,     "СИЛ"),
                new AttrRow(Attributes.VITALITY,     "СТК"),
                new AttrRow(Attributes.INTELLIGENCE, "ИНТ"),
                new AttrRow(Attributes.SPIRIT,       "ДУХ"),
                new AttrRow(Attributes.DEXTERITY,    "ЛВК"),
                new AttrRow(Attributes.EVASION,      "УКЛ")
        };
        for (AttrRow r : rows) {
            PlusBtn btn = new PlusBtn(xPlus, y, () -> ModNetworking.sendAllocatePoint(r.id.name()));
            this.addRenderableWidget(btn);
            plusButtons.add(btn);
            y += (this.font.lineHeight + 6);
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float pt) {
        // фон
        this.renderBackground(g, mouseX, mouseY, pt);

        // инвентарь (уже сдвинут)
        super.render(g, mouseX, mouseY, pt);

        // фон панели (240×360)
        g.blit(PANEL_BG, panelX, panelY, 0, 0, PANEL_W, PANEL_H, PANEL_W, PANEL_H);

        // закрыть
        boolean overClose = mouseX >= closeX && mouseX <= closeX + 12 && mouseY >= closeY && mouseY <= closeY + 12;
        int v = overClose ? (isMouseDown ? 24 : 12) : 0;
        g.blit(CLOSE_TEX, closeX, closeY, 0, v, 12, 12, 12, 36);

        // данные игрока
        var p = this.minecraft.player;
        var progressAtt = p.getData(ModAttachments.PLAYER_PROGRESS.get());
        var statsAtt    = p.getData(ModAttachments.PLAYER_STATS.get());

        PlayerProgress prog = progressAtt != null ? progressAtt.toSnapshot() : PlayerProgress.DEFAULT;
        int unspent = statsAtt != null ? statsAtt.getUnspentPoints() : 0;

        // шапка
        g.drawString(this.font, "Lv." + prog.level(), panelX + 10, panelY + 10, 0xFFE6DDAA, false);
        g.drawString(this.font, "Unspent: " + unspent, panelX + 110, panelY + 10, 0xFFFFFF, false);

        // атрибуты (аббревиатуры слева, значения справа; «+» уже нарисованы как виджеты)
        int xLabel = panelX + 10;
        int xValue = panelX + PANEL_W - 10 - 12 - 6 - 24; // справа от значений остаётся место под «+»
        int y = panelY + 30;

        if (statsAtt != null) {
            // порядок в точности как у кнопок
            AttrRow[] rows = new AttrRow[] {
                    new AttrRow(Attributes.STRENGTH,     "СИЛ"),
                    new AttrRow(Attributes.VITALITY,     "СТК"),
                    new AttrRow(Attributes.INTELLIGENCE, "ИНТ"),
                    new AttrRow(Attributes.SPIRIT,       "ДУХ"),
                    new AttrRow(Attributes.DEXTERITY,    "ЛВК"),
                    new AttrRow(Attributes.EVASION,      "УКЛ")
            };
            for (AttrRow r : rows) {
                int val = statsAtt.getAttribute(r.id);
                g.drawString(this.font, r.label, xLabel, y, 0xFFFFFF, false);
                g.drawString(this.font, String.valueOf(val), xValue, y, 0xFFFFFF, false);
                y += (this.font.lineHeight + 6);
            }
        } else {
            g.drawString(this.font, "No stats data", xLabel, y, 0xFFFFFF, false);
            y += (this.font.lineHeight + 6);
        }

        // разделитель
        y += 4;
        g.fill(panelX + 10, y, panelX + PANEL_W - 10, y + 1, 0x44FFFFFF);
        y += 6;

        // Характеристики (готовые значения из snapshot)
        g.drawString(this.font, "Характеристики", xLabel, y, 0xFFE6DDAA, false);
        y += (this.font.lineHeight + 4);

        if (statsAtt != null) {
            var s = statsAtt.getSnapshot();

            y = statLine(g, xLabel, y, "Max HP", String.valueOf((int) s.maxHealth));
            y = statLine(g, xLabel, y, "Max MP", String.valueOf((int) s.maxMana));
            y = statLine(g, xLabel, y, "Regen HP/s", String.valueOf((int) s.regenHealth));
            y = statLine(g, xLabel, y, "Regen MP/s", String.valueOf((int) s.regenMana));
            y = statLine(g, xLabel, y, "Speed", String.format("+%.2f%%", s.moveSpeedPct)); // <— НОВОЕ

            // резисты
            for (var e : s.resistances.entrySet()) {
                y = statLine(g, xLabel, y, "Res " + e.getKey().name(), Math.round(e.getValue() * 100) + "%");
                if (y > panelY + PANEL_H - 20) break;
            }
            // урон
            for (var e : s.damage.entrySet()) {
                y = statLine(g, xLabel, y, "Dmg " + e.getKey().name(), String.format("%.1f", e.getValue()));
                if (y > panelY + PANEL_H - 20) break;
            }
        }

        this.renderTooltip(g, mouseX, mouseY);
    }

    private int statLine(GuiGraphics g, int x, int y, String label, String value) {
        g.drawString(this.font, label, x, y, 0xFFFFFF, false);
        int vx = panelX + PANEL_W - 10 - this.font.width(value);
        g.drawString(this.font, value, vx, y, 0xFFFFFF, false);
        return y + this.font.lineHeight + 2;
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        isMouseDown = true;
        if (mx >= closeX && mx <= closeX + 12 && my >= closeY && my <= closeY + 12) {
            this.minecraft.setScreen(new InventoryScreen(this.minecraft.player));
            return true;
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        isMouseDown = false;
        return super.mouseReleased(mx, my, button);
    }

    private record AttrRow(Attributes id, String label) {}

    /** маленькая квадратная кнопка «+» 12×12 */
    private static final class PlusBtn extends AbstractButton {
        private final Runnable onPress;

        PlusBtn(int x, int y, Runnable onPress) {
            super(x, y, 12, 12, Component.empty());
            this.onPress = onPress;
        }

        @Override
        protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float pt) {
            int col = this.isHovered() ? 0xAAFFFFFF : 0x66FFFFFF;
            // полупрозрачный фон
            g.fill(getX(), getY(), getX() + 12, getY() + 12, 0x33000000 | (col & 0x00FFFFFF));
            // плюсик
            g.drawString(Minecraft.getInstance().font, "+", getX() + 3, getY() + 1, 0xFFFFFFFF, false);
        }

        @Override
        public void onPress() {
            if (onPress != null) onPress.run();
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narration) {
            this.defaultButtonNarrationText(narration);
        }
    }
}
