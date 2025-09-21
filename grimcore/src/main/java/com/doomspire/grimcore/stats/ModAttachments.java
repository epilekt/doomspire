package com.doomspire.grimcore.stats;

import com.doomspire.grimcore.Grimcore;
import com.doomspire.grimcore.attach.MobStatsAttachment;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.attachment.AttachmentType;
import com.doomspire.grimcore.attach.PlayerStatsAttachment;

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
}

