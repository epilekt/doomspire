package com.doomspire.grimcore.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class CoreCommonConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue PULL_ENABLED;
    public static final ModConfigSpec.IntValue PULL_RADIUS;     // 0..4
    public static final ModConfigSpec.IntValue MAX_CONTAINERS;  // safety cap

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        PULL_ENABLED   = b.comment("Enable QoL pulling from nearby containers").define("pullEnabled", true);
        PULL_RADIUS    = b.comment("Pull radius (0..4)").defineInRange("pullRadius", 3, 0, 4);
        MAX_CONTAINERS = b.comment("Safety cap for scanned containers").defineInRange("maxContainers", 24, 1, 128);
        SPEC = b.build();
    }

    private CoreCommonConfig() {}
}



