package com.doomspire.grimcore.net;

import com.doomspire.grimcore.Grimcore;
import com.doomspire.grimcore.attach.PlayerProgressAttachment;
import com.doomspire.grimcore.stat.ModAttachments;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class ProgressNetworking {
    private ProgressNetworking(){}

    public static void register(final RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar(Grimcore.MODID);
        registrar.playToClient(
                S2C_SyncPlayerProgress.TYPE,
                S2C_SyncPlayerProgress.STREAM_CODEC,
                (payload, ctx) -> {
                    var player = Minecraft.getInstance().player;
                    if (player != null) {
                        player.setData(ModAttachments.PLAYER_PROGRESS.get(), payload.att());
                    }
                }
        );
    }

    public static void syncPlayerProgress(ServerPlayer target, PlayerProgressAttachment att) {
        PacketDistributor.sendToPlayer(target, new S2C_SyncPlayerProgress(att));
    }

    public record S2C_SyncPlayerProgress(PlayerProgressAttachment att) implements CustomPacketPayload {
        public static final Type<S2C_SyncPlayerProgress> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(Grimcore.MODID, "sync_player_progress"));

        public static final StreamCodec<RegistryFriendlyByteBuf, S2C_SyncPlayerProgress> STREAM_CODEC =
                StreamCodec.composite(
                        PlayerProgressAttachment.STREAM_CODEC, S2C_SyncPlayerProgress::att,
                        S2C_SyncPlayerProgress::new
                );

        @Override
        public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }
}


