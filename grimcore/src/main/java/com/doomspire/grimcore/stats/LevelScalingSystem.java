package com.doomspire.grimcore.stats;

/**
 * Система скейлинга статов и урона от уровня.
 * Используется для пересчёта PlayerStats и формул оружия/заклинаний.
 */
public class LevelScalingSystem {

    private static final int HP_PER_LEVEL   = 10;
    private static final int MANA_PER_LEVEL = 10;
    private static final int SPELL_DAMAGE_PER_LEVEL = 5;
    private static final int WEAPON_DAMAGE_PER_LEVEL = 20;

    public static int scaleMaxHealth(int base, int level) {
        return base + HP_PER_LEVEL * (level - 1);
    }

    public static int scaleMaxMana(int base, int level) {
        return base + MANA_PER_LEVEL * (level - 1);
    }

    public static int scaleSpellDamage(int base, int level) {
        return base + SPELL_DAMAGE_PER_LEVEL * (level - 1);
    }

    public static int scaleWeaponDamage(int base, int level) {
        return base + WEAPON_DAMAGE_PER_LEVEL * (level - 1);
    }

    /**
     * Пересчитанные статы игрока под конкретный уровень.
     * На 1 уровне совпадают с PlayerStats.DEFAULT (100/100).
     */
    public static PlayerStats scaledStatsForLevel(int level) {
        int maxHealth = scaleMaxHealth(100, level);
        int maxMana   = scaleMaxMana(100, level);
        int regenHp   = 1 + (level / 2);
        int regenMana = 1 + (level / 3);

        return new PlayerStats(
                maxHealth,
                maxMana,
                maxHealth, // текущее здоровье при апе = фулл
                maxMana,   // текущая мана при апе = фулл
                regenHp,
                regenMana
        );
    }
}
