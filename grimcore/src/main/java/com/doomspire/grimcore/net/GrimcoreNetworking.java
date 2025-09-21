package com.doomspire.grimcore.net;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.minecraft.server.level.ServerPlayer;

public final class GrimcoreNetworking {
    public static final String MODID = "doomspire";

    private GrimcoreNetworking() {}

    public static void init(IEventBus modBus) {
        modBus.addListener(GrimcoreNetworking::onRegister);
    }

    private static void onRegister(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("grimcore").versioned("1");
        registrar.playToClient(
                S2C_SyncStats.TYPE,
                S2C_SyncStats.STREAM_CODEC,
                (msg, ctx) -> S2C_SyncStats.handle(msg)
        );
    }

    public static void sendToPlayer(ServerPlayer player, Object payload) {
        if (payload instanceof S2C_SyncStats p) {
            player.connection.send(p);
        }
        // сюда будем добавлять другие пакеты по мере появления
    }
}
