package com.doomspire.grimfate.client;

import com.doomspire.grimfate.core.Grimfate;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;

@EventBusSubscriber(modid = Grimfate.MODID, value = Dist.CLIENT)
public class HudOverlay {
    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiLayerEvent.Pre event) {
        // Убираем отображение ванили
        ResourceLocation layerName = event.getName();
        if (layerName.equals(ResourceLocation.fromNamespaceAndPath("minecraft", "player_health"))) {
            event.setCanceled(true);
        }
        if (layerName.equals(ResourceLocation.fromNamespaceAndPath("minecraft", "armor_level"))) {
            event.setCanceled(true);
        }
    }
}
