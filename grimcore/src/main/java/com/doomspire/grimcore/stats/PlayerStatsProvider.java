package com.doomspire.grimcore.stats;

import com.doomspire.grimcore.runtime.PlayerRuntimeData;
import com.doomspire.grimcore.runtime.PlayerRuntimeManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
/**
 * Провайдер для PlayerStats.
 * Содержит старые методы работы с immutable PlayerStats,
 * а также runtime-кэш MutablePlayerStats и commitIfDirty.
 */
public class PlayerStatsProvider {

    // runtime cache: UUID -> MutablePlayerStats (синхронизируется/управляется вручную)
    private static final Map<UUID, MutablePlayerStats> RUNTIME_CACHE = new ConcurrentHashMap<>();

    // Настраиваемый интервал синхронизации (тик = 1/20 сек)
    private static final long SYNC_INTERVAL_TICKS = 20L; // раз в секунду по-умолчанию

    // ===== Существующие методы, оставляем их для совместимости =====
    public static PlayerStats get(Player player) {
        PlayerStats stats = player.getData(ModAttachments.PLAYER_STATS);
        return stats != null ? stats : PlayerStats.DEFAULT;
    }

    public static void set(Player player, PlayerStats stats) {
        player.setData(ModAttachments.PLAYER_STATS, stats);
    }

    public static void damage(Player player, int amount) {
        PlayerStats stats = get(player);
        int newHp = Math.max(0, stats.health() - amount);
        set(player, new PlayerStats(
                newHp,
                stats.mana(),
                stats.maxHealth(),
                stats.maxMana(),
                stats.healthRegen(),
                stats.manaRegen()
        ));
    }

    public static void consumeMana(Player player, int amount) {
        PlayerStats stats = get(player);
        int newMana = Math.max(0, stats.mana() - amount);
        set(player, new PlayerStats(
                stats.health(),
                newMana,
                stats.maxHealth(),
                stats.maxMana(),
                stats.healthRegen(),
                stats.manaRegen()
        ));
    }

    // ===== Новые runtime-методы =====

    /**
     * Получить или создать MutablePlayerStats для данного сервера игрока.
     * Использовать для частых изменений (regen, эффекты) — изменения будут
     * применяться локально и синхронизироваться контролируемо.
     */
    public static MutablePlayerStats getMutable(ServerPlayer player) {
        if (player == null) return null;
        UUID id = player.getUUID();
        return RUNTIME_CACHE.computeIfAbsent(id, uuid -> {
            PlayerStats current = get(player);
            MutablePlayerStats m = new MutablePlayerStats(current);
            // кешируем ссылку и в runtime data
            PlayerRuntimeData runtime = PlayerRuntimeManager.getOrCreate(player);
            runtime.mutableStats = m;
            return m;
        });
    }

    /**
     * Удалить mutable-кеш (вызвать при выходе игрока чтобы не держать ссылку).
     */
    public static void clearMutableCache(ServerPlayer player) {
        if (player == null) return;
        RUNTIME_CACHE.remove(player.getUUID());
        PlayerRuntimeData rt = PlayerRuntimeManager.get(player);
        if (rt != null) rt.mutableStats = null;
    }

    /**
     * Если в runtime-кеше есть изменённые (dirty) данные и прошёл интервал,
     * преобразовать их в immutable PlayerStats и записать в Attachment.
     * Это вызовет стандартную сериализацию/синхронизацию NeoForge.
     */
    public static void commitIfDirty(ServerPlayer player) {
        if (player == null || player.level().isClientSide()) return;
        MutablePlayerStats m = RUNTIME_CACHE.get(player.getUUID());
        if (m == null || !m.isDirty()) return;

        PlayerRuntimeData runtime = PlayerRuntimeManager.getOrCreate(player);
        long now = player.level().getGameTime();
        if (now - runtime.lastSyncTick < SYNC_INTERVAL_TICKS) {
            // ещё не прошло время синка
            return;
        }

        // Синхронизация: записываем immutable в attachment
        PlayerStats newStats = m.toImmutable();
        player.setData(ModAttachments.PLAYER_STATS, newStats);

        m.clearDirty();
        runtime.lastSyncTick = now;
        runtime.dirty = false;
    }
}
