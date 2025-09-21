package com.doomspire.grimcore.events;

import com.doomspire.grimcore.attach.PlayerStatsAttachment;
import com.doomspire.grimcore.net.GrimcoreNetworking;
import com.doomspire.grimcore.stat.ModAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public final class CorePlayerEvents {
    private CorePlayerEvents() {}

    /** Зарегистрируй где-то в инициализации: NeoForge.EVENT_BUS.register(CorePlayerEvents.class); */
    public static void registerToBus() {
        NeoForge.EVENT_BUS.register(CorePlayerEvents.class);
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        PlayerStatsAttachment stats = player.getData(ModAttachments.PLAYER_STATS.get());
        if (stats == null) return;

        // инициализация текущих ресурсов при первом входе
        var snap = stats.getSnapshot();
        if (stats.getCurrentHealth() <= 0) stats.setCurrentHealth((int) snap.maxHealth);
        if (stats.getCurrentMana()   <= 0) stats.setCurrentMana((int) snap.maxMana);
        stats.markDirty();

        // мгновенно синканём, чтобы HUD сразу показал актуальные полоски
        GrimcoreNetworking.syncPlayerStats(player, stats);
    }
}


