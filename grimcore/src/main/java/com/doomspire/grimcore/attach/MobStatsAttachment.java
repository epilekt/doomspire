package com.doomspire.grimcore.attach;

import com.doomspire.grimcore.stat.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.EnumMap;

public class MobStatsAttachment {
    private final EnumMap<Attributes, Integer> attributes = new EnumMap<>(Attributes.class);
    private int currentHealth = 100;
    private boolean dirty = true;
    private StatSnapshot snapshot = new StatSnapshot();

    public MobStatsAttachment() {
        // у мобов нет маны → пропускаем SPIRIT
        for (Attributes a : Attributes.values()) {
            if (a != Attributes.SPIRIT) attributes.put(a, 0);
        }
    }

    public int getAttribute(Attributes attr) {
        return attributes.getOrDefault(attr, 0);
    }
    public void setAttribute(Attributes attr, int value) {
        if (attr == Attributes.SPIRIT) return; // игнор
        attributes.put(attr, Math.max(0, value)); dirty = true;
    }
    public void addAttribute(Attributes attr, int delta) {
        if (attr == Attributes.SPIRIT) return;
        attributes.put(attr, Math.max(0, getAttribute(attr) + delta)); dirty = true;
    }

    public int getCurrentHealth() { return currentHealth; }
    public void setCurrentHealth(int v) {
        int max = (int)Math.max(1, getSnapshot().maxHealth);
        currentHealth = Math.max(0, Math.min(v, max));
    }

    public StatSnapshot getSnapshot() {
        if (dirty) {
            snapshot = MobStatCalculator.calculate(this);
            dirty = false;
        }
        return snapshot;
    }
    public void markDirty() { dirty = true; }

    // --- net sync ---
    public static final StreamCodec<RegistryFriendlyByteBuf, MobStatsAttachment> STREAM_CODEC =
            StreamCodec.of(MobStatsAttachment::encode, MobStatsAttachment::decode);

    private static void encode(RegistryFriendlyByteBuf buf, MobStatsAttachment att) {
        buf.writeVarInt(att.currentHealth);
        // порядок атрибутов фиксируем:
        for (Attributes a : Attributes.values()) {
            if (a == Attributes.SPIRIT) continue;
            buf.writeVarInt(att.getAttribute(a));
        }
    }
    private static MobStatsAttachment decode(RegistryFriendlyByteBuf buf) {
        MobStatsAttachment att = new MobStatsAttachment();
        att.currentHealth = buf.readVarInt();
        for (Attributes a : Attributes.values()) {
            if (a == Attributes.SPIRIT) continue;
            att.attributes.put(a, buf.readVarInt());
        }
        att.dirty = true;
        return att;
    }
}

