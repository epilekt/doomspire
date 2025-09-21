package com.doomspire.grimcore.events;

import com.doomspire.grimcore.attach.MobStatsAttachment;
import com.doomspire.grimcore.stat.ModAttachments;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

public final class MobSpawnInit {
    private MobSpawnInit(){}

    public static void registerToBus() { NeoForge.EVENT_BUS.register(MobSpawnInit.class); }

    @SubscribeEvent
    public static void onJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getEntity() instanceof LivingEntity living)) return;
        if (living instanceof net.minecraft.world.entity.player.Player) return;

        MobStatsAttachment att = living.getData(ModAttachments.MOB_STATS.get());
        if (att == null) {
            att = new MobStatsAttachment();
            living.setData(ModAttachments.MOB_STATS.get(), att);
        }

        // Применяем data-driven оверрайд (если есть)
        MobTuning.applyPerEntityOverrides((ServerLevel) event.getLevel(), living, att);

        // Инициализируем HP от max
        int max = (int)Math.max(1, att.getSnapshot().maxHealth);
        if (att.getCurrentHealth() <= 0) att.setCurrentHealth(max);
    }
}

