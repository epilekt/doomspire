package com.doomspire.grimcore.events;

import com.doomspire.grimcore.stats.MobStats;
import com.doomspire.grimcore.stats.PlayerStats;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public final class CoreDamageCalculator {

    // Правила для игроков
    private static final Map<ResourceKey<DamageType>, BiFunction<PlayerStats, Float, Integer>> PLAYER_RULES = new HashMap<>();
    // Правила для мобов
    private static final Map<ResourceKey<DamageType>, BiFunction<MobStats, Float, Integer>> MOB_RULES = new HashMap<>();

    static {
        // ---- игроки ----
        PLAYER_RULES.put(DamageTypes.FALL,
                (stats, vanilla) -> Math.max(1, Math.round(vanilla)));
        PLAYER_RULES.put(DamageTypes.DROWN,
                (stats, v) -> Math.max(1, Math.round(stats.maxHealth() * 0.10f)));
        PLAYER_RULES.put(DamageTypes.LAVA,
                (stats, v) -> Math.max(1, Math.round(stats.maxHealth() * 0.20f)));
        PLAYER_RULES.put(DamageTypes.OUTSIDE_BORDER,
                (stats, v) -> stats.maxHealth()); // мгновенная смерть

        // ---- мобы ----
        MOB_RULES.put(DamageTypes.FALL,
                (stats, vanilla) -> Math.max(1, Math.round(vanilla)));
        MOB_RULES.put(DamageTypes.DROWN,
                (stats, v) -> Math.max(1, Math.round(stats.getMaxHealth() * 0.10f)));
        MOB_RULES.put(DamageTypes.LAVA,
                (stats, v) -> Math.max(1, Math.round(stats.getMaxHealth() * 0.20f)));
        MOB_RULES.put(DamageTypes.OUTSIDE_BORDER,
                (stats, v) -> stats.getMaxHealth()); // мгновенная смерть
    }

    private CoreDamageCalculator() {}

    public static int calculateForPlayer(LivingEntity entity, DamageSource source, float vanillaAmount, PlayerStats stats) {
        for (var entry : PLAYER_RULES.entrySet()) {
            if (source.is(entry.getKey())) {
                return entry.getValue().apply(stats, vanillaAmount);
            }
        }
        return Math.max(1, Math.round(vanillaAmount));
    }

    public static int calculateForMob(LivingEntity entity, DamageSource source, float vanillaAmount, MobStats stats) {
        for (var entry : MOB_RULES.entrySet()) {
            if (source.is(entry.getKey())) {
                return entry.getValue().apply(stats, vanillaAmount);
            }
        }
        return Math.max(1, Math.round(vanillaAmount));
    }
}

