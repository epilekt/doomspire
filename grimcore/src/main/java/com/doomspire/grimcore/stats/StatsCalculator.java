package com.doomspire.grimcore.stats;

public class StatsCalculator {

    public static PlayerStats recalc(PlayerProgress progress) {
        int level = progress.level();

        int maxHp   = LevelScalingSystem.scaleMaxHealth(PlayerStats.DEFAULT.maxHealth(), level);
        int maxMana = LevelScalingSystem.scaleMaxMana(PlayerStats.DEFAULT.maxMana(), level);

        return new PlayerStats(
                maxHp,                   // health = полное восстановление при апгрейде
                maxMana,
                maxHp,
                maxMana,
                PlayerStats.DEFAULT.healthRegen(),
                PlayerStats.DEFAULT.manaRegen()
        );
    }

    public static int scaledSpellDamage(int base, int level) {
        return LevelScalingSystem.scaleSpellDamage(base, level);
    }

    public static int scaledWeaponDamage(int base, int level) {
        return LevelScalingSystem.scaleWeaponDamage(base, level);
    }
}
