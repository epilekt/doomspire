package com.doomspire.grimcore.runtime;

import com.doomspire.grimcore.stats.MutablePlayerStats;

/**
 * Лёгкий runtime-хаб для каждого игрока.
 * Не сериализуется. Хранится в ConcurrentHashMap на сервере.
 */
public class PlayerRuntimeData {
    public double healthAccumulator = 0.0;
    public double manaAccumulator = 0.0;
    public long lastSyncTick = 0L; // gameTime последнего синка
    public boolean dirty = false;  // пометка для внешних систем
    public MutablePlayerStats mutableStats = null; // кэш мутабельных статов (если создан)
    // Кеши для тяжёлых вычислений
    public int cachedDamage = -1;
    public long cacheUntilTick = 0L;
}
