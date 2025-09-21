package com.doomspire.grimcore.xp;

public final class LevelTable {
    private static final int MAX_LEVEL = 50; // временно, вынесем в конфиг
    private static final int BASE = 100;
    private static final double GROWTH = 1.10; // +10% к требованию на уровень

    private LevelTable() {}

    /** Сколько XP нужно, чтобы перейти с level → level+1 */
    public static int expForLevel(int level) {
        if (level <= 0) return BASE;
        return (int) Math.round(BASE * Math.pow(GROWTH, level - 1));
    }

    /** Сколько XP нужно всего, чтобы достичь этого уровня */
    public static int capForLevel(int level) {
        int sum = 0;
        for (int i = 1; i <= level; i++) {
            sum += expForLevel(i);
        }
        return sum;
    }

    public static int maxLevel() {
        return MAX_LEVEL;
    }
}

