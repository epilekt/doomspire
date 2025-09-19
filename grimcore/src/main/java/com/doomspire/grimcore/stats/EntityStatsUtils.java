package com.doomspire.grimcore.stats;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class EntityStatsUtils {

    /**
     * ✅ Регенерация здоровья с использованием значения из статов.
     * (например, healthRegen у игрока)
     */
    public static void applyRegen(LivingEntity entity) {
        if (entity instanceof Player player) {
            PlayerStats stats = PlayerStatsProvider.get(player);
            if (stats == null) return;

            applyRegen(player, stats.healthRegen());
        } else {
            MobStats stats = MobStatsProvider.get(entity);
            if (stats == null) return;

            int baseRegen = 1; // 🔹 дефолтный реген для мобов (можно расширить позже)
            applyRegen(entity, baseRegen);
        }
    }

    /**
     * ✅ Регенерация здоровья с заданным количеством (игнорирует встроенные значения).
     * Используется системой тик-регена, баффами или предметами.
     */
    public static void applyRegen(LivingEntity entity, int amount) {
        if (amount <= 0) return;

        if (entity instanceof Player player) {
            PlayerStats stats = PlayerStatsProvider.get(player);
            if (stats == null) return;

            if (stats.health() < stats.maxHealth()) {
                int newHealth = Math.min(stats.maxHealth(), stats.health() + amount);
                PlayerStatsProvider.set(player, new PlayerStats(
                        newHealth,
                        stats.mana(),
                        stats.maxHealth(),
                        stats.maxMana(),
                        stats.healthRegen(),
                        stats.manaRegen()
                ));
            }
        } else {
            MobStats stats = MobStatsProvider.get(entity);
            if (stats == null) return;

            if (stats.getCurrentHealth() < stats.getMaxHealth()) {
                int newHealth = Math.min(stats.getMaxHealth(), stats.getCurrentHealth() + amount);
                MobStatsProvider.set(entity, new MobStats(stats.getMaxHealth(), newHealth));
            }
        }
    }

    /**
     * ✅ Регенерация маны с использованием значения из статов игрока.
     */
    public static void applyManaRegen(Player player) {
        PlayerStats stats = PlayerStatsProvider.get(player);
        if (stats == null) return;

        applyManaRegen(player, stats.manaRegen());
    }

    /**
     * ✅ Регенерация маны с заданным количеством.
     */
    public static void applyManaRegen(Player player, int amount) {
        if (amount <= 0) return;

        PlayerStats stats = PlayerStatsProvider.get(player);
        if (stats == null) return;

        if (stats.mana() < stats.maxMana()) {
            int newMana = Math.min(stats.maxMana(), stats.mana() + amount);
            PlayerStatsProvider.set(player, new PlayerStats(
                    stats.health(),
                    newMana,
                    stats.maxHealth(),
                    stats.maxMana(),
                    stats.healthRegen(),
                    stats.manaRegen()
            ));
        }
    }
}

