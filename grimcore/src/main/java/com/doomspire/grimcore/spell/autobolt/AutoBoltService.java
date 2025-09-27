package com.doomspire.grimcore.spell.autobolt;

import com.doomspire.grimcore.attach.PlayerStatsAttachment;
import com.doomspire.grimcore.net.GrimcoreNetworking;
import com.doomspire.grimcore.spell.autobolt.AutoBoltResult;
import com.doomspire.grimcore.spell.autobolt.AutoBoltTuning;
import com.doomspire.grimcore.stat.ModAttachments;
import com.doomspire.grimcore.stat.StatSnapshot;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * Ядро авто-атаки посоха: валидация/баланс/списание ресурсов.
 * НИЧЕГО не спавним и не играем звуки — это задача grimfate.
 */
public final class AutoBoltService {

    public static AutoBoltResult computeAndConsume(ServerPlayer sp, ItemStack usedStaff) {
        // 1) Достаём статы игрока и их снапшот
        PlayerStatsAttachment stats = sp.getData(ModAttachments.PLAYER_STATS.get());
        if (stats == null) return AutoBoltResult.denied("no_stats");

        StatSnapshot snap = stats.getSnapshot(); // кэшируемый снапшот из аттача

        // 2) Проверяем кулдаун именно на ЭТОТ предмет (пер-айтем)
        if (sp.getCooldowns().isOnCooldown(usedStaff.getItem())) {
            return AutoBoltResult.denied("cooldown");
        }

        // 3) Тюнинг авто-болта из datapack'а (mana cost / cd / projectile speed)
        AutoBoltTuning tuning = AutoBoltTuning.get();

        int manaCost = tuning.manaCost(snap, usedStaff);
        if (stats.getCurrentMana() < manaCost) {
            return AutoBoltResult.denied("not_enough_mana");
        }

        int cdTicks = tuning.cooldownTicks(snap, usedStaff);
        float projSpeed = tuning.projectileSpeed(snap, usedStaff);

        // 4) Списываем ману и синкаем
        stats.setCurrentMana(stats.getCurrentMana() - manaCost);
        stats.markDirty();
        GrimcoreNetworking.syncPlayerStats(sp, stats);

        // 5) Возвращаем параметры для контент-слоя
        return AutoBoltResult.ok(cdTicks, projSpeed);
    }

    private AutoBoltService() {}
}
