package com.doomspire.grimcore.combat;

import com.doomspire.grimcore.stat.DamageTypes;
import net.minecraft.world.entity.LivingEntity;

import java.util.EnumMap;

/**
 * Контекст одного удара/заклинания.
 * Заполняется перед вызовом DamageEngine.
 */
public class DamageContext {
    public final LivingEntity attacker;
    public final LivingEntity target;
    public final EnumMap<DamageTypes, Float> damageMap = new EnumMap<>(DamageTypes.class);
    public boolean critical = false;

    public DamageContext(LivingEntity attacker, LivingEntity target) {
        this.attacker = attacker;
        this.target = target;
        for (DamageTypes t : DamageTypes.values()) damageMap.put(t, 0f);
    }

    public DamageContext add(DamageTypes type, float amount) {
        damageMap.put(type, damageMap.get(type) + amount);
        return this;
    }
}

