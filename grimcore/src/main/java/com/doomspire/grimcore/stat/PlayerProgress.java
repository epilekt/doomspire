package com.doomspire.grimcore.stat;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * Чистые данные об уровне игрока.
 * Только хранение + сериализация в NBT/сеть.
 */
public record PlayerProgress(int level, int exp, int expCap) {

    public static final Codec<PlayerProgress> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("level").forGetter(PlayerProgress::level),
            Codec.INT.fieldOf("exp").forGetter(PlayerProgress::exp),
            Codec.INT.fieldOf("exp_cap").forGetter(PlayerProgress::expCap)
    ).apply(instance, PlayerProgress::new));

    public static final StreamCodec<FriendlyByteBuf, PlayerProgress> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, PlayerProgress::level,
            ByteBufCodecs.INT, PlayerProgress::exp,
            ByteBufCodecs.INT, PlayerProgress::expCap,
            PlayerProgress::new
    );

    public static final PlayerProgress DEFAULT = new PlayerProgress(1, 0, 100);

    /**
     * Вспомогательный метод для проверки прогресса на клиенте (HUD).
     */
    public String hudString() {
        return "Lvl " + level + " (" + exp + "/" + expCap + ")";
    }
}
