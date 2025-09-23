package com.doomspire.grimcore;

import com.doomspire.grimcore.data.ModDataComponents;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(Grimcore.MODID)
public final class Grimcore {
    public static final String MODID = "grimcore";
    public Grimcore(IEventBus modEventBus, ModContainer container) {
        // attachments регистрируем на MOD bus
        com.doomspire.grimcore.stat.ModAttachments.ATTACHMENT_TYPES.register(modEventBus);
        com.doomspire.grimcore.events.RegenTicker.registerToBus();
        // игровые события — на общий шина NeoForge
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.register(com.doomspire.grimcore.events.CoreDamageEvents.class);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.register(com.doomspire.grimcore.events.CorePlayerEvents.class);
        ModDataComponents.init(modEventBus);
    }
}



