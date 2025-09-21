// src/main/java/com/doomspire/grimcore/events/CoreDamageEvents.java
package com.doomspire.grimcore.events;

import com.doomspire.grimcore.attach.MobStatsAttachment;
import com.doomspire.grimcore.combat.DamageContext;
import com.doomspire.grimcore.combat.DamageEngine;
import com.doomspire.grimcore.combat.EnvironmentalDamage;
import com.doomspire.grimcore.stat.DamageTypes;
import com.doomspire.grimcore.stats.ModAttachments;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

public final class CoreDamageEvents {

    private CoreDamageEvents() {}

    /** Регистрируй из инициализации: NeoForge.EVENT_BUS.register(CoreDamageEvents.class); */
    public static void registerToBus() {
        NeoForge.EVENT_BUS.register(CoreDamageEvents.class);
    }

    /**
     * Ядро обработки урона. Никакой предметной/спелл-логики здесь нет.
     * Сначала экологический урон (% от MaxHP), затем боевой пайплайн.
     */
    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Pre event) {
        LivingEntity living = event.getEntity();
        if (living.level().isClientSide() || !living.isAlive()) return;

        // ---------- Экологический урон как % MaxHP ----------
        Float pct = EnvironmentalDamage.percentFor(event.getSource());
        if (pct != null) {
            if (living instanceof ServerPlayer sp) {
                var ps = sp.getData(ModAttachments.PLAYER_STATS.get());
                if (ps != null) {
                    int max = (int) Math.max(1, ps.getSnapshot().maxHealth);
                    ps.setCurrentHealth(ps.getCurrentHealth() - Math.max(1, Math.round(max * pct)));
                    ps.markDirty();
                    event.setNewDamage(0f);
                    if (ps.getCurrentHealth() <= 0) killByGeneric(sp);
                }
                return;
            } else {
                var ms = living.getData(ModAttachments.MOB_STATS.get());
                if (ms != null) {
                    int max = (int) Math.max(1, ms.getSnapshot().maxHealth);
                    ms.setCurrentHealth(ms.getCurrentHealth() - Math.max(1, Math.round(max * pct)));
                    ms.markDirty();
                    event.setNewDamage(0f);
                    if (ms.getCurrentHealth() <= 0) killByGeneric(living);
                }
                return;
            }
        }

        float amountAfterContent = event.getNewDamage();
        if (amountAfterContent <= 0f) return;

        Entity src = event.getSource() != null ? event.getSource().getEntity() : null;

        // ---------- Цель — игрок: расчёт через DamageEngine ----------
        if (living instanceof ServerPlayer serverPlayer) {
            LivingEntity attacker = (src instanceof LivingEntity le) ? le : null;

            DamageContext ctx = new DamageContext(attacker, serverPlayer);
            if (attacker != null) {
                // если атакует моб с кастомными статами — берём его физический урон
                MobStatsAttachment aStats = attacker.getData(ModAttachments.MOB_STATS.get());
                if (aStats != null) {
                    float phys = aStats.getSnapshot().damage.getOrDefault(DamageTypes.PHYS_MELEE, 0f);
                    ctx.add(DamageTypes.PHYS_MELEE, phys);
                } else {
                    // иначе fallback на ванильное число
                    ctx.add(DamageTypes.PHYS_MELEE, amountAfterContent);
                }
            } else {
                ctx.add(DamageTypes.PHYS_MELEE, amountAfterContent);
            }

            DamageEngine.resolveAndApply(ctx);
            event.setNewDamage(0f);

            var att = serverPlayer.getData(ModAttachments.PLAYER_STATS.get());
            if (att != null && att.getCurrentHealth() <= 0) killByGeneric(serverPlayer);
            return;
        }

        // ---------- Цель — моб: прямое применение к кастомному HP ----------
        {
            float base = amountAfterContent;
            if (src instanceof LivingEntity le) {
                MobStatsAttachment aStats = le.getData(ModAttachments.MOB_STATS.get());
                if (aStats != null) {
                    base = aStats.getSnapshot().damage.getOrDefault(DamageTypes.PHYS_MELEE, base);
                }
            }
            MobStatsAttachment tStats = living.getData(ModAttachments.MOB_STATS.get());
            if (tStats != null) {
                tStats.setCurrentHealth(tStats.getCurrentHealth() - Math.round(base));
                tStats.markDirty();
                event.setNewDamage(0f);
                if (tStats.getCurrentHealth() <= 0) killByGeneric(living);
            }
        }
    }

    private static void killByGeneric(LivingEntity entity) {
        entity.setHealth(0f);
        DamageSource genericKill = new DamageSource(
                entity.level().registryAccess()
                        .registryOrThrow(Registries.DAMAGE_TYPE)
                        .getHolderOrThrow(net.minecraft.world.damagesource.DamageTypes.GENERIC_KILL)
        );
        entity.hurt(genericKill, Float.MAX_VALUE);
    }
}


