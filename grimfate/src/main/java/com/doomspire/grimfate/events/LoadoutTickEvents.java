package com.doomspire.grimfate.events;

import com.doomspire.grimcore.attach.PlayerLoadoutAttachment;
import com.doomspire.grimcore.stat.ModAttachments;
import com.doomspire.grimfate.core.Grimfate;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = Grimfate.MODID, value = Dist.DEDICATED_SERVER) // слушаем только на сервере
public final class LoadoutTickEvents {
    private LoadoutTickEvents() {}

    private static final Map<UUID, Long> LAST_SYNC_TICK = new HashMap<>();
    private static final Map<UUID, Integer> LAST_SUM = new HashMap<>();
    private static final int SYNC_COOLDOWN_TICKS = 10; // ~0.5с при 20 TPS

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post e) {
        Player p = e.getEntity();
        if (p.level().isClientSide) return;

        PlayerLoadoutAttachment att = p.getData(ModAttachments.PLAYER_LOADOUT.get());
        if (att == null) return;

        // 1) уменьшаем кулдауны
        att.tickDown();

        // 2) считаем «суммарный кулдаун», чтобы дешево понимать, изменилось ли что-то
        int sum = 0;
        for (int i = 0; i < PlayerLoadoutAttachment.SLOTS; i++) {
            sum += att.getCooldown(i);
        }

        long now = p.level().getGameTime();
        UUID id = p.getUUID();
        long last = LAST_SYNC_TICK.getOrDefault(id, 0L);
        int prev = LAST_SUM.getOrDefault(id, -1);

        // 3) синкаем только если:
        //  - прошло >= SYNC_COOLDOWN_TICKS тиков
        //  - изменилась агрегированная сумма (включая переход в ноль)
        if ((now - last) >= SYNC_COOLDOWN_TICKS && sum != prev) {
            p.setData(ModAttachments.PLAYER_LOADOUT.get(), att); // триггерит .sync(...) из ModAttachments
            LAST_SYNC_TICK.put(id, now);
            LAST_SUM.put(id, sum);
        }
    }
}
