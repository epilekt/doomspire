package com.doomspire.grimfate.config;

import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

public class ClientConfig {
    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue HEALTH_BAR_X;
    public static final ModConfigSpec.IntValue HEALTH_BAR_Y;

    public static final ModConfigSpec.IntValue MANA_BAR_X;
    public static final ModConfigSpec.IntValue MANA_BAR_Y;

    public static final ModConfigSpec.IntValue XP_ICON_X;
    public static final ModConfigSpec.IntValue XP_ICON_Y;

    public static final ModConfigSpec.IntValue SPELLBAR_X;
    public static final ModConfigSpec.IntValue SPELLBAR_Y;

    static {
        SPELLBAR_X = BUILDER
                .comment("Смещение спелбара по X")
                .defineInRange("hud.spellbar_x", -60, -500, 500);

        SPELLBAR_Y = BUILDER
                .comment("Смещение спелбара по X")
                .defineInRange("hud.spellbar_y", -78, -500, 500);

        HEALTH_BAR_X = BUILDER
                .comment("Смещение полоски здоровья по X")
                .defineInRange("hud.health_bar_x", -60, -500, 500);

        HEALTH_BAR_Y = BUILDER
                .comment("Смещение полоски здоровья по Y")
                .defineInRange("hud.health_bar_y", -78, -500, 500);

        MANA_BAR_X = BUILDER
                .comment("Смещение полоски маны по X")
                .defineInRange("hud.mana_bar_x", -60, -500, 500);

        MANA_BAR_Y = BUILDER
                .comment("Смещение полоски маны по Y")
                .defineInRange("hud.mana_bar_y", -65, -500, 500);

        XP_ICON_X = BUILDER
                .comment("Смещение иконки опыта по X")
                .defineInRange("hud.xp_icon_x", -30, -500, 500);

        XP_ICON_Y = BUILDER
                .comment("Смещение иконки опыта по Y")
                .defineInRange("hud.xp_icon_y", -100, -500, 500);
    }

    public static final ModConfigSpec SPEC = BUILDER.build();
}


