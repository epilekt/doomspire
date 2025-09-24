package com.doomspire.grimfate.events;

import com.doomspire.grimcore.attach.PlayerLoadoutAttachment;
import com.doomspire.grimcore.stat.ModAttachments;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public final class LoadoutTickEvents {
    private LoadoutTickEvents() {}

    public static void register() {
        NeoForge.EVENT_BUS.register(LoadoutTickEvents.class);
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post e) {
        if (e.getEntity().level().isClientSide) return;
        var att = e.getEntity().getData(ModAttachments.PLAYER_LOADOUT.get());
        if (att != null) att.tickDown();
    }
}
