package com.doomspire.grimcore.attach;

import com.doomspire.grimcore.stat.PlayerProgress;
import com.doomspire.grimcore.xp.LevelTable;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

/**
 * Персистентный прогресс игрока (уровень/опыт/кап). Сохраняется через CODEC.
 * Клиенту отдаём лёгкий снапшот PlayerProgress.
 */
public class PlayerProgressAttachment {

    private int level;
    private int exp;
    private int expCap;

    public PlayerProgressAttachment() {
        this.level = 1;
        this.exp = 0;
        this.expCap = LevelTable.expForLevel(1); // кап до 2-го уровня
    }

    public int level() { return level; }
    public int exp() { return exp; }
    public int expCap() { return expCap; }

    /** Возвращает число полученных уровней (для выдачи очков атрибутов). */
    public int addExp(int amount) {
        if (amount <= 0) return 0;
        int gained = 0;
        exp += amount;
        while (exp >= expCap && level < LevelTable.maxLevel()) {
            exp -= expCap;
            level++;
            expCap = LevelTable.expForLevel(level);
            gained++;
        }
        return gained;
    }

    /** Снимок для HUD/клиента. */
    public PlayerProgress toSnapshot() {
        return new PlayerProgress(level, exp, expCap);
    }

    // ---------- Persist (save/load) ----------
    public static final Codec<PlayerProgressAttachment> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.INT.fieldOf("level").forGetter(PlayerProgressAttachment::level),
            Codec.INT.fieldOf("exp").forGetter(PlayerProgressAttachment::exp),
            Codec.INT.fieldOf("exp_cap").forGetter(PlayerProgressAttachment::expCap)
    ).apply(i, (lvl, e, cap) -> {
        PlayerProgressAttachment a = new PlayerProgressAttachment();
        a.level = Math.max(1, lvl);
        a.exp = Math.max(0, e);
        a.expCap = Math.max(1, cap);
        return a;
    }));

    // ---------- Network (instant sync, если шлём кастомный пакет) ----------
    public static final StreamCodec<RegistryFriendlyByteBuf, PlayerProgressAttachment> STREAM_CODEC =
            StreamCodec.of(PlayerProgressAttachment::encode, PlayerProgressAttachment::decode);

    private static void encode(RegistryFriendlyByteBuf buf, PlayerProgressAttachment a) {
        buf.writeVarInt(a.level);
        buf.writeVarInt(a.exp);
        buf.writeVarInt(a.expCap);
    }

    private static PlayerProgressAttachment decode(RegistryFriendlyByteBuf buf) {
        PlayerProgressAttachment a = new PlayerProgressAttachment();
        a.level  = buf.readVarInt();
        a.exp    = buf.readVarInt();
        a.expCap = buf.readVarInt();
        return a;
    }
}



