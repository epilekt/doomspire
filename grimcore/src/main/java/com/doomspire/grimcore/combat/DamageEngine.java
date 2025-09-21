package com.doomspire.grimcore.combat;

import com.doomspire.grimcore.attach.MobStatsAttachment;
import com.doomspire.grimcore.attach.PlayerStatsAttachment;
import com.doomspire.grimcore.net.GrimcoreNetworking;
import com.doomspire.grimcore.stat.DamageTypes;
import com.doomspire.grimcore.stat.ResistTypes;
import com.doomspire.grimcore.stat.StatSnapshot;
import com.doomspire.grimcore.stat.ModAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

import java.util.concurrent.ThreadLocalRandom;

public final class DamageEngine {
    private DamageEngine() {}

    public static float resolveAndApply(DamageContext ctx) {
        final LivingEntity target = ctx.target;

        // --- читаем снапшот цели (игрок или моб) ---
        StatSnapshot tSnap;
        boolean targetIsPlayer = target.getData(ModAttachments.PLAYER_STATS.get()) != null;
        if (targetIsPlayer) {
            PlayerStatsAttachment tAtt = target.getData(ModAttachments.PLAYER_STATS.get());
            if (tAtt == null) return 0f;
            tSnap = tAtt.getSnapshot();
            // Evade
            if (ThreadLocalRandom.current().nextFloat() < tSnap.evasionChance) {
                return 0f;
            }
        } else {
            MobStatsAttachment tAtt = target.getData(ModAttachments.MOB_STATS.get());
            if (tAtt == null) return 0f;
            tSnap = tAtt.getSnapshot();
            if (ThreadLocalRandom.current().nextFloat() < tSnap.evasionChance) {
                return 0f;
            }
        }

        // --- снапшот атакера (для крита/воровства) ---
        StatSnapshot aSnap = null;
        if (ctx.attacker != null) {
            var aPlayer = ctx.attacker.getData(ModAttachments.PLAYER_STATS.get());
            if (aPlayer != null) aSnap = aPlayer.getSnapshot();
            else {
                var aMob = ctx.attacker.getData(ModAttachments.MOB_STATS.get());
                if (aMob != null) aSnap = aMob.getSnapshot();
            }
        }

        float total = 0f;

        for (var entry : ctx.damageMap.entrySet()) {
            DamageTypes type = entry.getKey();
            float dmg = entry.getValue();
            if (dmg <= 0f) continue;

            // crit
            if (aSnap != null) {
                boolean rollCrit = ctx.critical || ThreadLocalRandom.current().nextFloat() < aSnap.critChance;
                if (rollCrit) {
                    dmg *= (1f + Math.max(0f, aSnap.critDamage)); // +50% => 0.5
                }
            }

            // resist mapping
            float resist = switch (type) {
                case PHYS_MELEE, PHYS_RANGED -> tSnap.resistances.getOrDefault(ResistTypes.PHYS, 0f);
                case FIRE -> tSnap.resistances.getOrDefault(ResistTypes.FIRE, 0f);
                case FROST -> tSnap.resistances.getOrDefault(ResistTypes.FROST, 0f);
                case LIGHTNING -> tSnap.resistances.getOrDefault(ResistTypes.LIGHTNING, 0f);
                case POISON -> tSnap.resistances.getOrDefault(ResistTypes.POISON, 0f);
            };
            resist = Math.max(0f, Math.min(resist, 0.90f)); // хард-кап 90%
            dmg *= (1f - resist);

            total += Math.max(0f, dmg);
        }

        // lifesteal/manasteal по суммарному урону
        if (aSnap != null && total > 0f && ctx.attacker instanceof LivingEntity attackerLe) {
            int heal = Math.round(total * Math.max(0f, aSnap.lifesteal));
            int mana = Math.round(total * Math.max(0f, aSnap.manasteal));

            var aPlayer = attackerLe.getData(ModAttachments.PLAYER_STATS.get());
            if (aPlayer != null) {
                boolean changed = false;
                if (heal > 0) {
                    aPlayer.setCurrentHealth(aPlayer.getCurrentHealth() + heal);
                    changed = true;
                }
                if (mana > 0) {
                    aPlayer.setCurrentMana(aPlayer.getCurrentMana() + mana);
                    changed = true;
                }
                if (changed) {
                    aPlayer.markDirty();
                    if (attackerLe instanceof ServerPlayer spA) {
                        GrimcoreNetworking.syncPlayerStats(spA, aPlayer);
                    }
                }
            } else {
                var aMob = attackerLe.getData(ModAttachments.MOB_STATS.get());
                if (aMob != null && heal > 0) {
                    aMob.setCurrentHealth(aMob.getCurrentHealth() + heal);
                    aMob.markDirty();
                }
            }
        }

        // применяем к цели
        int applied = Math.max(0, Math.round(total));
        if (applied > 0) {
            if (targetIsPlayer) {
                PlayerStatsAttachment tAtt = target.getData(ModAttachments.PLAYER_STATS.get());
                tAtt.setCurrentHealth(tAtt.getCurrentHealth() - applied);
                tAtt.markDirty();
                if (target instanceof ServerPlayer spT) {
                    GrimcoreNetworking.syncPlayerStats(spT, tAtt); // мгновенный HUD-синк цели
                }
            } else {
                MobStatsAttachment tAtt = target.getData(ModAttachments.MOB_STATS.get());
                tAtt.setCurrentHealth(tAtt.getCurrentHealth() - applied);
                tAtt.markDirty();
            }
        }

        return applied;
    }
}
