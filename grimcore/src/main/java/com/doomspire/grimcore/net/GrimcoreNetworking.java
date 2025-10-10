package com.doomspire.grimcore.net;

import com.doomspire.grimcore.Grimcore;
import com.doomspire.grimcore.attach.PlayerStatsAttachment;
import com.doomspire.grimcore.stat.ModAttachments;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class GrimcoreNetworking {
    private GrimcoreNetworking() {}

    public static void register(final RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar(Grimcore.MODID);

        registrar.playToClient(
                S2C_SyncPlayerStats.TYPE,
                S2C_SyncPlayerStats.STREAM_CODEC,
                (payload, ctx) -> {
                    var mc = Minecraft.getInstance();
                    Player clientPlayer = mc.player;
                    if (clientPlayer == null) return;

                    // Положили свежий attachment от сервера
                    clientPlayer.setData(ModAttachments.PLAYER_STATS.get(), payload.att());

                    // ВАЖНО: прямо сейчас пересчитываем снапшот с аффиксами на клиенте,
                    // чтобы HUD видел новые maxHealth/maxMana немедленно.
                    var stats = clientPlayer.getData(ModAttachments.PLAYER_STATS.get());
                    if (stats != null) {
                        stats.markDirty();
                        stats.getSnapshotWithAffixes(clientPlayer);
                    }
                }
        );
    }

    /** Вызывать на сервере после изменения статов, чтобы HUD сразу обновился. */
    public static void syncPlayerStats(ServerPlayer target, PlayerStatsAttachment att) {
        PacketDistributor.sendToPlayer(target, new S2C_SyncPlayerStats(att));
    }

    // -------- payload (CLIENT-bound) --------
    public record S2C_SyncPlayerStats(PlayerStatsAttachment att) implements CustomPacketPayload {
        public static final Type<S2C_SyncPlayerStats> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(Grimcore.MODID, "sync_player_stats"));

        public static final StreamCodec<RegistryFriendlyByteBuf, S2C_SyncPlayerStats> STREAM_CODEC =
                StreamCodec.composite(
                        PlayerStatsAttachment.STREAM_CODEC, S2C_SyncPlayerStats::att,
                        S2C_SyncPlayerStats::new
                );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
}
