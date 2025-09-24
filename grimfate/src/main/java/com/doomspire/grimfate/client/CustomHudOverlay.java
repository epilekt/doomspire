package com.doomspire.grimfate.client;

import com.doomspire.grimcore.attach.PlayerLoadoutAttachment;
import com.doomspire.grimcore.attach.PlayerStatsAttachment;
import com.doomspire.grimcore.stat.PlayerProgress;
import com.doomspire.grimcore.attach.PlayerProgressAttachment;
import com.doomspire.grimcore.stat.ModAttachments;
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

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = Grimfate.MODID, value = Dist.CLIENT) // FORGE bus
public class CustomHudOverlay {

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

    private static final Map<UUID, Float> DISPLAYED_HEALTH = new ConcurrentHashMap<>();
    private static final Map<UUID, Float> DISPLAYED_MANA   = new ConcurrentHashMap<>();
    private static final Map<UUID, Float> DISPLAYED_XP     = new ConcurrentHashMap<>();
    private static final float LERP_ALPHA = 0.20f;

    // --- хот-бар спеллов (визуальные размеры) ---
    private static final int SPELL_CELL_W = 20;
    private static final int SPELL_CELL_H = 20;
    private static final int SPELL_CELL_PAD = 2;

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) return;

        var player = mc.player;

        // --- статы игрока ---
        PlayerStatsAttachment statsAtt = player.getData(ModAttachments.PLAYER_STATS.get());
        if (statsAtt == null) return; // без статов — наш HUD не нужен

        var snap = statsAtt.getSnapshot();
        int health    = statsAtt.getCurrentHealth();
        int maxHealth = Math.max(1, (int) snap.maxHealth);
        int mana      = statsAtt.getCurrentMana();
        int maxMana   = Math.max(1, (int) snap.maxMana);

        // --- прогресс игрока ---
        PlayerProgressAttachment progressAtt = player.getData(ModAttachments.PLAYER_PROGRESS.get());
        PlayerProgress progress = (progressAtt != null) ? progressAtt.toSnapshot() : PlayerProgress.DEFAULT;

        GuiGraphics gui = event.getGuiGraphics();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        UUID uuid = player.getUUID();

        // Панель HP/MP/XP — как было
        renderHealthBar(gui, mc, screenWidth, screenHeight, uuid, health, maxHealth);
        renderManaBar(gui, mc, screenWidth, screenHeight, uuid, mana, maxMana);
        renderXpIcon(gui, mc, screenWidth, screenHeight, uuid, progress);

        // Хот-бар спеллов — по центру внизу, только если есть хотя бы один спелл
        renderSpellHotbar(gui, mc, screenWidth, screenHeight);
    }

    private static void renderHealthBar(GuiGraphics gui, Minecraft mc, int screenWidth, int screenHeight,
                                        UUID uuid, int health, int maxHealth) {
        final int textureWidth = 120;
        final int textureHeight = 12;

        float displayedHealth = DISPLAYED_HEALTH.getOrDefault(uuid, (float) health);
        displayedHealth += ((float) health - displayedHealth) * LERP_ALPHA;
        displayedHealth = Math.max(0, Math.min(displayedHealth, maxHealth));
        DISPLAYED_HEALTH.put(uuid, displayedHealth);

        float healthPercent = displayedHealth / (float) maxHealth;

        // Позиции — как у тебя: от центра/низа с клиентскими смещениями
        int xH = screenWidth / 2 + ClientConfig.HEALTH_BAR_X.get();
        int yH = screenHeight + ClientConfig.HEALTH_BAR_Y.get();

        gui.blit(HEALTH_BAR_EMPTY, xH, yH, 0, 0, textureWidth, textureHeight, textureWidth, textureHeight);

        int filledHealth = (int) (textureWidth * healthPercent);
        if (filledHealth > 0) {
            gui.blit(HEALTH_BAR_FULL, xH, yH, 0, 0, filledHealth, textureHeight, textureWidth, textureHeight);
        }

        String healthText = health + "/" + maxHealth;
        int textX = xH + textureWidth / 2 - mc.font.width(healthText) / 2;
        int textY = yH + (textureHeight - mc.font.lineHeight) / 2;
        gui.drawString(mc.font, Component.literal(healthText), textX, textY, 0xFFFFFF, true);
    }

    private static void renderManaBar(GuiGraphics gui, Minecraft mc, int screenWidth, int screenHeight,
                                      UUID uuid, int mana, int maxMana) {
        final int textureWidth = 120;
        final int textureHeight = 12;

        float displayedMana = DISPLAYED_MANA.getOrDefault(uuid, (float) mana);
        displayedMana += ((float) mana - displayedMana) * LERP_ALPHA;
        displayedMana = Math.max(0, Math.min(displayedMana, maxMana));
        DISPLAYED_MANA.put(uuid, displayedMana);

        float manaPercent = displayedMana / (float) maxMana;

        int xM = screenWidth / 2 + ClientConfig.MANA_BAR_X.get();
        int yM = screenHeight + ClientConfig.MANA_BAR_Y.get();

        gui.blit(MANA_BAR_EMPTY, xM, yM, 0, 0, textureWidth, textureHeight, textureWidth, textureHeight);

        int filledMana = (int) (textureWidth * manaPercent);
        if (filledMana > 0) {
            gui.blit(MANA_BAR_FULL, xM, yM, 0, 0, filledMana, textureHeight, textureWidth, textureHeight);
        }

        String manaText = mana + "/" + maxMana;
        int textX = xM + textureWidth / 2 - mc.font.width(manaText) / 2;
        int textY = yM + (textureHeight - mc.font.lineHeight) / 2;
        gui.drawString(mc.font, Component.literal(manaText), textX, textY, 0xFFFFFF, true);
    }

    private static void renderXpIcon(GuiGraphics gui, Minecraft mc, int screenWidth, int screenHeight,
                                     UUID uuid, PlayerProgress progress) {
        final int textureWidth = 32;
        final int textureHeight = 32;

        int exp = progress.exp();
        int cap = Math.max(1, progress.expCap());

        float displayedXp = DISPLAYED_XP.getOrDefault(uuid, (float) exp);
        displayedXp += ((float) exp - displayedXp) * LERP_ALPHA;
        displayedXp = Math.max(0, Math.min(displayedXp, cap));
        DISPLAYED_XP.put(uuid, displayedXp);

        float xpPercent = displayedXp / (float) cap;

        int xXp = screenWidth / 2 + ClientConfig.XP_ICON_X.get();
        int yXp = screenHeight + ClientConfig.XP_ICON_Y.get();

        gui.blit(XP_BG, xXp, yXp, 0, 0, textureWidth, textureHeight, textureWidth, textureHeight);

        int filledHeight = (int) (textureHeight * xpPercent);
        if (filledHeight > 0) {
            gui.blit(XP_FILL, xXp, yXp + (textureHeight - filledHeight),
                    0, textureHeight - filledHeight,
                    textureWidth, filledHeight,
                    textureWidth, textureHeight);
        }

        String levelText = String.valueOf(progress.level());
        int levelX = xXp + textureWidth / 2 - mc.font.width(levelText) / 2;
        int levelY = yXp + textureHeight / 2 - mc.font.lineHeight / 2;
        gui.drawString(mc.font, Component.literal(levelText), levelX, levelY, 0xFFFFFF, true);

        String expText = exp + "/" + cap;
        int expX = xXp + textureWidth / 2 - mc.font.width(expText) / 2;
        int expY = yXp + textureHeight + 2;
        gui.drawString(mc.font, Component.literal(expText), expX, expY, 0xFFFFFF, false);
    }

    private static void renderSpellHotbar(GuiGraphics gg, Minecraft mc, int sw, int sh) {
        var p = mc.player;
        PlayerLoadoutAttachment loadout = p.getData(ModAttachments.PLAYER_LOADOUT.get());
        if (loadout == null) return;

        final int CELLS = PlayerLoadoutAttachment.SLOTS;
        boolean any = false;
        for (int i = 0; i < CELLS; i++) if (loadout.slots[i] != null) { any = true; break; }
        if (!any) return;

        int totalW = CELLS * SPELL_CELL_W + (CELLS - 1) * SPELL_CELL_PAD;
        int x0 = (sw - totalW) / 2;
        int y0 = sh - 60; // положение как обсуждали

        RenderSystem.enableBlend();

        for (int i = 0; i < CELLS; i++) {
            int x = x0 + i * (SPELL_CELL_W + SPELL_CELL_PAD);
            int y = y0;

            // фон
            gg.fill(x, y, x + SPELL_CELL_W, y + SPELL_CELL_H, 0x7F000000);
            gg.fill(x + 1, y + 1, x + SPELL_CELL_W - 1, y + SPELL_CELL_H - 1, 0x3FA0A0A0);

            // подпись (короткий id спелла)
            var rl = loadout.slots[i];
            if (rl != null) {
                String shortId = rl.getPath();
                if (shortId.length() > 8) shortId = shortId.substring(0, 8);
                gg.drawString(mc.font, shortId, x + 3, y + 6, 0xFFFFFF, false);
            } else {
                gg.drawString(mc.font, "-", x + 8, y + 6, 0xFF999999, false);
            }

            // хоткей в уголке
            String hk = Hotkeys.spellKeyName(i);
            gg.drawString(mc.font, hk, x + SPELL_CELL_W - mc.font.width(hk) - 2, y + SPELL_CELL_H - 9, 0xFFE6DDAA, false);

            // кулдаун (если > 0)
            int cd = loadout.getCooldown(i);
            if (cd > 0) {
                gg.fill(x + 2, y + 2, x + SPELL_CELL_W - 2, y + SPELL_CELL_H - 2, 0x80000000);
                String s = String.valueOf(cd / 20);
                gg.drawString(mc.font, s, x + (SPELL_CELL_W - mc.font.width(s)) / 2, y + 5, 0xFFFFFFFF, false);
            }
        }

        RenderSystem.disableBlend();
    }
}
