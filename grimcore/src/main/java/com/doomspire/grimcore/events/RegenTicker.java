package com.doomspire.grimcore.events;

import com.doomspire.grimcore.attach.PlayerStatsAttachment;
import com.doomspire.grimcore.net.GrimcoreNetworking;
import com.doomspire.grimcore.stat.ModAttachments;
import com.doomspire.grimcore.stat.StatSnapshot;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Серверная регенерация HP/MP с аккумуляторами.
 * - читает РЕАЛЬНЫЕ значения из снапшота с АФФИКСАМИ
 * - применяет целые очки, дробную часть копит
 * - клампит по НОВЫМ капам из снапшота
 * - синкает на клиент не чаще, чем раз в 10 тиков и только при изменениях
 */
public final class RegenTicker {
    private RegenTicker(){}

    private static final class Accum {
        double hpFrac = 0.0;
        double mpFrac = 0.0;
        int    lastSyncedHp  = Integer.MIN_VALUE;
        int    lastSyncedMp  = Integer.MIN_VALUE;
        int    lastSyncedMaxHp = Integer.MIN_VALUE;
        int    lastSyncedMaxMp = Integer.MIN_VALUE;
        long   lastSyncGameTime = 0L;
    }

    private static final Map<UUID, Accum> ACCUMS = new HashMap<>();
    private static final int SYNC_COOLDOWN_TICKS = 10;

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post e) {
        if (!(e.getEntity() instanceof ServerPlayer sp)) return;
        var level = sp.serverLevel();

        PlayerStatsAttachment att = sp.getData(ModAttachments.PLAYER_STATS.get());
        if (att == null) return;

        // ВАЖНО: читаем РЕАЛЬНЫЙ снапшот (база + аффиксы).
        // Метод сам дешево вернет кеш, если dirty=false.
        StatSnapshot snap = att.getSnapshotWithAffixes(sp);

        // Капы → int
        int maxHp = (int) Math.max(1, Math.floor(snap.maxHealth));
        int maxMp = (int) Math.max(1, Math.floor(snap.maxMana));

        // реген/сек из снапшота → реген/тик
        double hpPerTick = Math.max(0.0, snap.regenHealth) / 20.0;
        double mpPerTick = Math.max(0.0, snap.regenMana)   / 20.0;

        var a = ACCUMS.computeIfAbsent(sp.getUUID(), k -> new Accum());
        a.hpFrac += hpPerTick;
        a.mpFrac += mpPerTick;

        int curHp = att.getCurrentHealth();
        int curMp = att.getCurrentMana();

        int hpGainWhole = (int) Math.floor(a.hpFrac);
        int mpGainWhole = (int) Math.floor(a.mpFrac);

        boolean changed = false;

        if (hpGainWhole > 0 && curHp < maxHp) {
            int allowed = Math.min(hpGainWhole, maxHp - curHp);
            if (allowed > 0) {
                a.hpFrac -= allowed;              // списываем ровно применённую целую часть
                att.setCurrentHealth(curHp + allowed);
                curHp = att.getCurrentHealth();
                changed = true;
            }
        }
        if (mpGainWhole > 0 && curMp < maxMp) {
            int allowed = Math.min(mpGainWhole, maxMp - curMp);
            if (allowed > 0) {
                a.mpFrac -= allowed;
                att.setCurrentMana(curMp + allowed);
                curMp = att.getCurrentMana();
                changed = true;
            }
        }

        // Синк: при изменении текущих значений ИЛИ при изменении капов; с кулдауном
        boolean capsChanged = (maxHp != a.lastSyncedMaxHp) || (maxMp != a.lastSyncedMaxMp);
        boolean hpChanged   = (curHp != a.lastSyncedHp);
        boolean mpChanged   = (curMp != a.lastSyncedMp);

        long now = level.getGameTime();
        if ((capsChanged || hpChanged || mpChanged) && now - a.lastSyncGameTime >= SYNC_COOLDOWN_TICKS) {
            GrimcoreNetworking.syncPlayerStats(sp, att);
            a.lastSyncedHp    = curHp;
            a.lastSyncedMp    = curMp;
            a.lastSyncedMaxHp = maxHp;
            a.lastSyncedMaxMp = maxMp;
            a.lastSyncGameTime = now;
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent e) {
        if (e.getEntity() != null) {
            ACCUMS.remove(e.getEntity().getUUID());
        }
    }
}
