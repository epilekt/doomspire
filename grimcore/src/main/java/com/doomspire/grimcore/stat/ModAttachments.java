package com.doomspire.grimcore.stat;

import com.doomspire.grimcore.Grimcore;
import com.doomspire.grimcore.attach.*;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.attachment.AttachmentType;

public final class ModAttachments {
    private ModAttachments() {}

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Grimcore.MODID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<PlayerStatsAttachment>> PLAYER_STATS =
            ATTACHMENT_TYPES.register("player_stats",
                    () -> AttachmentType.builder(PlayerStatsAttachment::new)
                            // персист сохранять позже, когда добавим Codec:
                            // .serialize(YourCodecHere)
                            .sync(PlayerStatsAttachment.STREAM_CODEC) // сеть ОК
                            .build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<MobStatsAttachment>> MOB_STATS =
            ATTACHMENT_TYPES.register("mob_stats",
                    () -> AttachmentType.builder(MobStatsAttachment::new)
                            .sync(MobStatsAttachment.STREAM_CODEC) // можно выключить, если на клиенте не нужно
                            .build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<PlayerProgressAttachment>> PLAYER_PROGRESS =
            ATTACHMENT_TYPES.register("player_progress", () ->
                    AttachmentType.builder(PlayerProgressAttachment::new)
                            .serialize(PlayerProgressAttachment.CODEC)        // автосейв в сейв игрока
                            .sync(PlayerProgressAttachment.STREAM_CODEC)      // авто-синк при замене
                            .build()
            );

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<PlayerLoadoutAttachment>> PLAYER_LOADOUT =
            ATTACHMENT_TYPES.register("player_loadout",
                    () -> AttachmentType.builder(PlayerLoadoutAttachment::new)
                            .serialize(PlayerLoadoutAttachment.CODEC)      // хот-бар сохраняется
                            .sync(PlayerLoadoutAttachment.STREAM_CODEC)     // ВАЖНО: клиент видит изменения
                            .build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<MobThreatAttachment>> MOB_THREAT =
            ATTACHMENT_TYPES.register("mob_threat",
                    () -> AttachmentType.builder(MobThreatAttachment::new)
                            .sync(MobThreatAttachment.STREAM_CODEC) // сеть
                            //.serialize(MobThreatAttachment.CODEC) // добавим позже, если нужно сохранять в сейв
                            .build());


}

