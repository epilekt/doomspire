package com.doomspire.grimcore.stats;

import net.minecraft.world.entity.LivingEntity;

public class MobStatsProvider {

    public static MobStats get(LivingEntity mob) {
        MobStats stats = mob.getData(ModAttachments.MOB_STATS);
        return stats != null ? stats : MobStats.DEFAULT;
    }

    public static void set(LivingEntity mob, MobStats stats) {
        mob.setData(ModAttachments.MOB_STATS, stats);
    }

    public static void damage(LivingEntity mob, int amount) {
        MobStats stats = get(mob);
        int health = Math.max(0, stats.getCurrentHealth() - amount);
        set(mob, new MobStats(
                stats.getMaxHealth(),
                health
        ));
    }

    public static void heal(LivingEntity mob, int amount) {
        MobStats stats = get(mob);
        int health = Math.min(stats.getMaxHealth(), stats.getCurrentHealth() + amount);
        set(mob, new MobStats(
                stats.getMaxHealth(),
                health
        ));
    }
}
