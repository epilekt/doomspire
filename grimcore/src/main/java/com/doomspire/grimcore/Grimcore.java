package com.doomspire.grimcore;

import com.doomspire.grimcore.events.CoreDamageEvents;
import com.doomspire.grimcore.events.CorePlayerEvents;
import com.doomspire.grimcore.net.GrimcoreNetworking;
import com.doomspire.grimcore.stats.ModAttachments;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

@Mod(Grimcore.MODID)
public final class Grimcore {
    public static final String MODID = "grimcore";
    public Grimcore(IEventBus modEventBus, ModContainer container) {
        ModAttachments.ATTACHMENT_TYPES.register(modEventBus);
        NeoForge.EVENT_BUS.register(CoreDamageEvents.class);
        NeoForge.EVENT_BUS.register(CorePlayerEvents.class);
        GrimcoreNetworking.init(modEventBus);
    }
}



