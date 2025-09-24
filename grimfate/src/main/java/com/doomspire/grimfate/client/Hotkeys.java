package com.doomspire.grimfate.client;

import com.doomspire.grimfate.client.gui.StatsHubScreen;
import com.doomspire.grimfate.core.Grimfate;
import com.doomspire.grimfate.network.ModNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

public final class Hotkeys {
    private Hotkeys() {}

    private static final String CAT = "key.categories.grimfate";

    // Используем только новые ID, чтобы исключить любые пересечения:
    private static final String[] SPELL_IDS = {
            "key.grimfate.spellslot_1",
            "key.grimfate.spellslot_2",
            "key.grimfate.spellslot_3",
            "key.grimfate.spellslot_4",
            "key.grimfate.spellslot_5",
            "key.grimfate.spellslot_6"
    };
    private static final int[] DEFAULTS = {
            GLFW.GLFW_KEY_R, GLFW.GLFW_KEY_F, GLFW.GLFW_KEY_C,
            GLFW.GLFW_KEY_V, GLFW.GLFW_KEY_B, GLFW.GLFW_KEY_N
    };

    public static KeyMapping OPEN_STATS;
    private static final KeyMapping[] SPELL_KEYS = new KeyMapping[6];

    private static boolean CREATED = false;
    private static boolean REGISTERED = false;

    private static void ensureCreated() {
        if (CREATED) return;
        CREATED = true;

        OPEN_STATS = new KeyMapping("key.grimfate.open_stats", GLFW.GLFW_KEY_H, CAT);
        for (int i = 0; i < SPELL_KEYS.length; i++) {
            SPELL_KEYS[i] = new KeyMapping(SPELL_IDS[i], DEFAULTS[i], CAT);
        }
    }

    /** MOD-bus: регистрация key mappings. */
    public static void onRegisterKeys(RegisterKeyMappingsEvent e) {
        ensureCreated();
        if (REGISTERED) {
            Grimfate.LOGGER.warn("[GF] Key mappings already registered — skip.");
            return;
        }
        REGISTERED = true;

        e.register(OPEN_STATS);
        for (KeyMapping km : SPELL_KEYS) e.register(km);
    }

    /** Forge-bus: обработка нажатий. Вешается из client-setup. */
    public static void onClientTick(ClientTickEvent.Post e) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer p = mc.player;
        if (p == null) return;

        if (OPEN_STATS != null && OPEN_STATS.consumeClick() && mc.screen == null) {
            mc.setScreen(new StatsHubScreen());
        }
        for (int i = 0; i < SPELL_KEYS.length; i++) {
            KeyMapping km = SPELL_KEYS[i];
            if (km != null && km.consumeClick()) {
                ModNetworking.sendCastSpellSlot(i);
            }
        }
    }

    public static String spellKeyName(int idx) {
        if (idx < 0 || idx >= SPELL_KEYS.length || SPELL_KEYS[idx] == null) return "?";
        return SPELL_KEYS[idx].getTranslatedKeyMessage().getString();
    }
}

