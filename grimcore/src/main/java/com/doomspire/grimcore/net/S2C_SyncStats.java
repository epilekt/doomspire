package com.doomspire.grimcore.net;

import com.doomspire.grimcore.attach.PlayerStatsAttachment;
import com.doomspire.grimcore.stat.ModAttachments;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record S2C_SyncStats(PlayerStatsAttachment stats) implements CustomPacketPayload {
    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath("grimcore", "sync_stats");
    public static final Type<S2C_SyncStats> TYPE = new Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, S2C_SyncStats> STREAM_CODEC =
            StreamCodec.composite(
                    PlayerStatsAttachment.STREAM_CODEC, S2C_SyncStats::stats,
                    S2C_SyncStats::new
            );

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(S2C_SyncStats msg) {
        Minecraft.getInstance().execute(() -> {
            var player = Minecraft.getInstance().player;
            if (player != null) {
                player.setData(ModAttachments.PLAYER_STATS.get(), msg.stats);
            }
        });
    }
}


