package com.doomspire.grimfate.spell;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Кастомный фаерболт с полем урона.
 */
public class FireboltEntity extends SmallFireball {
    private final int spellDamage;

    public FireboltEntity(Level level, LivingEntity shooter, Vec3 direction, int damage) {
        // используем стандартный EntityType.SMALL_FIREBALL
        super(EntityType.SMALL_FIREBALL, level);
        this.setOwner(shooter);

        // Задаём вектор движения (скорость)
        this.setDeltaMovement(direction);

        this.spellDamage = damage;
    }

    public int getSpellDamage() {
        return spellDamage;
    }
}
