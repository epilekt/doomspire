package com.doomspire.grimcore;

import com.doomspire.grimcore.data.ModDataComponents;
import com.doomspire.grimcore.events.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

@Mod(Grimcore.MODID)
public final class Grimcore {
    public static final String MODID = "grimcore";
    public Grimcore(IEventBus modEventBus, ModContainer container) {
        // attachments регистрируем на MOD bus
        com.doomspire.grimcore.stat.ModAttachments.ATTACHMENT_TYPES.register(modEventBus);
        ModDataComponents.init(modEventBus);
        // игровые события — на общий шина NeoForge
        NeoForge.EVENT_BUS.register(CoreDamageEvents.class);
        NeoForge.EVENT_BUS.register(CorePlayerEvents.class);
        NeoForge.EVENT_BUS.register(MobSpawnInit.class);
        NeoForge.EVENT_BUS.register(RegenTicker.class);


    }
}



