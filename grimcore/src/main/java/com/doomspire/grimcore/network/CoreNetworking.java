package com.doomspire.grimcore.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * Тонкий фасад для регистрации пакетов ядра или получения регистраторов.
 * Сам grimcore может не объявлять собственных пакетов — но даёт единый способ создать registrar.
 */
public final class CoreNetworking {

    public static final String CORE_CHANNEL = "grimcore";
    private CoreNetworking() {}

    /** Вернёт registrar для канала grimcore (если нужно регать общие пакеты ядра). */
    public static PayloadRegistrar registrar(RegisterPayloadHandlersEvent event) {
        return event.registrar(CORE_CHANNEL);
    }

    /** Хук для инициализации ядра сети (если появятся пакеты ядра — регать их здесь). */
    public static void register(RegisterPayloadHandlersEvent event) {
        // пример (оставлено пустым до появления S2C/C2S пакетов ядра)
        // var r = registrar(event);
        // r.playToClient(StatsSyncPacket.TYPE, StatsSyncPacket.STREAM_CODEC, StatsSyncPacket::handleClient);
    }
}

