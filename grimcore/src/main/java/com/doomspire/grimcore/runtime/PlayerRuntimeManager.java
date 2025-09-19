package com.doomspire.grimcore.runtime;

import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Простой manager для runtime-данных (сервер).
 */
public final class PlayerRuntimeManager {
    private static final ConcurrentHashMap<UUID, PlayerRuntimeData> RUNTIME = new ConcurrentHashMap<>();

    private PlayerRuntimeManager() {}

    public static PlayerRuntimeData getOrCreate(ServerPlayer player) {
        return RUNTIME.computeIfAbsent(player.getUUID(), uuid -> new PlayerRuntimeData());
    }

    public static PlayerRuntimeData get(ServerPlayer player) {
        return RUNTIME.get(player.getUUID());
    }

    public static void remove(ServerPlayer player) {
        if (player != null) RUNTIME.remove(player.getUUID());
    }

    public static void remove(UUID uuid) {
        if (uuid != null) RUNTIME.remove(uuid);
    }

    public static void clearAll() {
        RUNTIME.clear();
    }
}
