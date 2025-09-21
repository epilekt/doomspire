package com.doomspire.grimcore.combat;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;

import java.util.Map;

public final class EnvironmentalDamage {
    private EnvironmentalDamage(){}

    // проценты от maxHP (0..1)
    private static final Map<net.minecraft.resources.ResourceKey<net.minecraft.world.damagesource.DamageType>, Float> PERCENTS = Map.of(
            DamageTypes.FALL, 0.15f,
            DamageTypes.DROWN, 0.10f,
            DamageTypes.LAVA, 0.25f,
            DamageTypes.ON_FIRE, 0.08f,
            DamageTypes.IN_FIRE, 0.12f,
            DamageTypes.HOT_FLOOR, 0.10f,
            DamageTypes.SWEET_BERRY_BUSH, 0.04f,
            DamageTypes.OUTSIDE_BORDER, 1.0f
    );

    public static Float percentFor(DamageSource src) {
        for (var e : PERCENTS.entrySet()) {
            if (src.is(e.getKey())) return e.getValue();
        }
        return null;
    }
}
