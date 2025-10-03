package com.doomspire.grimcore.attach;

import com.doomspire.grimcore.stat.Attributes;
import com.doomspire.grimcore.stat.ModAttachments;
import com.doomspire.grimcore.stat.StatCalculator;
import com.doomspire.grimcore.stat.StatSnapshot;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import com.doomspire.grimcore.datapack.BalanceData;

import java.util.EnumMap;
import java.util.Locale;

//NOTE: Attachment игрока: очки атрибутов, текущие ресурсы и кэш агрегированных статов.

public class PlayerStatsAttachment {

    // ---- Текущие ресурсы (кастомные полосы) ----
    private int currentHealth = 100;
    private int currentMana   = 100;

    /**
     * Текущая «сверхщит» полоса (overshield).
     * Списывается ПЕРВОЙ при получении урона, затем уже здоровье.
     * Значение не входит в StatSnapshot, это runtime-ресурс (как временные бафы/щит).
     */
    private int overshield = 0;

    // ---- Распределённые очки по атрибутам ----
    private final EnumMap<Attributes, Integer> attributes = new EnumMap<>(Attributes.class);

    // ---- Очки, нераспределённые игроком ----
    private int unspentPoints = 0;

    // ---- Кэш снапшота и грязный флаг ----
    private StatSnapshot snapshot = new StatSnapshot();
    private boolean dirty = true;

    public PlayerStatsAttachment() {
        for (Attributes a : Attributes.values()) {
            attributes.put(a, 0);
        }
    }

    // ===================== API =====================

    public int getCurrentHealth() { return currentHealth; }
    public int getCurrentMana()   { return currentMana; }
    public int getOvershield()    { return overshield; }

    public void setCurrentHealth(int v) {
        int max = (int) Math.max(1, getSnapshot().maxHealth);
        currentHealth = Math.max(0, Math.min(v, max));
    }

    public void setCurrentMana(int v) {
        int max = (int) Math.max(1, getSnapshot().maxMana);
        currentMana = Math.max(0, Math.min(v, max));
    }

    /**
     * Установить текущий overshield с учётом верхней границы.
     * Верхнюю границу можно задавать через снапшот (например, статы/аффиксы),
     * иначе — ограничиваем «разумным» максимумом по здоровью.
     */
    public void setOvershield(int v) {
        int maxOs = estimateMaxOvershield();
        overshield = Math.max(0, Math.min(v, maxOs));
    }

    /** Увеличить overshield (для способностей/аффиксов), с клампом. */
    public void addOvershield(int delta) {
        if (delta <= 0) return;
        setOvershield(overshield + delta);
    }

    /**
     * Списать часть overshield, вернуть сколько НЕ покрылось (остаток урона).
     * Удобно вызывать из DamageEngine перед уроном по здоровью.
     */
    public int consumeOvershield(int amount) {
        if (amount <= 0 || overshield <= 0) return amount;
        int used = Math.min(amount, overshield);
        overshield -= used;
        return amount - used; // остаток, который пойдёт в здоровье/дальше по пайплайну
    }

    /** Оценка верхней границы overshield. Позже можно читать из снапшота (maxOvershield). */
    private int estimateMaxOvershield() {
        // Пока используем максимум равный текущему максимальному здоровью.
        // Если в StatCalculator появится отдельный stat maxOvershield — читать оттуда.
        return (int) Math.max(1, getSnapshot().maxHealth);
    }

    public void markDirty() { this.dirty = true; }

    public int getAttribute(Attributes attr) {
        return attributes.getOrDefault(attr, 0);
    }

    public void setAttribute(Attributes attr, int value) {
        attributes.put(attr, Math.max(0, value));
        dirty = true;
    }

    public void addAttribute(Attributes attr, int delta) {
        attributes.put(attr, Math.max(0, getAttribute(attr) + delta));
        dirty = true;
    }

    public int getUnspentPoints() { return unspentPoints; }
    public void setUnspentPoints(int v) { unspentPoints = Math.max(0, v); }
    public void addUnspentPoints(int amount) { if (amount > 0) unspentPoints += amount; }

    public int hardCapFor(Attributes attr) {
        return BalanceData.attrs().cap(attr);
    }

    /** Потратить 1 очко в атрибут с проверкой капа. Возвращает true при успехе. */
    public boolean tryAllocatePoint(Attributes attr) {
        if (unspentPoints <= 0) return false;
        int cap = hardCapFor(attr);
        int cur = getAttribute(attr);
        if (cur >= cap) return false;

        setAttribute(attr, cur + 1);
        unspentPoints--;
        return true;
    }

    /** Базовый снапшот без учёта аффиксов экипировки. */
    public StatSnapshot getSnapshot() {
        if (dirty) {
            snapshot = StatCalculator.calculate(this);
            dirty = false;
        }
        return snapshot;
    }

    /**
     * Полный снапшот с применением аффиксов владельца.
     * Если owner == null — вернёт базовый вариант как в {@link #getSnapshot()}.
     */
    public StatSnapshot getSnapshotWithAffixes(Player owner) {
        if (dirty) {
            if (owner != null) {
                snapshot = StatCalculator.calculateWithAffixes(this, owner);
            } else {
                snapshot = StatCalculator.calculate(this);
            }
            dirty = false;
        }
        return snapshot;
    }

    // ===================== NET (StreamCodec) =====================
    public static final StreamCodec<RegistryFriendlyByteBuf, PlayerStatsAttachment> STREAM_CODEC =
            StreamCodec.of(PlayerStatsAttachment::encode, PlayerStatsAttachment::decode);

    private static void encode(RegistryFriendlyByteBuf buf, PlayerStatsAttachment att) {
        buf.writeVarInt(att.unspentPoints);
        buf.writeVarInt(att.currentHealth);
        buf.writeVarInt(att.currentMana);
        buf.writeVarInt(att.overshield); // <<< новое поле
        for (Attributes a : Attributes.values()) {
            buf.writeVarInt(att.getAttribute(a));
        }
    }

    private static PlayerStatsAttachment decode(RegistryFriendlyByteBuf buf) {
        PlayerStatsAttachment att = new PlayerStatsAttachment();
        att.unspentPoints = buf.readVarInt();
        att.currentHealth = buf.readVarInt();
        att.currentMana   = buf.readVarInt();
        att.overshield    = buf.readVarInt(); // <<< читаем в том же порядке
        for (Attributes a : Attributes.values()) {
            att.attributes.put(a, buf.readVarInt());
        }
        att.dirty = true;
        return att;
    }

    // ===================== Утилиты =====================

    /** Достаёт аттачмент у игрока. */
    public static PlayerStatsAttachment get(Player player) {
        return player.getData(ModAttachments.PLAYER_STATS.get());
    }

    /** Пытается распарсить строковый id атрибута в enum (без краша). */
    public static Attributes parseAttrId(String id) {
        if (id == null) return null;
        try {
            return Attributes.valueOf(id.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
