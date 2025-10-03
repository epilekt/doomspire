package com.doomspire.grimcore.attach;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;

import java.util.*;

//NOTE: Таблица угрозы для одной боевой единицы (обычно — для моба).
/**
 * Модель:
 *  - Угроза накапливается по игрокам (UUID -> threat).
 *  - Экспоненциально затухает со временем (decay).
 *  - Целью считается игрок с максимальной текущей угрозой.
 *
 * Применение:
 *  - Слой боёвки добавляет угрозу при нанесении урона: addThreat(attacker, dmg * factor)
 *  - Заклинания могут добавлять «чистую» угрозу (Таунт/Могучий удар): addThreat(attacker, flat)
 *  - Серверный тикер/AI периодически вызывает decay(...)
 *
 * Замечания по производительности:
 *  - Карта чистится от «пыли» (значения < epsilon).
 *  - Операции O(n) только при поиске максимума (n — число агрессоров по данному мобу).
 */
public final class MobThreatAttachment {

    /** Порог отсеивания «пыли» при чистке карты угрозы. */
    public static final float EPSILON = 0.05f;

    /**
     * По умолчанию затухаем на ~15% в секунду:
     * decay = value * (1 - 0.15)^seconds
     */
    public static final float DEFAULT_DECAY_PER_SEC = 0.15f;

    private final Map<UUID, Float> threat = new HashMap<>();
    private long lastCombatMs = 0L;

    // ---------- Мутации ----------

    /** Добавить угрозу атакеру. Возвращает итоговое значение угрозы для этого игрока. */
    public float addThreat(UUID playerId, float amount) {
        if (playerId == null || amount <= 0f) return getThreat(playerId);
        float val = threat.getOrDefault(playerId, 0f) + amount;
        val = Mth.clamp(val, 0f, Float.MAX_VALUE);
        threat.put(playerId, val);
        lastCombatMs = System.currentTimeMillis();
        return val;
    }

    /** Установить (перезаписать) угрозу конкретного игрока значением >= 0. */
    public void setThreat(UUID playerId, float value) {
        if (playerId == null) return;
        float v = Math.max(0f, value);
        if (v < EPSILON) {
            threat.remove(playerId);
        } else {
            threat.put(playerId, v);
        }
    }

    /** Обнулить угрозу указанного игрока. */
    public void clearFor(UUID playerId) {
        if (playerId != null) threat.remove(playerId);
    }

    /** Полная очистка. */
    public void clearAll() {
        threat.clear();
        lastCombatMs = 0L;
    }

    // ---------- Чтение/вычисления ----------

    /** Текущее значение угрозы по игроку (0, если нет записей). */
    public float getThreat(UUID playerId) {
        if (playerId == null) return 0f;
        return threat.getOrDefault(playerId, 0f);
    }

    /** UUID игрока с максимальной угрозой, либо null если карта пуста. */
    public UUID topThreatPlayer() {
        Map.Entry<UUID, Float> best = null;
        for (Map.Entry<UUID, Float> e : threat.entrySet()) {
            if (best == null || e.getValue() > best.getValue()) best = e;
        }
        return best != null ? best.getKey() : null;
    }

    /** Есть ли активный бой (за последние {@code timeoutMs} миллисекунд накапливалась угроза). */
    public boolean isInCombat(long nowMs, long timeoutMs) {
        return lastCombatMs > 0L && (nowMs - lastCombatMs) <= Math.max(0L, timeoutMs);
    }

    /** Небольшая копия топ-N для UI/отладки (по убыванию). */
    public List<Map.Entry<UUID, Float>> topN(int n) {
        ArrayList<Map.Entry<UUID, Float>> list = new ArrayList<>(threat.entrySet());
        list.sort((a, b) -> Float.compare(b.getValue(), a.getValue()));
        return list.size() > n ? list.subList(0, n) : list;
    }

    // ---------- Затухание ----------

    /**
     * Экспоненциальное затухание.
     * keep = (1 - decayPerSec) ^ seconds
     * Пример: seconds=0.5, decayPerSec=0.15 -> сохранится ~92.8%.
     *
     * @param seconds     прошедшее время, сек
     * @param decayPerSec коэффициент затухания в секунду [0..1]
     */
    public void decay(float seconds, float decayPerSec) {
        if (threat.isEmpty()) return;
        float s = Math.max(0f, seconds);
        float d = Mth.clamp(decayPerSec, 0f, 1f);

        if (s <= 0f || d <= 0f) return;

        float keep = (float) Math.pow(1.0 - d, s);
        if (!(keep > 0f)) { // NaN или 0
            clearAll();
            return;
        }

        threat.replaceAll((id, v) -> v * keep);
        // зачистка «пыли»
        threat.entrySet().removeIf(e -> e.getValue() < EPSILON);
    }

    // ---------- Сериализация ----------

    /**
     * Лёгкий двоичный кодек для сети/персиста.
     * Формат:
     *  - varInt size
     *  - повторить size раз: (UUID hi, UUID lo, float value)
     *  - long lastCombatMs
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, MobThreatAttachment> STREAM_CODEC =
            StreamCodec.of(MobThreatAttachment::encode, MobThreatAttachment::decode);

    private static void encode(RegistryFriendlyByteBuf buf, MobThreatAttachment att) {
        buf.writeVarInt(att.threat.size());
        for (Map.Entry<UUID, Float> e : att.threat.entrySet()) {
            UUID id = e.getKey();
            buf.writeLong(id.getMostSignificantBits());
            buf.writeLong(id.getLeastSignificantBits());
            buf.writeFloat(e.getValue());
        }
        buf.writeLong(att.lastCombatMs);
    }

    private static MobThreatAttachment decode(RegistryFriendlyByteBuf buf) {
        MobThreatAttachment a = new MobThreatAttachment();
        int n = buf.readVarInt();
        for (int i = 0; i < n; i++) {
            long hi = buf.readLong();
            long lo = buf.readLong();
            float v = buf.readFloat();
            UUID id = new UUID(hi, lo);
            if (v >= EPSILON) a.threat.put(id, v);
        }
        a.lastCombatMs = buf.readLong();
        return a;
    }
}
