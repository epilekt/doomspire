package com.doomspire.grimcore.events;

import com.doomspire.grimcore.attach.MobStatsAttachment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

/**
 * Заглушка-провайдер пер-мобных оверрайдов.
 * На следующем шаге подменим на чтение JSON из datapack.
 */
public final class MobTuning {
    private MobTuning(){}

    public static void applyPerEntityOverrides(ServerLevel level, LivingEntity mob, MobStatsAttachment att) {
        ResourceLocation id = mob.getType().builtInRegistryHolder().key().location();
        // Пример: зомби — пожирнее, паук — ловчее
        if ("minecraft".equals(id.getNamespace()) && "zombie".equals(id.getPath())) {
            att.addAttribute(com.doomspire.grimcore.stat.Attributes.VITALITY, 3);
            att.addAttribute(com.doomspire.grimcore.stat.Attributes.STRENGTH, 2);
        } else if ("minecraft".equals(id.getNamespace()) && "spider".equals(id.getPath())) {
            att.addAttribute(com.doomspire.grimcore.stat.Attributes.EVASION, 5);
            att.addAttribute(com.doomspire.grimcore.stat.Attributes.DEXTERITY, 3);
        }
        att.markDirty();
    }
}

