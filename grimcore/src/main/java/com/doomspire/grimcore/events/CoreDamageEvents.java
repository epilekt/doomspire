package com.doomspire.grimcore.events;

import com.doomspire.grimcore.stats.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

public final class CoreDamageEvents {

    private CoreDamageEvents() {}

    /** Регистрируй из инициализации (например, из Grimfate entrypoint): NeoForge.EVENT_BUS.register(CoreDamageEvents.class); */
    public static void registerToBus() {
        NeoForge.EVENT_BUS.register(CoreDamageEvents.class);
    }

    /**
     * Ядро обработки урона. Работает на LOWEST, чтобы контент мог скорректировать event.setNewDamage(...) раньше.
     * Никакой предметной/спелл-логики здесь нет.
     */
    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Pre event) {
        LivingEntity living = event.getEntity();

        if (living.level().isClientSide()) return;
        if (!living.isAlive()) return;

        float amountAfterContent = event.getNewDamage(); // учитываем возможные правки контента
        if (amountAfterContent <= 0f) return;

        Entity src = event.getSource() != null ? event.getSource().getEntity() : null;

        // ===== Игрок как цель =====
        if (living instanceof ServerPlayer serverPlayer) {
            PlayerStats stats = PlayerStatsProvider.get(serverPlayer);
            if (stats == null) return;

            int damage = CoreDamageCalculator.calculateForPlayer(serverPlayer, event.getSource(), amountAfterContent, stats);

            var m = PlayerStatsProvider.getMutable(serverPlayer);
            int newHp = Math.max(0, m.health - damage);
            m.setHealth(newHp);

            // Гасим ванильный урон
            event.setNewDamage(0f);

            // Немедленный коммит, чтобы HUD увидел новое значение
            PlayerStatsProvider.commitIfDirty(serverPlayer);

            // (опционально) короткие системные сообщения — оставляем, но можно убрать/переключить в конфиг
            PlayerStats afterStats = PlayerStatsProvider.get(serverPlayer);
            if (afterStats != null) {
                serverPlayer.sendSystemMessage(Component.literal(
                        "⚡ Получено " + damage + " урона. HP: " +
                                afterStats.health() + "/" + afterStats.maxHealth()
                ));
            }

            // Смерть через ванильный механизм
            if (m.health <= 0) {
                killByGeneric(serverPlayer);
            }
            return;
        }

        // ===== Мобы =====
        MobStats before = MobStatsProvider.get(living);
        if (before == null) return;

        int damage = CoreDamageCalculator.calculateForMob(living, event.getSource(), amountAfterContent, before);
        MobStatsProvider.damage(living, damage);

        // Гасим ванильный урон
        event.setNewDamage(0f);

        MobStats after = MobStatsProvider.get(living);

        if (after != null && after.getCurrentHealth() <= 0) {
            killByGeneric(living);
            // ВАЖНО: выдача XP/лут — это контент. Делай это в grimfate через отдельный хук (например, LivingDeathEvent)
        }
    }

    private static void killByGeneric(LivingEntity entity) {
        entity.setHealth(0f);
        DamageSource outOfWorld = new DamageSource(
                entity.level().registryAccess()
                        .registryOrThrow(Registries.DAMAGE_TYPE)
                        .getHolderOrThrow(DamageTypes.GENERIC_KILL)
        );
        entity.hurt(outOfWorld, Float.MAX_VALUE);
    }
}

