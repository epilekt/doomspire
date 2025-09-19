package com.doomspire.grimcore;

import com.doomspire.grimcore.events.CoreDamageEvents;
import com.doomspire.grimcore.network.CoreNetworking;
import com.doomspire.grimcore.stats.ModAttachments;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(Grimcore.MODID)
public final class Grimcore {
    public static final String MODID = "grimcore";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Grimcore(IEventBus modEventBus, ModContainer container) {
        // Сеть ядра (если появятся пакеты)
        modEventBus.addListener(CoreNetworking::register);

        // Реестры ядра (attachments и пр.)
        ModAttachments.ATTACHMENT_TYPES.register(modEventBus);

        // Рантайм-события ядра (урон/статика и т.п.)
        NeoForge.EVENT_BUS.register(CoreDamageEvents.class);

        LOGGER.info("[grimcore] initialized");
    }
}


