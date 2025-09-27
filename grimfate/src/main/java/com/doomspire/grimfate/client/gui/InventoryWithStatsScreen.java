package com.doomspire.grimfate.client.gui;

import com.doomspire.grimcore.attach.PlayerStatsAttachment;
import com.doomspire.grimcore.stat.Attributes;
import com.doomspire.grimcore.stat.ModAttachments;
import com.doomspire.grimcore.stat.PlayerProgress;
import com.doomspire.grimfate.core.Grimfate;
import com.doomspire.grimfate.network.ModNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

public class InventoryWithStatsScreen extends InventoryScreen {

    // === Твой фон панели 160×224 ===
    private static final int PANEL_W = 160;
    private static final int PANEL_H = 224;
    // Насколько уезжает инвентарь влево, чтобы справа поместилась панель
    private static final int INV_SHIFT = 120;

    private static final ResourceLocation PANEL_BG =
            ResourceLocation.fromNamespaceAndPath(Grimfate.MODID, "textures/gui/stats/stats_panel_bg.png");

    // Иконки характеристик-атласа (подключатся, когда положишь PNG)
    private static final ResourceLocation ICONS_TEX =
            ResourceLocation.fromNamespaceAndPath(Grimfate.MODID, "textures/gui/stats/stats_icons.png");

    // Панель прижата к правому краю инвентаря
    private int panelX, panelY;

    // Прямоугольные кликабельные зоны «+»
    private final List<AttrBtn> attrButtons = new ArrayList<>();

    public InventoryWithStatsScreen(Player player) {
        super(player);
    }

    @Override
    protected void init() {
        super.init();

        // Сдвигаем инвентарь влево, чтобы справа было место под панель
        this.leftPos -= INV_SHIFT;

        // Инициализируем прямоугольники «+» по макету (координаты внутри панели)
        attrButtons.clear();
        // СТК
        attrButtons.add(makeAttrBtn(Attributes.VITALITY,     34, 22, 54, 22));
        // СИЛ
        attrButtons.add(makeAttrBtn(Attributes.STRENGTH,     34, 35, 54, 35));
        // ДУХ (SPIRIT)
        attrButtons.add(makeAttrBtn(Attributes.SPIRIT,       69, 22, 89, 22));
        // ИНТ
        attrButtons.add(makeAttrBtn(Attributes.INTELLIGENCE, 69, 35, 89, 35));
        // ЛВК
        attrButtons.add(makeAttrBtn(Attributes.DEXTERITY,   105, 22,125, 22));
        // УКЛ
        attrButtons.add(makeAttrBtn(Attributes.EVASION,     105, 35,125, 35));
    }

    private AttrBtn makeAttrBtn(Attributes id, int labelX, int labelY, int valueX, int valueY) {
        // ширина: от (labelX - 2) до valueX, высота 10, по Y прямо под надписью (labelY + 10)
        int x = labelX - 2;
        int y = labelY + 10;
        int w = valueX - x;
        int h = 10;
        return new AttrBtn(id, x, y, w, h);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float pt) {
        // ВАЖНО: каждый кадр привязываем панель к актуальному положению инвентаря
        panelX = this.leftPos + this.imageWidth + 2;
        panelY = this.topPos;

        // фон экрана
        this.renderBackground(g, mouseX, mouseY, pt);

        // отрисовка инвентаря
        super.render(g, mouseX, mouseY, pt);

        // фон панели 160×224
        g.blit(PANEL_BG, panelX, panelY, 0, 0, PANEL_W, PANEL_H, PANEL_W, PANEL_H);

        // данные игрока
        var p = this.minecraft.player;
        var progressAtt = p.getData(ModAttachments.PLAYER_PROGRESS.get());
        var statsAtt    = p.getData(ModAttachments.PLAYER_STATS.get());

        PlayerProgress prog = progressAtt != null ? progressAtt.toSnapshot() : PlayerProgress.DEFAULT;
        int unspent = statsAtt != null ? statsAtt.getUnspentPoints() : 0;

        // === Шапка (твои координаты) ===
        // "Уровень: X" (мы выводим "Уровень: N" одной строкой)
        String lvlText = "Уровень: " + prog.level();
        g.drawString(this.font, lvlText, panelX + 49, panelY + 7, 0xFFE6DDAA, false);

        // Нераспределённые очки — только число
        g.drawString(this.font, String.valueOf(unspent), panelX + 107, panelY + 7, 0xFFFFFF, false);

        // === Атрибуты (аббревиатуры и значения) ===
        // порядок: СТК, СИЛ, ДУХ, ИНТ, ЛВК, УКЛ
        AttrLine[] lines = new AttrLine[] {
                new AttrLine("СТК", Attributes.VITALITY,     34, 22, 54, 22),
                new AttrLine("СИЛ", Attributes.STRENGTH,     34, 35, 54, 35),
                new AttrLine("ДУХ", Attributes.SPIRIT,       69, 22, 89, 22),
                new AttrLine("ИНТ", Attributes.INTELLIGENCE, 69, 35, 89, 35),
                new AttrLine("ЛВК", Attributes.DEXTERITY,   105, 22,125, 22),
                new AttrLine("УКЛ", Attributes.EVASION,     105, 35,125, 35)
        };
        for (AttrLine L : lines) {
            g.drawString(this.font, L.label, panelX + L.labelX, panelY + L.labelY, 0xFFFFFF, false);
            int val = (statsAtt != null) ? statsAtt.getAttribute(L.id) : 0;
            g.drawString(this.font, String.valueOf(val), panelX + L.valueX, panelY + L.valueY, 0xFFFFFF, false);
        }

        // === Кнопки «+» под атрибутами (прямоугольники) ===
        for (AttrBtn b : attrButtons) {
            int ax = panelX + b.x;
            int ay = panelY + b.y;
            boolean hover = mouseX >= ax && mouseX < ax + b.w && mouseY >= ay && mouseY < ay + b.h;
            int bg = hover ? 0x66FFFFFF : 0x33FFFFFF; // подсветка на hover
            g.fill(ax, ay, ax + b.w, ay + b.h, (bg & 0x00FFFFFF) | 0x33000000);
            // тень верх-низ для псевдокнопки
            g.fill(ax, ay, ax + b.w, ay + 1, 0x55FFFFFF);
            g.fill(ax, ay + b.h - 1, ax + b.w, ay + b.h, 0x22000000);
            // маленький плюсик по центру
            int px = ax + (b.w - this.font.width("+")) / 2;
            int py = ay + (b.h - this.font.lineHeight) / 2 + 1;
            g.drawString(this.font, "+", px, py, 0xFFFFFFFF, false);
        }

        // === Заголовок "Характеристики" по центру в (80, 55) ===
        String hdr = "Характеристики";
        int hdrX = panelX + 80 - this.font.width(hdr) / 2;
        int hdrY = panelY + 55;
        g.drawString(this.font, hdr, hdrX, hdrY, 0xFFE6DDAA, false);

        // === Три колонки: иконка + значение (числа). Иконки подключу, когда положишь PNG. ===
        // Сетка колонок: левый 10px отступ, равные колонки
        final int col1L = panelX + 10,  col1R = panelX + 56;
        final int col2L = panelX + 60,  col2R = panelX + 106;
        final int col3L = panelX + 110, col3R = panelX + 156;
        final int firstY = panelY + 70;
        final int rowStep = 14;

        if (statsAtt != null) {
            var s = statsAtt.getSnapshot();

            // Колонка 1 (базовые): HP, HP/s, MP, MP/s, Speed, AttackSpeed(=0 пока)
            drawStatIconRow(g, "hp_max",        s.maxHealth,       col1L, col1R, firstY + rowStep * 0, ValueFmt.INT);
            drawStatIconRow(g, "hp_regen",      s.regenHealth,     col1L, col1R, firstY + rowStep * 1, ValueFmt.INT);
            drawStatIconRow(g, "mp_max",        s.maxMana,         col1L, col1R, firstY + rowStep * 2, ValueFmt.INT);
            drawStatIconRow(g, "mp_regen",      s.regenMana,       col1L, col1R, firstY + rowStep * 3, ValueFmt.INT);
            drawStatIconRow(g, "move_speed",    s.moveSpeedPct,    col1L, col1R, firstY + rowStep * 4, ValueFmt.PCT);
            drawStatIconRow(g, "attack_speed",  0.0,               col1L, col1R, firstY + rowStep * 5, ValueFmt.INT);

            // Колонка 2 (резисты): phys, fire, frost, lightning, poison, armor(=0 пока)
            drawStatIconRow(g, "res_phys",      s.resistances.getOrDefault(com.doomspire.grimcore.stat.DamageTypes.PHYS_MELEE, 0f) * 100.0, col2L, col2R, firstY + rowStep * 0, ValueFmt.PCT_RAW);
            drawStatIconRow(g, "res_fire",      s.resistances.getOrDefault(com.doomspire.grimcore.stat.DamageTypes.FIRE, 0f) * 100.0,       col2L, col2R, firstY + rowStep * 1, ValueFmt.PCT_RAW);
            drawStatIconRow(g, "res_frost",     s.resistances.getOrDefault(com.doomspire.grimcore.stat.DamageTypes.FROST, 0f) * 100.0,      col2L, col2R, firstY + rowStep * 2, ValueFmt.PCT_RAW);
            drawStatIconRow(g, "res_lightning", s.resistances.getOrDefault(com.doomspire.grimcore.stat.DamageTypes.LIGHTNING, 0f) * 100.0,  col2L, col2R, firstY + rowStep * 3, ValueFmt.PCT_RAW);
            drawStatIconRow(g, "res_poison",    s.resistances.getOrDefault(com.doomspire.grimcore.stat.DamageTypes.POISON, 0f) * 100.0,     col2L, col2R, firstY + rowStep * 4, ValueFmt.PCT_RAW);
            drawStatIconRow(g, "armor",         0.0,               col2L, col2R, firstY + rowStep * 5, ValueFmt.INT);

            // Колонка 3 (уроны)
            drawStatIconRow(g, "dmg_melee",     s.damage.getOrDefault(com.doomspire.grimcore.stat.DamageTypes.PHYS_MELEE, 0f),  col3L, col3R, firstY + rowStep * 0, ValueFmt.F1);
            drawStatIconRow(g, "dmg_ranged",    s.damage.getOrDefault(com.doomspire.grimcore.stat.DamageTypes.PHYS_RANGED, 0f), col3L, col3R, firstY + rowStep * 1, ValueFmt.F1);
            drawStatIconRow(g, "dmg_fire",      s.damage.getOrDefault(com.doomspire.grimcore.stat.DamageTypes.FIRE, 0f),        col3L, col3R, firstY + rowStep * 2, ValueFmt.F1);
            drawStatIconRow(g, "dmg_frost",     s.damage.getOrDefault(com.doomspire.grimcore.stat.DamageTypes.FROST, 0f),       col3L, col3R, firstY + rowStep * 3, ValueFmt.F1);
            drawStatIconRow(g, "dmg_lightning", s.damage.getOrDefault(com.doomspire.grimcore.stat.DamageTypes.LIGHTNING, 0f),   col3L, col3R, firstY + rowStep * 4, ValueFmt.F1);
            drawStatIconRow(g, "dmg_poison",    s.damage.getOrDefault(com.doomspire.grimcore.stat.DamageTypes.POISON, 0f),      col3L, col3R, firstY + rowStep * 5, ValueFmt.F1);
        }

        // тултипы можно будет добавить по наведению на области иконок/значений
        this.renderTooltip(g, mouseX, mouseY);
    }

    // Рисуем (пока только число; иконку подключу, когда добавишь PNG)
    private void drawStatIconRow(GuiGraphics g, String iconName, double value,
                                 int colL, int colR, int y, ValueFmt fmt) {
        // Иконка (16×16) слева; если текстуры ещё нет — просто пропустим blit
        int iconX = colL;
        int valRight = colR; // правое выравнивание числа
        try {
            // расчёт кадра анимации: кадр вверх-вниз 16 px
            IconDef def = Icons.get(iconName);
            if (def != null) {
                int frame = def.frames > 1 ? (int)((System.currentTimeMillis() / def.frameMs) % def.frames) : 0;
                int u = def.u;
                int v = def.v + frame * def.h;
                g.blit(ICONS_TEX, iconX, y - 3, u, v, def.w, def.h);
            }
        } catch (Throwable ignored) {
            // если текстуры нет — тихо не рисуем
        }

        String txt = switch (fmt) {
            case INT     -> String.valueOf((int)Math.round(value));
            case F1      -> String.format(java.util.Locale.ROOT, "%.1f", value);
            case PCT     -> String.format(java.util.Locale.ROOT, "+%.2f%%", value);
            case PCT_RAW -> String.format(java.util.Locale.ROOT, "%.0f%%", value);
        };
        int vx = valRight - this.font.width(txt);
        g.drawString(this.font, txt, vx, y, 0xFFFFFF, false);
    }

    private enum ValueFmt { INT, F1, PCT, PCT_RAW }

    // === Клики по «плюсам» ===
    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        // клики по прямоугольникам «+»
        for (AttrBtn b : attrButtons) {
            int ax = panelX + b.x;
            int ay = panelY + b.y;
            if (mx >= ax && mx < ax + b.w && my >= ay && my < ay + b.h) {
                ModNetworking.sendAllocatePoint(b.id.name());
                return true;
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    // ===== Вспомогательные структуры =====

    private record AttrLine(String label, Attributes id, int labelX, int labelY, int valueX, int valueY) {}

    private static final class AttrBtn {
        final Attributes id;
        final int x, y, w, h;
        AttrBtn(Attributes id, int x, int y, int w, int h) {
            this.id = id; this.x = x; this.y = y; this.w = w; this.h = h;
        }
    }

    // ——— Иконки (подключатся, когда добавишь stats_icons.png) ———
    private static final class IconDef {
        final int u, v, w, h, frames, frameMs;
        IconDef(int u, int v, int w, int h, int frames, int frameMs) {
            this.u = u; this.v = v; this.w = w; this.h = h; this.frames = frames; this.frameMs = Math.max(frameMs, 1);
        }
    }
    private static final class Icons {
        private static final java.util.Map<String, IconDef> MAP = new java.util.HashMap<>();
        static {
            // колонка 1
            MAP.put("hp_max",       new IconDef(  0, 0,16,16, 2,120));
            MAP.put("hp_regen",     new IconDef( 16, 0,16,16, 6, 60));
            MAP.put("mp_max",       new IconDef( 32, 0,16,16, 8, 60));
            MAP.put("mp_regen",     new IconDef( 48, 0,16,16, 8, 60));
            MAP.put("move_speed",   new IconDef( 64, 0,16,16, 7,120));
            MAP.put("attack_speed", new IconDef( 80, 0,16,16,10, 20));
            // колонка 2
            MAP.put("res_phys",     new IconDef( 96, 0,16,16, 8, 60));
            MAP.put("res_fire",     new IconDef(112, 0,16,16, 6, 60));
            MAP.put("res_frost",    new IconDef(128, 0,16,16, 6, 60));
            MAP.put("res_lightning",new IconDef(144, 0,16,16, 6, 20));
            MAP.put("res_poison",   new IconDef(160, 0,16,16, 6, 60));
            MAP.put("armor",        new IconDef(176, 0,16,16, 1,  0));
            // колонка 3
            MAP.put("dmg_melee",    new IconDef(192, 0,16,16, 1,  0));
            MAP.put("dmg_ranged",   new IconDef(208, 0,16,16, 1,  0));
            MAP.put("dmg_fire",     new IconDef(224, 0,16,16, 1,  0));
            MAP.put("dmg_frost",    new IconDef(240, 0,16,16, 1,  0));
            MAP.put("dmg_lightning",new IconDef(256, 0,16,16, 1,  0));
            MAP.put("dmg_poison",   new IconDef(272, 0,16,16, 1,  0));
        }
        static IconDef get(String key) { return MAP.get(key); }
    }
}
