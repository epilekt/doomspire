package com.doomspire.grimcore;

import com.doomspire.grimcore.net.GrimcoreNetworking;
import com.doomspire.grimcore.net.ProgressNetworking;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@EventBusSubscriber(modid = Grimcore.MODID) // без bus = ...
public final class ModBusHandlers {
    private ModBusHandlers() {}

    @SubscribeEvent
    public static void onRegisterPayloads(RegisterPayloadHandlersEvent e) {
        GrimcoreNetworking.register(e);
        ProgressNetworking.register(e);
    }
}
