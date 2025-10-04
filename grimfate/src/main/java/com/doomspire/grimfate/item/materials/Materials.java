package com.doomspire.grimfate.item.materials;

import com.doomspire.grimfate.registry.ModItems;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;

//NOTE: Подмодуль регистрации материалов крафта.

public final class Materials {
    private Materials() {}

    public static final DeferredHolder<Item, Item> FIBER =
            ModItems.ITEMS.register("fiber", () -> new Item(new Item.Properties()));

    public static final DeferredHolder<Item, Item> CANVAS_FABRIC =
            ModItems.ITEMS.register("canvas_fabric.json", () -> new Item(new Item.Properties()));

    public static final DeferredHolder<Item, Item> RAWHIDE =
            ModItems.ITEMS.register("rawhide", () -> new Item(new Item.Properties()));

    public static final DeferredHolder<Item, Item> PORK_FAT =
            ModItems.ITEMS.register("pork_fat", () -> new Item(new Item.Properties()));

    /** Вызов из ModItems.init(modBus) при необходимости; регистрации статические. */
    public static void init(IEventBus modBus) {}
}
