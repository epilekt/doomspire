package com.doomspire.grimcore.events;

import com.doomspire.grimcore.attach.PlayerStatsAttachment;
import com.doomspire.grimcore.net.GrimcoreNetworking;
import com.doomspire.grimcore.stat.ModAttachments;
import com.doomspire.grimcore.stat.StatSnapshot;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Простая серверная регенерация HP/MP с аккумуляторами.
 * - считает реген каждый тик
 * - применяет целые очки, дробную часть копит
 * - синкает на клиент не чаще, чем раз в 10 тиков и только при изменениях
 */
public final class RegenTicker {
    private RegenTicker(){}

    public static void registerToBus() {
        NeoForge.EVENT_BUS.register(RegenTicker.class);
    }

    private static final class Accum {
        double hpFrac = 0.0;
        double mpFrac = 0.0;
        int    lastSyncedHp = Integer.MIN_VALUE;
        int    lastSyncedMp = Integer.MIN_VALUE;
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

        StatSnapshot snap = att.getSnapshot();
        // реген/сек из снапшота → реген/тик
        double hpPerTick = Math.max(0.0, snap.regenHealth) / 20.0;
        double mpPerTick = Math.max(0.0, snap.regenMana)   / 20.0;

        var a = ACCUMS.computeIfAbsent(sp.getUUID(), k -> new Accum());
        a.hpFrac += hpPerTick;
        a.mpFrac += mpPerTick;

        int hpGain = (int) Math.floor(a.hpFrac);
        int mpGain = (int) Math.floor(a.mpFrac);
        if (hpGain != 0 || mpGain != 0) {
            a.hpFrac -= hpGain;
            a.mpFrac -= mpGain;

            // применяем к текущим ресурсам
            int beforeHp = att.getCurrentHealth();
            int beforeMp = att.getCurrentMana();
            att.setCurrentHealth(beforeHp + hpGain);
            att.setCurrentMana(beforeMp + mpGain);
            att.markDirty();

            // редкий синк: только если значения изменились и прошло >= cooldown
            boolean hpChanged = att.getCurrentHealth() != a.lastSyncedHp;
            boolean mpChanged = att.getCurrentMana()   != a.lastSyncedMp;
            long now = level.getGameTime();
            if ((hpChanged || mpChanged) && now - a.lastSyncGameTime >= SYNC_COOLDOWN_TICKS) {
                GrimcoreNetworking.syncPlayerStats(sp, att);
                a.lastSyncedHp = att.getCurrentHealth();
                a.lastSyncedMp = att.getCurrentMana();
                a.lastSyncGameTime = now;
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent e) {
        if (e.getEntity() != null) {
            ACCUMS.remove(e.getEntity().getUUID());
        }
    }
}
