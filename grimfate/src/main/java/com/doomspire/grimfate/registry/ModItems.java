package com.doomspire.grimfate.registry;

import com.doomspire.grimfate.core.Grimfate;
import com.doomspire.grimfate.item.StaffItem;
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

    public static final DeferredHolder<Item, Item> RUSTY_RING = ITEMS.register("rusty_ring",
            () -> new Item(new Item.Properties()
                    .stacksTo(1)
                    .rarity(Rarity.UNCOMMON)
            ));

    public static final DeferredHolder<Item, Item> STAFF =
            ITEMS.register("staff", () -> new StaffItem(new Item.Properties().stacksTo(1)));

    /** Используется рендерером болта — простой предмет-«обёртка» для модели снаряда. */
    public static final DeferredHolder<Item, Item> BOLT_ITEM =
            ITEMS.register("bolt", () -> new Item(new Item.Properties()));

    public static void init(IEventBus modBus) {
        ITEMS.register(modBus);
    }
}

