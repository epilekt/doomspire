package com.doomspire.grimfate.core;

import com.doomspire.grimcore.affix.AffixAggregator;
import com.doomspire.grimcore.affix.ModAffixes;
import com.doomspire.grimfate.commands.AddSpellCommand;
import com.doomspire.grimfate.config.ClientConfig;
import com.doomspire.grimfate.network.ModNetworking;
import com.doomspire.grimfate.registry.ModArmorMaterials;
import com.doomspire.grimfate.registry.ModDataComponents;
import com.doomspire.grimfate.registry.ModEntityTypes;
import com.doomspire.grimfate.registry.ModItems;
import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

@Mod(Grimfate.MODID)
public class Grimfate {
    public static final String MODID = "grimfate";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public Grimfate(IEventBus modEventBus, ModContainer modContainer) {
        // Конфиги
        modContainer.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        // Сеть
        modEventBus.addListener(ModNetworking::register);

        // Контент
        CREATIVE_MODE_TABS.register(modEventBus);
        ModItems.init(modEventBus);
        ModEntityTypes.init(modEventBus);
        ModArmorMaterials.ARMOR_MATERIALS.register(modEventBus);
        com.doomspire.grimfate.loot.ModLootModifiers.init(modEventBus);
        ModDataComponents.DATA_COMPONENT_TYPES.register(modEventBus);

        // Фазы
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::addCreative);

        // Клиент — все клиентские MOD-бус листенеры
        if (FMLEnvironment.dist.isClient()) {
            GrimfateClient.registerModBusListeners(modEventBus);
        }
    }

    private void commonSetup(final FMLCommonSetupEvent e) {
        e.enqueueWork(() -> {
            // 1) Базовые аффиксы ядра (dr_all, max_mana_flat, fire_resist и т.д.)
            ModAffixes.bootstrap();

            // 2) Подключаем экстрактор аффиксов со стороны контента
            AffixAggregator.setExtractor(com.doomspire.grimfate.affix.GrimfateAffixExtraction::extractFromEntity);

            LOGGER.info("[Grimfate] AffixAggregator hooked and ModAffixes bootstrapped.");
        });
    }

    private void addCreative(final BuildCreativeModeTabContentsEvent e) { /* ... */ }

    @net.neoforged.bus.api.SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent e) {
        AddSpellCommand.register(e.getDispatcher());
    }

    public static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    @net.neoforged.bus.api.SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) { /* ... */ }
}
