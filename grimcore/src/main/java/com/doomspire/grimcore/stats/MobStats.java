package com.doomspire.grimcore.stats;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

/**
 * Кастомные статы мобов (пока только здоровье).
 * Хранится как AttachmentType<MobStats>.
 */
public class MobStats {
    public static final MobStats DEFAULT = new MobStats(100, 100);

    private int maxHealth;
    private int currentHealth;

    public MobStats(int maxHealth, int currentHealth) {
        this.maxHealth = maxHealth;
        this.currentHealth = Math.min(currentHealth, maxHealth);
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public void setMaxHealth(int maxHealth) {
        this.maxHealth = maxHealth;
        if (currentHealth > maxHealth) {
            currentHealth = maxHealth;
        }
    }

    public int getCurrentHealth() {
        return currentHealth;
    }

    public void setCurrentHealth(int health) {
        this.currentHealth = Math.max(0, Math.min(health, maxHealth));
    }

    public void damage(int amount) {
        setCurrentHealth(currentHealth - amount);
    }

    public boolean isDead() {
        return currentHealth <= 0;
    }

    // 🔹 Кодек для NBT (сохраняем maxHealth и currentHealth)
    public static final Codec<MobStats> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("maxHealth").forGetter(MobStats::getMaxHealth),
            Codec.INT.fieldOf("currentHealth").forGetter(MobStats::getCurrentHealth)
    ).apply(instance, MobStats::new));

    // 🔹 Кодек для синхронизации (передача по сети)
    public static final StreamCodec<FriendlyByteBuf, MobStats> STREAM_CODEC = StreamCodec.of(
            (buf, stats) -> {
                buf.writeInt(stats.getMaxHealth());
                buf.writeInt(stats.getCurrentHealth());
            },
            buf -> new MobStats(buf.readInt(), buf.readInt())
    );
}


