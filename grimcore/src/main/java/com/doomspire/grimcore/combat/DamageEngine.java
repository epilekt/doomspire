package com.doomspire.grimcore.combat;

import com.doomspire.grimcore.attach.MobStatsAttachment;
import com.doomspire.grimcore.attach.PlayerStatsAttachment;
import com.doomspire.grimcore.net.GrimcoreNetworking;
import com.doomspire.grimcore.stat.DamageTypes;
import com.doomspire.grimcore.stat.ModAttachments;
import com.doomspire.grimcore.stat.ResistTypes;
import com.doomspire.grimcore.stat.StatSnapshot;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

import java.util.concurrent.ThreadLocalRandom;

//NOTE: Централизованный пайплайн урона - задаем игре последовательность расчета урона от каждого hit

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

        // --- базовый расчёт по типам: крит -> резисты ---
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

        int applied; // фактически нанесённый урон (после overshield и DR_all)

        // --- overshield (только у игрока) -> затем damage_reduction_all ---
        if (targetIsPlayer) {
            PlayerStatsAttachment tAtt = target.getData(ModAttachments.PLAYER_STATS.get());
            int incoming = Math.max(0, Math.round(total));

            // 1) Снять overshield ПЕРВЫМ
            int afterOS = tAtt.consumeOvershield(incoming);
            boolean osChanged = (afterOS != incoming);

            // 2) Применить глобальную редукцию
            float drAll = clamp01(readDamageReductionAll(tSnap), 0.90f);
            float afterDR = afterOS * (1f - drAll);

            applied = Math.max(0, Math.round(afterDR));

            // 3) Урон по здоровью
            if (applied > 0) {
                tAtt.setCurrentHealth(tAtt.getCurrentHealth() - applied);
            }

            if (osChanged || applied > 0) {
                tAtt.markDirty();
                if (target instanceof ServerPlayer spT) {
                    GrimcoreNetworking.syncPlayerStats(spT, tAtt); // мгновенный HUD-синк цели
                }
            }
        } else {
            // Мобы: только DR_all (если в снапшоте есть), без overshield
            float drAll = clamp01(readDamageReductionAll(tSnap), 0.90f);
            float afterDR = total * (1f - drAll);
            applied = Math.max(0, Math.round(afterDR));

            if (applied > 0) {
                MobStatsAttachment tAtt = target.getData(ModAttachments.MOB_STATS.get());
                tAtt.setCurrentHealth(tAtt.getCurrentHealth() - applied);
                tAtt.markDirty();
            }
        }

        // --- lifesteal/manasteal по фактически нанесённому урону ---
        if (aSnap != null && applied > 0 && ctx.attacker instanceof LivingEntity attackerLe) {
            int heal = Math.round(applied * Math.max(0f, aSnap.lifesteal));
            int mana = Math.round(applied * Math.max(0f, aSnap.manasteal));

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

        return applied;
    }

    // ===== helpers =====

    /**
     * Чтение глобальной редукции урона. До того как поле появится в StatSnapshot,
     * метод вернёт 0f, чтобы не ломать компиляцию.
     */
    private static float readDamageReductionAll(StatSnapshot snap) {
        try {
            // Публичное поле
            var f = snap.getClass().getField("damageReductionAll");
            Object v = f.get(snap);
            if (v instanceof Number n) return n.floatValue();
        } catch (Throwable ignored) {
            // попробуем геттер
            try {
                var m = snap.getClass().getMethod("damageReductionAll");
                Object v = m.invoke(snap);
                if (v instanceof Number n) return n.floatValue();
            } catch (Throwable ignored2) {
                // поля нет — используем 0
            }
        }
        return 0f;
    }

    private static float clamp01(float v, float hardCap) {
        if (Float.isNaN(v) || Float.isInfinite(v)) return 0f;
        if (hardCap <= 0f) hardCap = 1f;
        return Math.max(0f, Math.min(v, hardCap));
    }
}
