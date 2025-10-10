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

        // Пересчёт "база+аффиксы" на сервере
        stats.markDirty();
        StatSnapshot snap = stats.getSnapshotWithAffixes(player);

        // Первичная инициализация/клампы текущих ресурсов под новые капы
        int maxHp = (int) Math.max(1, Math.floor(snap.maxHealth));
        int maxMp = (int) Math.max(1, Math.floor(snap.maxMana));
        if (stats.getCurrentHealth() <= 0) stats.setCurrentHealth(maxHp);
        else if (stats.getCurrentHealth() > maxHp) stats.setCurrentHealth(maxHp);

        if (stats.getCurrentMana() <= 0) stats.setCurrentMana(maxMp);
        else if (stats.getCurrentMana() > maxMp) stats.setCurrentMana(maxMp);

        // Синк клиенту → HUD обновится
        GrimcoreNetworking.syncPlayerStats(player, stats);

        // Применяем эффекты к ваниле (например, скорость бега)
        StatEffects.applyAll(player);
    }

    /** Любая смена ванильной экипировки: броня, мейн/оффхэнд. */
    @SubscribeEvent
    public static void onEquipChange(LivingEquipmentChangeEvent e) {
        if (!(e.getEntity() instanceof ServerPlayer player)) return;

        PlayerStatsAttachment att = player.getData(ModAttachments.PLAYER_STATS.get());
        if (att == null) return;

        // 1) форсим пересчёт "база+аффиксы"
        att.markDirty();
        StatSnapshot snap = att.getSnapshotWithAffixes(player);

        // 2) клампы на СЕРВЕРЕ (важно: int!)
        int maxHp = (int) Math.max(1, Math.floor(snap.maxHealth));
        int maxMp = (int) Math.max(1, Math.floor(snap.maxMana));
        if (att.getCurrentHealth() > maxHp) att.setCurrentHealth(maxHp);
        if (att.getCurrentMana()   > maxMp) att.setCurrentMana(maxMp);

        // 3) применяем мост к ванили (скорость и т.п.)
        StatEffects.applyAll(player);

        // 4) один пакет синка ПОСЛЕ всего
        GrimcoreNetworking.syncPlayerStats(player, att);
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        PlayerStatsAttachment stats = player.getData(ModAttachments.PLAYER_STATS.get());
        if (stats == null) return;

        // После респауна пересчитываем "база+аффиксы", т.к. Entity новый
        stats.markDirty();
        StatSnapshot snap = stats.getSnapshotWithAffixes(player);

        // Восстанавливаем/клампим ресурсы под новые капы
        int maxHp = (int) Math.max(1, Math.floor(snap.maxHealth));
        int maxMp = (int) Math.max(1, Math.floor(snap.maxMana));
        if (stats.getCurrentHealth() > maxHp) stats.setCurrentHealth(maxHp);
        if (stats.getCurrentMana()   > maxMp) stats.setCurrentMana(maxMp);

        // Синк для HUD
        GrimcoreNetworking.syncPlayerStats(player, stats);

        // Пере-применяем эффекты к ванильным атрибутам
        StatEffects.applyAll(player);
    }
}


