package com.doomspire.grimcore.attach;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.*;

/**
 * Таблица угрозы для одной боевой единицы (обычно — для моба).
 *
 * Модель:
 *  - БАЗОВАЯ угроза (накапливается от урона, постоянных статов и т.п.) — не "тает".
 *  - ВРЕМЕННЫЕ модификаторы (таунт/умения) — каждый с суммой и временем истечения.
 *  - Целью считается игрок с максимальной актуальной угрозой (base + активные temp).
 *
 * Никакого общего decay по тикам нет.
 */
public final class MobThreatAttachment {

    /** Порог «пыли», ниже которого записи очищаются. */
    public static final float EPSILON = 0.05f;

    /** Базовая угроза по игроку. */
    private final Map<UUID, Float> baseThreat = new HashMap<>();

    /** Временные модификаторы по игроку. */
    private final Map<UUID, List<TempBonus>> tempThreats = new HashMap<>();

    /** Момент последнего боевого события (для эвристик AI/логов). */
    private long lastCombatMs = 0L;

    /** Запись временного бонуса. */
    public static final class TempBonus {
        public float amount;
        public long expiresAtMs;

        public TempBonus(float amount, long expiresAtMs) {
            this.amount = amount;
            this.expiresAtMs = expiresAtMs;
        }
    }

    // ======== Мутации ========

    /** + к БАЗОВОЙ угрозе. Возвращает новое базовое значение. */
    public float addBaseThreat(UUID playerId, float amount) {
        if (playerId == null || amount <= 0f) return getBaseThreat(playerId);
        float next = Math.max(0f, baseThreat.getOrDefault(playerId, 0f) + amount);
        if (next < EPSILON) {
            baseThreat.remove(playerId);
        } else {
            baseThreat.put(playerId, next);
        }
        lastCombatMs = System.currentTimeMillis();
        return next;
    }

    /** Установить БАЗОВУЮ угрозу конкретного игрока. */
    public void setBaseThreat(UUID playerId, float value) {
        if (playerId == null) return;
        float v = Math.max(0f, value);
        if (v < EPSILON) baseThreat.remove(playerId);
        else baseThreat.put(playerId, v);
    }

    /** Добавить ВРЕМЕННУЮ угрозу (таунт/скилл) с длительностью в миллисекундах. */
    public void addTempThreat(UUID playerId, float amount, long durationMs) {
        if (playerId == null || amount <= 0f || durationMs <= 0L) return;
        long now = System.currentTimeMillis();
        long exp = now + durationMs;

        var list = tempThreats.computeIfAbsent(playerId, k -> new ArrayList<>());
        list.add(new TempBonus(amount, exp));
        lastCombatMs = now;
    }

    /** Очистить все угрозы конкретного игрока (base + temp). */
    public void clearFor(UUID playerId) {
        if (playerId == null) return;
        baseThreat.remove(playerId);
        tempThreats.remove(playerId);
    }

    /** Полная очистка. */
    public void clearAll() {
        baseThreat.clear();
        tempThreats.clear();
        lastCombatMs = 0L;
    }

    // ======== Чтение/вычисления ========

    /** БАЗОВАЯ угроза (без временных бонусов). */
    public float getBaseThreat(UUID playerId) {
        if (playerId == null) return 0f;
        return baseThreat.getOrDefault(playerId, 0f);
    }

    /** Полная угроза (base + актуальные temp), с ленивой очисткой просроченных бонусов. */
    public float totalThreatFor(UUID playerId, long nowMs) {
        if (playerId == null) return 0f;
        float base = baseThreat.getOrDefault(playerId, 0f);
        float sumTemp = sumAndPruneTemps(playerId, nowMs);
        float total = base + sumTemp;
        if (total < EPSILON) {
            // зачистим пустышки
            baseThreat.remove(playerId);
            if (sumTemp == 0f) tempThreats.remove(playerId);
            return 0f;
        }
        return total;
    }

    /** UUID игрока с максимальной актуальной угрозой (или null). */
    public UUID topThreatPlayer(long nowMs) {
        // объединённый набор ключей (у кого есть либо base, либо temp)
        Set<UUID> keys = new HashSet<>(baseThreat.keySet());
        keys.addAll(tempThreats.keySet());

        UUID bestId = null;
        float bestVal = 0f;

        for (UUID id : keys) {
            float val = totalThreatFor(id, nowMs);
            if (val > bestVal) {
                bestVal = val;
                bestId = id;
            }
        }
        // небольшая чистка глобальных карт, если всё осыпалось
        if (bestId == null) {
            baseThreat.entrySet().removeIf(e -> e.getValue() < EPSILON);
            tempThreats.entrySet().removeIf(e -> {
                float s = sumAndPruneTemps(e.getKey(), nowMs);
                return s < EPSILON;
            });
        }
        return bestId;
    }

    /** Топ-N (id, total) по убыванию, для отладки/UI. */
    public List<Map.Entry<UUID, Float>> topN(int n, long nowMs) {
        Set<UUID> keys = new HashSet<>(baseThreat.keySet());
        keys.addAll(tempThreats.keySet());
        ArrayList<Map.Entry<UUID, Float>> list = new ArrayList<>(keys.size());
        for (UUID id : keys) {
            float v = totalThreatFor(id, nowMs);
            if (v >= EPSILON) list.add(Map.entry(id, v));
        }
        list.sort((a, b) -> Float.compare(b.getValue(), a.getValue()));
        return list.size() > n ? list.subList(0, n) : list;
    }

    /** Были ли бои в последние timeoutMs миллисекунд. */
    public boolean isInCombat(long nowMs, long timeoutMs) {
        return lastCombatMs > 0L && (nowMs - lastCombatMs) <= Math.max(0L, timeoutMs);
    }

    // ======== Вспомогательное ========

    private float sumAndPruneTemps(UUID playerId, long nowMs) {
        var list = tempThreats.get(playerId);
        if (list == null || list.isEmpty()) return 0f;

        float sum = 0f;
        // лениво чистим просроченные
        list.removeIf(tb -> tb == null || tb.expiresAtMs <= nowMs || tb.amount < EPSILON);
        for (TempBonus tb : list) sum += tb.amount;

        if (list.isEmpty()) tempThreats.remove(playerId);
        return sum;
    }

    // ======== Сериализация (StreamCodec) ========

    /**
     * Формат:
     *  base: varInt size, затем (UUID hi, UUID lo, float base)
     *  temps: varInt size, затем для каждого ключа:
     *         (UUID hi, UUID lo, varInt listSize, повторить listSize раз: float amount, long expiresAtMs)
     *  lastCombatMs: long
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, MobThreatAttachment> STREAM_CODEC =
            StreamCodec.of(MobThreatAttachment::encode, MobThreatAttachment::decode);

    private static void encode(RegistryFriendlyByteBuf buf, MobThreatAttachment att) {
        // base
        buf.writeVarInt(att.baseThreat.size());
        for (var e : att.baseThreat.entrySet()) {
            UUID id = e.getKey();
            buf.writeLong(id.getMostSignificantBits());
            buf.writeLong(id.getLeastSignificantBits());
            buf.writeFloat(e.getValue());
        }
        // temps
        buf.writeVarInt(att.tempThreats.size());
        for (var e : att.tempThreats.entrySet()) {
            UUID id = e.getKey();
            buf.writeLong(id.getMostSignificantBits());
            buf.writeLong(id.getLeastSignificantBits());
            List<TempBonus> list = e.getValue();
            buf.writeVarInt(list != null ? list.size() : 0);
            if (list != null) {
                for (TempBonus tb : list) {
                    buf.writeFloat(tb.amount);
                    buf.writeLong(tb.expiresAtMs);
                }
            }
        }
        buf.writeLong(att.lastCombatMs);
    }

    private static MobThreatAttachment decode(RegistryFriendlyByteBuf buf) {
        MobThreatAttachment a = new MobThreatAttachment();
        // base
        int bSize = buf.readVarInt();
        for (int i = 0; i < bSize; i++) {
            UUID id = new UUID(buf.readLong(), buf.readLong());
            float v = buf.readFloat();
            if (v >= EPSILON) a.baseThreat.put(id, v);
        }
        // temps
        int tSize = buf.readVarInt();
        for (int i = 0; i < tSize; i++) {
            UUID id = new UUID(buf.readLong(), buf.readLong());
            int n = buf.readVarInt();
            if (n > 0) {
                ArrayList<TempBonus> list = new ArrayList<>(n);
                for (int j = 0; j < n; j++) {
                    float amt = buf.readFloat();
                    long exp = buf.readLong();
                    if (amt >= EPSILON) list.add(new TempBonus(amt, exp));
                }
                if (!list.isEmpty()) a.tempThreats.put(id, list);
            }
        }
        a.lastCombatMs = buf.readLong();
        return a;
    }
}
