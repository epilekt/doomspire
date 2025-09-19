package com.doomspire.grimcore.stats;

import com.doomspire.grimcore.Grimcore;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.attachment.AttachmentType;

public class ModAttachments {
    // Регистрируем все AttachmentType под нашим модом
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Grimcore.MODID);

    // Attachment для PlayerStats
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<PlayerStats>> PLAYER_STATS =
            ATTACHMENT_TYPES.register("player_stats",
                    () -> AttachmentType.builder(() -> PlayerStats.DEFAULT)
                            .serialize(PlayerStats.CODEC)   // сохранение в NBT
                            //.copyOnDeath()                // дрисня, которая копирует статы мертвого игрока буквально
                            .sync(PlayerStats.STREAM_CODEC)                // главное! синхронизация на клиент
                            .build()
            );
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<MobStats>> MOB_STATS =
            ATTACHMENT_TYPES.register("mob_stats",
                    () -> AttachmentType.builder(() -> MobStats.DEFAULT)
                            .serialize(MobStats.CODEC)
                            .sync(MobStats.STREAM_CODEC)
                            .build()
            );
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<PlayerProgress>> PLAYER_PROGRESS =
            ATTACHMENT_TYPES.register("player_progress",
                    () -> AttachmentType.builder(() -> PlayerProgress.DEFAULT)
                            .serialize(PlayerProgress.CODEC)
                            .sync(PlayerProgress.STREAM_CODEC)
                            .build()
            );

}
