package com.doomspire.grimcore.events;

import com.doomspire.grimcore.attach.PlayerStatsAttachment;
import com.doomspire.grimcore.net.GrimcoreNetworking;
import com.doomspire.grimcore.stat.ModAttachments;
import com.doomspire.grimcore.stat.StatEffects;
import com.doomspire.grimcore.stat.StatSnapshot;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public final class CorePlayerEvents {
    private CorePlayerEvents() {}

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        PlayerStatsAttachment stats = player.getData(ModAttachments.PLAYER_STATS.get());
        if (stats == null) return;

        // первичная инициализация «текущих» ресурсов
        var base = stats.getSnapshot(); // базовый снапшот (без аффиксов)
        if (stats.getCurrentHealth() <= 0) stats.setCurrentHealth((int) base.maxHealth);
        if (stats.getCurrentMana()   <= 0) stats.setCurrentMana((int) base.maxMana);

        // ВАЖНО: полный пересчёт "база + аффиксы" прямо сейчас
        stats.markDirty();
        var snap = stats.getSnapshotWithAffixes(player);

        // клампим текущие под новые капы
        stats.setCurrentHealth(Math.min(stats.getCurrentHealth(), (int) snap.maxHealth));
        stats.setCurrentMana(Math.min(stats.getCurrentMana(),   (int) snap.maxMana));

        // синк клиенту — HUD на клиенте дальше сам пересчитает снапшот (см. патч клиента ниже)
        GrimcoreNetworking.syncPlayerStats(player, stats);

        // ванильные атрибуты (скорость и т.п.)
        StatEffects.applyAll(player);
    }

    /** Любая смена ванильной экипировки: броня, main/offhand. */
    @SubscribeEvent
    public static void onEquipChange(LivingEquipmentChangeEvent e) {
        if (!(e.getEntity() instanceof ServerPlayer sp)) return;
        PlayerStatsAttachment att = sp.getData(ModAttachments.PLAYER_STATS.get());
        if (att == null) return;

        // Полный пересчёт "база + аффиксы"
        att.markDirty();
        var snap = att.getSnapshotWithAffixes(sp);

        // Подрезаем текущие ресурсы под новые капы (если max упал)
        att.setCurrentHealth(Math.min(att.getCurrentHealth(), (int) snap.maxHealth));
        att.setCurrentMana(Math.min(att.getCurrentMana(),   (int) snap.maxMana));

        // Синхронизируем клиенту и прокидываем ванильные модификаторы
        GrimcoreNetworking.syncPlayerStats(sp, att);
        StatEffects.applyAll(sp);
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        // после респауна повторно навешиваем ванильные модификаторы
        StatEffects.applyAll(player);
    }
}


