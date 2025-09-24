package com.doomspire.grimfate.core;

import com.doomspire.grimfate.client.Hotkeys;
import com.doomspire.grimfate.entity.BoltProjectileEntity;
import com.doomspire.grimfate.registry.ModEntityTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.common.NeoForge;

public final class GrimfateClient {
    private GrimfateClient() {}

    public static void registerModBusListeners(IEventBus modBus) {
        modBus.addListener(GrimfateClient::onRegisterRenderers);
        modBus.addListener(GrimfateClient::onClientSetup);
        // Ключевая строка: регистрируем KeyMappings только отсюда
        modBus.addListener(Hotkeys::onRegisterKeys);
    }

    static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers e) {
        e.registerEntityRenderer(ModEntityTypes.BOLT.get(),
                ctx -> new ThrownItemRenderer<BoltProjectileEntity>(ctx, 1.0f, false));
    }

    static void onClientSetup(FMLClientSetupEvent e) {
        Grimfate.LOGGER.info("Client setup OK. User={}", Minecraft.getInstance().getUser().getName());
        // Forge-bus listener для тиков — ровно один раз
        NeoForge.EVENT_BUS.addListener(Hotkeys::onClientTick);
    }
}

