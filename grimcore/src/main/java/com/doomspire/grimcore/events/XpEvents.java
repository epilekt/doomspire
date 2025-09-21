package com.doomspire.grimcore.events;

import com.doomspire.grimcore.attach.MobStatsAttachment;
import com.doomspire.grimcore.attach.PlayerProgressAttachment;
import com.doomspire.grimcore.attach.PlayerStatsAttachment;
import com.doomspire.grimcore.net.GrimcoreNetworking;
import com.doomspire.grimcore.net.ProgressNetworking;
import com.doomspire.grimcore.stat.ModAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

public final class XpEvents {
    private XpEvents(){}

    public static void registerToBus() {
        NeoForge.EVENT_BUS.register(XpEvents.class);
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide()) return;

        LivingEntity dead = event.getEntity();
        if (dead instanceof ServerPlayer) return;

        ServerPlayer killer = null;
        if (event.getSource() != null && event.getSource().getEntity() instanceof ServerPlayer sp) {
            killer = sp;
        } else if (dead.getKillCredit() instanceof ServerPlayer sp2) {
            killer = sp2;
        }
        if (killer == null) return;

        // --- вычисляем "цену" моба ---
        int maxHp;
        MobStatsAttachment mobAtt = dead.getData(ModAttachments.MOB_STATS.get());
        if (mobAtt != null) {
            maxHp = Math.max(1, (int) mobAtt.getSnapshot().maxHealth);
        } else {
            maxHp = Math.max(1, (int) Math.ceil(dead.getMaxHealth()));
        }
        int xp = Math.max(1, Math.round((float) Math.pow(maxHp, 0.80) * 4f));

        // --- применяем на игроке ---
        PlayerProgressAttachment prog = killer.getData(ModAttachments.PLAYER_PROGRESS.get());
        PlayerStatsAttachment stats   = killer.getData(ModAttachments.PLAYER_STATS.get());
        if (prog == null || stats == null) return;

        int levels = prog.addExp(xp);

        if (levels > 0) {
            stats.addUnspentPoints(levels); // выдаём очки за ап-левел
            stats.markDirty();
            GrimcoreNetworking.syncPlayerStats(killer, stats); // мгновенно обновим HUD (очки можно показывать)
        }

        // прогресс: мгновенный клиентский синк (HUD увидит exp/level/cap)
        ProgressNetworking.syncPlayerProgress(killer, prog);
    }
}


