package com.doomspire.grimcore.runtime;

/**
 * Лёгкий runtime-хаб для каждого игрока.
 * Не сериализуется. Хранится в ConcurrentHashMap на сервере.
 */
public class PlayerRuntimeData {
    public double healthAccumulator = 0.0;
    public double manaAccumulator = 0.0;
    public long lastSyncTick = 0L; // gameTime последнего синка
    public boolean dirty = false;  // пометка для внешних систем
    // Кеши для тяжёлых вычислений
    public int cachedDamage = -1;
    public long cacheUntilTick = 0L;
}
