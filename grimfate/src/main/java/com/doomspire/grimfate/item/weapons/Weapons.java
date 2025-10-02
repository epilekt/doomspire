package com.doomspire.grimfate.item.weapons;

import com.doomspire.grimfate.item.BaseBowItem;
import com.doomspire.grimfate.registry.ModItems;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;

//NOTE: Подмодуль регистрации оружия/щита. Объявляем RegistryObject/DeferredHolder. Сюда прокидываем новые пушки
/**
 * ВАЖНО: сам DeferredRegister<Item> хранится в ModItems.
 * Здесь мы только объявляем RegistryObject/DeferredHolder и регистрируем через ModItems.ITEMS.register(...).
 */
public final class Weapons {
    private Weapons() {}

    // ===== Оружие / щит (PHASE_PLAN) =====
    public static final DeferredHolder<Item, Item> COPPER_SWORD =
            ModItems.ITEMS.register("copper_sword",
                    () -> new Item(new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, Item> SCHOLAR_STAFF =
            ModItems.ITEMS.register("scholar_staff",
                    () -> new com.doomspire.grimfate.item.StaffItem(new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, Item> WEAKLING_BOW =
            ModItems.ITEMS.register("weakling_bow",
                    () -> new BaseBowItem(new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, Item> COPPERFORCED_SHIELD =
            ModItems.ITEMS.register("copperforced_shield",
                    () -> new Item(new Item.Properties().stacksTo(1)));

    /** Вызывается из ModItems.init(modBus) для явной инициализации подмодуля (на будущее). */
    public static void init(IEventBus modBus) {
        // Ничего: все регистрации делаются статически через ModItems.ITEMS.register(...)
        // Метод оставлен для единообразия вызовов из ModItems/Grimfate.
    }
}
