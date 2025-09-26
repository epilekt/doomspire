package com.doomspire.grimfate.client;

import com.doomspire.grimcore.attach.PlayerLoadoutAttachment;
import com.doomspire.grimcore.attach.PlayerProgressAttachment;
import com.doomspire.grimcore.attach.PlayerStatsAttachment;
import com.doomspire.grimcore.stat.ModAttachments;
import com.doomspire.grimcore.stat.PlayerProgress;
import com.doomspire.grimfate.config.ClientConfig;
import com.doomspire.grimfate.core.Grimfate;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = Grimfate.MODID, value = Dist.CLIENT)
public final class CustomHudOverlay {
    private CustomHudOverlay() {}

    // ---- текстуры ----
    private static final ResourceLocation HEALTH_BAR_EMPTY =
            ResourceLocation.fromNamespaceAndPath(Grimfate.MODID, "textures/gui/health_bar_bg.png");
    private static final ResourceLocation HEALTH_BAR_FULL =
            ResourceLocation.fromNamespaceAndPath(Grimfate.MODID, "textures/gui/health_bar_fill.png");

    private static final ResourceLocation MANA_BAR_EMPTY =
            ResourceLocation.fromNamespaceAndPath(Grimfate.MODID, "textures/gui/health_bar_bg.png");
    private static final ResourceLocation MANA_BAR_FULL =
            ResourceLocation.fromNamespaceAndPath(Grimfate.MODID, "textures/gui/mana_bar_fill.png");

    private static final ResourceLocation XP_BG =
            ResourceLocation.fromNamespaceAndPath(Grimfate.MODID, "textures/gui/xp_background.png");
    private static final ResourceLocation XP_FILL =
            ResourceLocation.fromNamespaceAndPath(Grimfate.MODID, "textures/gui/xp_fill.png");

    private static final ResourceLocation SPELL_CELL =
            ResourceLocation.fromNamespaceAndPath(Grimfate.MODID, "textures/gui/spellbar/spell_bar_cell.png");

    // ---- плавные значения ----
    private static final Map<UUID, Float> DISPLAYED_HEALTH = new ConcurrentHashMap<>();
    private static final Map<UUID, Float> DISPLAYED_MANA   = new ConcurrentHashMap<>();
    private static final Map<UUID, Float> DISPLAYED_XP     = new ConcurrentHashMap<>();
    private static final float LERP_ALPHA = 0.20f;

    // ---- хот-бар ----
    private static final int SPELL_CELL_W = 20;
    private static final int SPELL_CELL_H = 20;
    private static final int SPELL_CELL_PAD = 2;

    // ---- какие ванильные слои гасим ----
    private static final Set<ResourceLocation> VANILLA_LAYERS_TO_HIDE = Set.of(
            ResourceLocation.fromNamespaceAndPath("minecraft", "player_health"),
            ResourceLocation.fromNamespaceAndPath("minecraft", "armor_level")
    );

    /** Гасим ванильные слои, чтобы рисовать свои HP/броню/ману/XP. */
    @SubscribeEvent
    public static void onRenderGuiLayerPre(RenderGuiLayerEvent.Pre event) {
        if (VANILLA_LAYERS_TO_HIDE.contains(event.getName())) {
            event.setCanceled(true);
        }
    }

    /** Рисуем наш общий HUD и хот-бар. */
    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) return;

        var player = mc.player;

        // --- статы ---
        PlayerStatsAttachment statsAtt = player.getData(ModAttachments.PLAYER_STATS.get());
        if (statsAtt == null) return;

        var snap = statsAtt.getSnapshot();
        int health    = statsAtt.getCurrentHealth();
        int maxHealth = Math.max(1, (int) snap.maxHealth);
        int mana      = statsAtt.getCurrentMana();
        int maxMana   = Math.max(1, (int) snap.maxMana);

        // --- прогресс ---
        PlayerProgressAttachment progressAtt = player.getData(ModAttachments.PLAYER_PROGRESS.get());
        PlayerProgress progress = (progressAtt != null) ? progressAtt.toSnapshot() : PlayerProgress.DEFAULT;

        GuiGraphics gui = event.getGuiGraphics();
        int sw = mc.getWindow().getGuiScaledWidth();
        int sh = mc.getWindow().getGuiScaledHeight();
        UUID uuid = player.getUUID();

        // наши бары
        renderHealthBar(gui, mc, sw, sh, uuid, health, maxHealth);
        renderManaBar(gui, mc, sw, sh, uuid, mana, maxMana);
        renderXpIcon(gui, mc, sw, sh, uuid, progress);

        // хот-бар спеллов
        renderSpellHotbar(gui, mc, sw, sh);
    }

    // -------------------- отрисовка --------------------

    private static void renderHealthBar(GuiGraphics gui, Minecraft mc, int sw, int sh,
                                        UUID uuid, int health, int maxHealth) {
        final int W = 120, H = 12;

        float disp = DISPLAYED_HEALTH.getOrDefault(uuid, (float) health);
        disp += (health - disp) * LERP_ALPHA;
        disp = Math.max(0, Math.min(disp, maxHealth));
        DISPLAYED_HEALTH.put(uuid, disp);

        int x = sw / 2 + ClientConfig.HEALTH_BAR_X.get();
        int y = sh + ClientConfig.HEALTH_BAR_Y.get();

        gui.blit(HEALTH_BAR_EMPTY, x, y, 0, 0, W, H, W, H);
        int filled = (int) (W * (disp / Math.max(1f, maxHealth)));
        if (filled > 0) gui.blit(HEALTH_BAR_FULL, x, y, 0, 0, filled, H, W, H);

        String text = health + "/" + maxHealth;
        gui.drawString(mc.font, Component.literal(text),
                x + W / 2 - mc.font.width(text) / 2,
                y + (H - mc.font.lineHeight) / 2,
                0xFFFFFF, true);
    }

    private static void renderManaBar(GuiGraphics gui, Minecraft mc, int sw, int sh,
                                      UUID uuid, int mana, int maxMana) {
        final int W = 120, H = 12;

        float disp = DISPLAYED_MANA.getOrDefault(uuid, (float) mana);
        disp += (mana - disp) * LERP_ALPHA;
        disp = Math.max(0, Math.min(disp, maxMana));
        DISPLAYED_MANA.put(uuid, disp);

        int x = sw / 2 + ClientConfig.MANA_BAR_X.get();
        int y = sh + ClientConfig.MANA_BAR_Y.get();

        gui.blit(MANA_BAR_EMPTY, x, y, 0, 0, W, H, W, H);
        int filled = (int) (W * (disp / Math.max(1f, maxMana)));
        if (filled > 0) gui.blit(MANA_BAR_FULL, x, y, 0, 0, filled, H, W, H);

        String text = mana + "/" + maxMana;
        gui.drawString(mc.font, Component.literal(text),
                x + W / 2 - mc.font.width(text) / 2,
                y + (H - mc.font.lineHeight) / 2,
                0xFFFFFF, true);
    }

    private static void renderXpIcon(GuiGraphics gui, Minecraft mc, int sw, int sh,
                                     UUID uuid, PlayerProgress progress) {
        final int W = 32, H = 32;

        int exp = progress.exp();
        int cap = Math.max(1, progress.expCap());

        float disp = DISPLAYED_XP.getOrDefault(uuid, (float) exp);
        disp += (exp - disp) * LERP_ALPHA;
        disp = Math.max(0, Math.min(disp, cap));
        DISPLAYED_XP.put(uuid, disp);

        int x = sw / 2 + ClientConfig.XP_ICON_X.get();
        int y = sh + ClientConfig.XP_ICON_Y.get();

        gui.blit(XP_BG, x, y, 0, 0, W, H, W, H);

        int filledH = (int) (H * (disp / (float) cap));
        if (filledH > 0) {
            gui.blit(XP_FILL, x, y + (H - filledH), 0, H - filledH, W, filledH, W, H);
        }

        String lvl = String.valueOf(progress.level());
        gui.drawString(mc.font, Component.literal(lvl),
                x + W / 2 - mc.font.width(lvl) / 2,
                y + H / 2 - mc.font.lineHeight / 2,
                0xFFFFFF, true);

        String expTxt = exp + "/" + cap;
        gui.drawString(mc.font, Component.literal(expTxt),
                x + W / 2 - mc.font.width(expTxt) / 2,
                y + H + 2,
                0xFFFFFF, false);
    }

    private static void renderSpellHotbar(GuiGraphics gg, Minecraft mc, int sw, int sh) {
        var p = mc.player;
        var loadout = p.getData(ModAttachments.PLAYER_LOADOUT.get());
        if (loadout == null) return;

        final int CELLS = PlayerLoadoutAttachment.SLOTS; // 6
        final int CELL = 20; // 20x20, без отступов

        // 1) ширина полосы
        int totalW = CELLS * CELL;

        // 2) дефолт по X: центр экрана; по Y: ровно между прицелом (sh/2) и ванильным хотбаром (~sh - 22)
        int vanillaHotbarY = sh - 22;
        int midY = (sh / 2 + vanillaHotbarY) / 2;

        int x0 = (sw - totalW) / 2 + ClientConfig.SPELLBAR_X.get();
        int y0 = midY + ClientConfig.SPELLBAR_Y.get();

        // 3) рисуем все 6 ячеек
        for (int i = 0; i < CELLS; i++) {
            int x = x0 + i * CELL;
            int y = y0;

            // фон ячейки (твоя текстура 20x20)
            gg.blit(SPELL_CELL, x, y, 0, 0, CELL, CELL, CELL, CELL);

            var rl = loadout.get(i);

            // если слот пуст — ничего не пишем (ни хоткей, ни кд)
            if (rl == null) continue;

            // кулдаун
            int cd = loadout.getCooldown(i);
            if (cd > 0) {
                // тёмная маска поверх (оставим как заливку; если захочешь — заменим на текстуру маски)
                gg.fill(x + 1, y + 1, x + CELL - 1, y + CELL - 1, 0x80000000);
                String s = String.valueOf(cd / 20);
                gg.drawString(mc.font, s, x + (CELL - mc.font.width(s)) / 2, y + 5, 0xFFFFFFFF, false);
            }

            // хоткей (буква/цифра) — только если слот не пуст
            String hk = Hotkeys.spellKeyName(i);
            // рисуем в правом нижнем углу, вписываемся в ~8x9px
            int hkX = x + CELL - mc.font.width(hk) - 2;
            int hkY = y + CELL - 9;
            gg.drawString(mc.font, hk, hkX, hkY, 0xFFE6DDAA, false);
        }
    }
}
