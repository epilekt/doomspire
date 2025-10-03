package com.doomspire.grimfate.item.jewelry;

import com.doomspire.grimfate.registry.ModItems;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;

//NOTE: Подмодуль регистрации бижутерии (Curios-слоты).

public final class Jewelry {
    private Jewelry() {}

    // ===== Rings =====

    public static final DeferredHolder<Item, Item> COPPER_RING =
            ModItems.ITEMS.register("copper_ring",
                    () -> new Item(new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, Item> BRONZE_RING =
            ModItems.ITEMS.register("bronze_ring",
                    () -> new Item(new Item.Properties().stacksTo(1)));

    // ===== Amulets =====
    public static final DeferredHolder<Item, Item> COPPER_NECKLACE =
            ModItems.ITEMS.register("copper_necklace",
                    () -> new Item(new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, Item> BRONZE_NECKLACE =
            ModItems.ITEMS.register("bronze_necklace",
                    () -> new Item(new Item.Properties().stacksTo(1)));

    // ===== Belt =====
   // public static final DeferredHolder<Item, Item> LEATHER_BELT =
   //         ModItems.ITEMS.register("leather_belt",
   //                 () -> new Item(new Item.Properties().stacksTo(1)));

    /** Вызов из ModItems.init(modBus); регистрации статические. */
    public static void init(IEventBus modBus) {}
}
