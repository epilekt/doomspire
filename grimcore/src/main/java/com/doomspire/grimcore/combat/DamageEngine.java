package com.doomspire.grimcore.combat;

import com.doomspire.grimcore.attach.PlayerStatsAttachment;
import com.doomspire.grimcore.stat.*;
import com.doomspire.grimcore.stats.ModAttachments;
import net.minecraft.world.entity.LivingEntity;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Центральный движок расчёта урона.
 */
public final class DamageEngine {
    private DamageEngine() {}

    public static float resolveAndApply(DamageContext ctx) {
        final LivingEntity target = ctx.target;
        final PlayerStatsAttachment tAtt = target.getData(ModAttachments.PLAYER_STATS.get());
        final StatSnapshot tSnap = tAtt.getSnapshot();

        // 1) Evade
        if (ThreadLocalRandom.current().nextFloat() < tSnap.evasionChance) {
            return 0f;
        }

        float total = 0f;

        for (var e : ctx.damageMap.entrySet()) {
            final DamageTypes dt = e.getKey();
            float dmg = e.getValue();
            if (dmg <= 0f) continue;

            // 2) Attr scaling — пока базово: dmg уже посчитан источником

            // 3) Crit (шанс/множитель берём у АТАКУЮЩЕГО позже — на сейчас читаем из цели как заглушку)
            if (ThreadLocalRandom.current().nextFloat() < tSnap.critChance) {
                ctx.critical = true;
                dmg *= (1f + tSnap.critDamage);
            }

            // 4) Resist
            final ResistTypes rt = mapResist(dt);
            float resist = tSnap.resistances.getOrDefault(rt, 0f);
            dmg *= (1f - resist);

            // 5) Block (phys only)
            if (rt == ResistTypes.PHYS && target.isBlocking()) {
                // простой вариант: повторно применяем физ-редукцию цели
                dmg *= (1f - tSnap.resistances.getOrDefault(ResistTypes.PHYS, 0f));
            }

            total += Math.max(0f, dmg);
        }

        // 6) Apply to custom HP
        int hp = tAtt.getCurrentHealth();
        tAtt.setCurrentHealth(hp - Math.round(total));
        tAtt.markDirty();

        return total;
    }

    private static ResistTypes mapResist(DamageTypes t) {
        return switch (t) {
            case PHYS_MELEE, PHYS_RANGED -> ResistTypes.PHYS;
            case FIRE -> ResistTypes.FIRE;
            case FROST -> ResistTypes.FROST;
            case LIGHTNING -> ResistTypes.LIGHTNING;
            case POISON -> ResistTypes.POISON;
        };
    }
}

