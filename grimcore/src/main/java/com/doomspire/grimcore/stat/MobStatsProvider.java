// src/main/java/com/doomspire/grimcore/stats/MobStatsProvider.java
package com.doomspire.grimcore.stat;

import com.doomspire.grimcore.attach.MobStatsAttachment;

import com.doomspire.grimcore.stats.ModAttachments;
import net.minecraft.world.entity.LivingEntity;

public class MobStatsProvider {

    public static MobStatsAttachment get(LivingEntity mob) {
        return mob.getData(ModAttachments.MOB_STATS.get());
    }

    public static void set(LivingEntity mob, MobStatsAttachment att) {
        mob.setData(ModAttachments.MOB_STATS.get(), att);
    }

    public static void damage(LivingEntity mob, int amount) {
        MobStatsAttachment att = get(mob);
        if (att == null) return;
        att.setCurrentHealth(att.getCurrentHealth() - Math.max(0, amount));
        att.markDirty();
    }

    public static void heal(LivingEntity mob, int amount) {
        MobStatsAttachment att = get(mob);
        if (att == null) return;
        att.setCurrentHealth(att.getCurrentHealth() + Math.max(0, amount));
        att.markDirty();
    }
}

