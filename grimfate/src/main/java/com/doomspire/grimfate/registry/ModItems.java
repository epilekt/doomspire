package com.doomspire.grimfate.registry;

import com.doomspire.grimfate.combat.WeaponType;
import com.doomspire.grimfate.core.Grimfate;
import com.doomspire.grimfate.item.StaffItem;
import com.doomspire.grimfate.item.comp.WeaponProfileComponent;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    private ModItems(){}

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, Grimfate.MODID);

    // Материалы
    public static final DeferredHolder<Item, Item> HARDWOOD_SHAFT = ITEMS.register("hardwood_shaft", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> LINEN_CLOTH    = ITEMS.register("linen_cloth",    () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> COPPER_RIVET   = ITEMS.register("copper_rivet",   () -> new Item(new Item.Properties()));

    // Оружие/щит (каждому задаём WEAPON_PROFILE через Properties.component)
    public static final DeferredHolder<Item, Item> RUSTY_SWORD = ITEMS.register("rusty_sword",
            () -> new Item(new Item.Properties()
                    .stacksTo(1)
                    .component(ModDataComponents.WEAPON_PROFILE.get(),
                            new WeaponProfileComponent(WeaponType.SWORD, false, 1.0f, 1.0f, 1))));

    public static final DeferredHolder<Item, Item> COPPER_SWORD = ITEMS.register("copper_sword",
            () -> new Item(new Item.Properties()
                    .stacksTo(1)
                    .component(ModDataComponents.WEAPON_PROFILE.get(),
                            new WeaponProfileComponent(WeaponType.SWORD, false, 1.05f, 1.1f, 1))));

    public static final DeferredHolder<Item, Item> WEATHERED_STAFF = ITEMS.register("weathered_staff",
            () -> new StaffItem(new Item.Properties()
                    .stacksTo(1)
                    .component(ModDataComponents.WEAPON_PROFILE.get(),
                            new WeaponProfileComponent(WeaponType.STAFF, true, 1.0f, 1.0f, 2))));

    public static final DeferredHolder<Item, Item> APPRENTICE_STAFF = ITEMS.register("apprentice_staff",
            () -> new StaffItem(new Item.Properties()
                    .stacksTo(1)
                    .rarity(Rarity.UNCOMMON)
                    .component(ModDataComponents.WEAPON_PROFILE.get(),
                            new WeaponProfileComponent(WeaponType.STAFF, true, 1.05f, 1.1f, 2))));

    public static final DeferredHolder<Item, Item> SIMPLE_BOW = ITEMS.register("simple_bow",
            () -> new Item(new Item.Properties()
                    .stacksTo(1)
                    .component(ModDataComponents.WEAPON_PROFILE.get(),
                            new WeaponProfileComponent(WeaponType.BOW, true, 1.0f, 1.0f, 1))));

    public static final DeferredHolder<Item, Item> HUNTERS_BOW = ITEMS.register("hunters_bow",
            () -> new Item(new Item.Properties()
                    .stacksTo(1)
                    .rarity(Rarity.UNCOMMON)
                    .component(ModDataComponents.WEAPON_PROFILE.get(),
                            new WeaponProfileComponent(WeaponType.BOW, true, 1.05f, 1.15f, 2))));

    public static final DeferredHolder<Item, Item> WEATHERED_SHIELD = ITEMS.register("weathered_shield",
            () -> new Item(new Item.Properties()
                    .stacksTo(1)
                    .component(ModDataComponents.WEAPON_PROFILE.get(),
                            new WeaponProfileComponent(WeaponType.SHIELD, false, 0.9f, 0.0f, 0))));

    public static final DeferredHolder<Item, Item> RUSTY_RING = ITEMS.register("rusty_ring",
            () -> new Item(new Item.Properties()
                    .stacksTo(1)
                    .rarity(Rarity.UNCOMMON)));

    public static void init(IEventBus modBus) {
        ITEMS.register(modBus);
    }
}

