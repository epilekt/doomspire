package com.doomspire.grimcore.stats;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Immutable статы игрока: здоровье, мана, реген.
 * Для runtime-изменений используется MutablePlayerStats.
 */
public record PlayerStats(
        int health,
        int mana,
        int maxHealth,
        int maxMana,
        int healthRegen,
        int manaRegen
) {
    // Сохранение в NBT/Json
    public static final Codec<PlayerStats> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("health").forGetter(PlayerStats::health),
            Codec.INT.fieldOf("mana").forGetter(PlayerStats::mana),
            Codec.INT.fieldOf("max_health").forGetter(PlayerStats::maxHealth),
            Codec.INT.fieldOf("max_mana").forGetter(PlayerStats::maxMana),
            Codec.INT.fieldOf("health_regen").forGetter(PlayerStats::healthRegen),
            Codec.INT.fieldOf("mana_regen").forGetter(PlayerStats::manaRegen)
    ).apply(instance, PlayerStats::new));

    // Синхронизация по сети
    public static final StreamCodec<FriendlyByteBuf, PlayerStats> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, PlayerStats::health,
            ByteBufCodecs.INT, PlayerStats::mana,
            ByteBufCodecs.INT, PlayerStats::maxHealth,
            ByteBufCodecs.INT, PlayerStats::maxMana,
            ByteBufCodecs.INT, PlayerStats::healthRegen,
            ByteBufCodecs.INT, PlayerStats::manaRegen,
            PlayerStats::new
    );

    // Базовые значения при первом входе (совпадают с LevelScalingSystem.scaledStatsForLevel(1))
    public static final PlayerStats DEFAULT = new PlayerStats(
            100,
            100,
            100,
            100,
            1,
            1
    );

    /** @return true если игрок "жив" (HP > 0) */
    public boolean isAlive() {
        return health > 0;
    }

    /** Вернёт новые статы с обновлённым здоровьем */
    public PlayerStats withHealth(int newHealth) {
        return new PlayerStats(
                newHealth,
                mana,
                maxHealth,
                maxMana,
                healthRegen,
                manaRegen
        );
    }

    /** Вернёт новые статы с обновлённой маной */
    public PlayerStats withMana(int newMana) {
        return new PlayerStats(
                health,
                newMana,
                maxHealth,
                maxMana,
                healthRegen,
                manaRegen
        );
    }
}