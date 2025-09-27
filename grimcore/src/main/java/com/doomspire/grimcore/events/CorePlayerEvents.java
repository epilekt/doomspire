package com.doomspire.grimcore.events;

import com.doomspire.grimcore.attach.PlayerStatsAttachment;
import com.doomspire.grimcore.net.GrimcoreNetworking;
import com.doomspire.grimcore.stat.ModAttachments;
import com.doomspire.grimcore.stat.StatEffects;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public final class CorePlayerEvents {
    private CorePlayerEvents() {}

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        PlayerStatsAttachment stats = player.getData(ModAttachments.PLAYER_STATS.get());
        if (stats == null) return;

        // первичная инициализация «текущих» ресурсов
        var snap = stats.getSnapshot();
        if (stats.getCurrentHealth() <= 0) stats.setCurrentHealth((int) snap.maxHealth);
        if (stats.getCurrentMana()   <= 0) stats.setCurrentMana((int) snap.maxMana);
        stats.markDirty();

        // синк клиенту, чтобы HUD сразу обновился
        GrimcoreNetworking.syncPlayerStats(player, stats);

        // применяем ВСЕ эффекты статов к ванильным атрибутам (DEX→скорость и т.д.)
        StatEffects.applyAll(player);
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        // после респауна: повторно применяем эффекты (могли слететь с нового Entity)
        StatEffects.applyAll(player);
    }
}


