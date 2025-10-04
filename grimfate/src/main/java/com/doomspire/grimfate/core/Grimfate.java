package com.doomspire.grimfate.core;

import com.doomspire.grimcore.affix.AffixAggregator;
import com.doomspire.grimcore.affix.ModAffixes;
import com.doomspire.grimfate.commands.AddSpellCommand;
import com.doomspire.grimfate.config.ClientConfig;
import com.doomspire.grimfate.item.armor.Armors;
import com.doomspire.grimfate.item.jewelry.Jewelry;
import com.doomspire.grimfate.item.materials.Materials;
import com.doomspire.grimfate.item.weapons.Weapons;
import com.doomspire.grimfate.network.ModNetworking;
import com.doomspire.grimfate.registry.*;
import com.mojang.logging.LogUtils;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

@Mod(Grimfate.MODID)
public class Grimfate {
    public static final String MODID = "grimfate";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    // Вкладка 1: Снаряжение (оружие/броня/бижутерия)
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB_EQUIPMENT =
            CREATIVE_MODE_TABS.register("equipment", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.grimfate.equipment"))
                    .icon(() -> new ItemStack(Weapons.COPPER_SWORD.get()))
                    .withSearchBar()
                    .build());

    // Вкладка 2: Материалы и блоки
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB_MATERIALS =
            CREATIVE_MODE_TABS.register("materials", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.grimfate.materials"))
                    // Иконка — попытаемся взять fiber, если нет — fallback на медный меч
                    .icon(() -> new ItemStack(Materials.RAWHIDE.get()))
                    .withSearchBar()
                    .build());

    // Вкладка 3: Мобы (пока пусто, но таб будет готов)
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB_MOBS =
            CREATIVE_MODE_TABS.register("mobs", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.grimfate.mobs"))
                    .icon(() -> new ItemStack(Materials.PORK_FAT.get()))
                    .withSearchBar()
                    .build());

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

        com.doomspire.grimfate.item.weapons.Weapons.init(modEventBus);
        com.doomspire.grimfate.item.armor.Armors.init(modEventBus);
        com.doomspire.grimfate.item.materials.Materials.init(modEventBus);
        com.doomspire.grimfate.item.jewelry.Jewelry.init(modEventBus);

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
            // Two-hand резолвер для WeaponGate (читает профили из data-компонента)
            com.doomspire.grimcore.spell.api.WeaponGate.setTwoHandedResolver((stack, player) -> {
                var comp = stack.get(com.doomspire.grimfate.registry.ModDataComponents.WEAPON_PROFILE.get());
                return comp != null && comp.twoHanded();
            });

            // Интеграции
            com.doomspire.grimfate.compat.bettercombat.BetterCombatBridge.init();
            com.doomspire.grimfate.registry.ModCurios.init();

            // Аффиксы
            ModAffixes.bootstrap();
            AffixAggregator.setExtractor(com.doomspire.grimfate.affix.GrimfateAffixExtraction::extractFromEntity);

            LOGGER.info("[Grimfate] Creative tabs registered; AffixAggregator hooked; integrations bootstrapped.");
        });
    }

    // Наполнение вкладок
    private void addCreative(final BuildCreativeModeTabContentsEvent e) {
        if (e.getTab() == TAB_EQUIPMENT.get()) {

            safeAdd(e, Weapons.COPPER_SWORD);
            safeAdd(e, Weapons.SCHOLAR_STAFF);
            safeAdd(e, Weapons.WEAKLING_BOW);
            safeAdd(e, Weapons.COPPERFORCED_SHIELD);
            safeAdd(e, Armors.COPPER_BOOTS);
            safeAdd(e, Armors.COPPER_CHESTPLATE);
            safeAdd(e, Armors.COPPER_LEGGINGS);
            safeAdd(e, Armors.COPPER_HELMET);
            safeAdd(e, Jewelry.COPPER_RING);
            safeAdd(e, Jewelry.COPPER_NECKLACE);
            safeAdd(e, Jewelry.BRONZE_RING);
            safeAdd(e, Jewelry.BRONZE_NECKLACE);
            safeAdd(e, Armors.LINEN_SHOES);
            safeAdd(e, Armors.LINEN_CAPE);
            safeAdd(e, Armors.LINEN_PANTS);
            safeAdd(e, Armors.LINEN_HAT);
            safeAdd(e, Armors.RAWHIDE_BOOTS);
            safeAdd(e, Armors.RAWHIDE_JACKET);
            safeAdd(e, Armors.RAWHIDE_WRAPS);
            safeAdd(e, Armors.RAWHIDE_HOOD);

        }
        else if (e.getTab() == TAB_MATERIALS.get()) {
            // Материалы по id (без жёсткой зависимости на класс подмодуля)
            acceptIfPresent(e, rl("fiber"));
            acceptIfPresent(e, rl("canvas_fabric.json"));
            acceptIfPresent(e, rl("pork_fat"));
            acceptIfPresent(e, rl("rawhide"));
            // Блоки добавим позже, когда появятся
        }
        else if (e.getTab() == TAB_MOBS.get()) {
            // Пока пусто — добавим яйца призыва и дропы, когда появятся сущности
        }
    }

    // ==== Helpers ============================================================

    private static void safeAdd(BuildCreativeModeTabContentsEvent e, DeferredHolder<Item, ? extends Item> h) {
        if (h != null && h.isBound()) e.accept(h.get());
    }

    private static void acceptIfPresent(BuildCreativeModeTabContentsEvent e, ResourceLocation id) {
        Item item = lookupItem(id);
        if (item != null) e.accept(item);
    }

    private static ItemStack stackOrFallback(ResourceLocation prefer, Item fallback) {
        Item item = lookupItem(prefer);
        return new ItemStack(item != null ? item : fallback);
    }

    private static Item lookupItem(ResourceLocation id) {
        return BuiltInRegistries.ITEM.getOptional(id).orElse(null);
    }


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
