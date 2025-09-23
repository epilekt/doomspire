package com.doomspire.grimcore.xp;

import com.doomspire.grimcore.datapack.BalanceData;

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
        return Math.max(1, BalanceData.levels().maxLevel());
    }

    /** Сколько XP нужно, чтобы достичь уровня L (с начала прогрессии). */
    public static long totalXpForLevel(int level) {
        var lv = BalanceData.levels();
        int L = Math.max(1, Math.min(level, lv.maxLevel()));
        double base = lv.base();
        double growth = lv.growth();
        // Геометрическая прогрессия: base * growth^(L-1) суммой от 1..L-1
        double sum = 0.0;
        double term = base;
        for (int i = 1; i < L; i++) {
            sum += term;
            term *= growth;
        }
        return Math.round(sum);
    }

    /** Сколько XP нужно от L до L+1 (инкрементальный шаг). */
    public static int xpForNextLevel(int level) {
        var lv = BalanceData.levels();
        int L = Math.max(1, Math.min(level, lv.maxLevel()));
        return (int)Math.round(lv.base() * Math.pow(lv.growth(), Math.max(0, L-1)));
    }
}

