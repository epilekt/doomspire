package com.doomspire.grimcore.attach;

import com.doomspire.grimcore.stat.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.EnumMap;

/**
 * Attachment: хранит базовые атрибуты игрока и кэш статов.
 */
public class PlayerStatsAttachment {
    // текущее состояние ресурсов
    private int currentHealth = 100;
    private int currentMana   = 100;

    public int getCurrentHealth() { return currentHealth; }
    public int getCurrentMana()   { return currentMana;   }

    public void setCurrentHealth(int v) {
        int max = (int)Math.max(1, getSnapshot().maxHealth);
        currentHealth = Math.max(0, Math.min(v, max));
    }

    public void setCurrentMana(int v) {
        int max = (int)Math.max(1, getSnapshot().maxMana);
        currentMana = Math.max(0, Math.min(v, max));
    }

    public void markDirty() { this.dirty = true; }
    private final EnumMap<Attributes, Integer> attributes = new EnumMap<>(Attributes.class);
    private int unspentPoints = 0;

    private StatSnapshot snapshot = new StatSnapshot();
    private boolean dirty = true;

    public PlayerStatsAttachment() {
        for (Attributes a : Attributes.values()) {
            attributes.put(a, 0);
        }
    }

    // --- API ---

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

    public int getUnspentPoints() {
        return unspentPoints;
    }

    public void addUnspentPoints(int amount) {
        unspentPoints += amount;
    }

    public void spendPoint(Attributes attr) {
        if (unspentPoints > 0) {
            addAttribute(attr, 1);
            unspentPoints--;
        }
    }

    public StatSnapshot getSnapshot() {
        if (dirty) {
            snapshot = StatCalculator.calculate(this);
            dirty = false;
        }
        return snapshot;
    }

    // --- Sync ---
    public static final StreamCodec<RegistryFriendlyByteBuf, PlayerStatsAttachment> STREAM_CODEC =
            StreamCodec.of(PlayerStatsAttachment::encode, PlayerStatsAttachment::decode);

    private static void encode(RegistryFriendlyByteBuf buf, PlayerStatsAttachment att) {
        buf.writeVarInt(att.unspentPoints);
        buf.writeVarInt(att.currentHealth);
        buf.writeVarInt(att.currentMana);
        for (Attributes a : Attributes.values()) {
            buf.writeVarInt(att.getAttribute(a));
        }
    }

    private static PlayerStatsAttachment decode(RegistryFriendlyByteBuf buf) {
        PlayerStatsAttachment att = new PlayerStatsAttachment();
        att.unspentPoints = buf.readVarInt();
        att.currentHealth = buf.readVarInt();
        att.currentMana   = buf.readVarInt();
        for (Attributes a : Attributes.values()) {
            att.attributes.put(a, buf.readVarInt());
        }
        att.dirty = true;
        return att;
    }
}

