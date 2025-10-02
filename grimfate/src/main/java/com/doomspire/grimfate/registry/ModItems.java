package com.doomspire.grimfate.registry;

import com.doomspire.grimfate.core.Grimfate;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

//NOTE: Реестр предметов grimfate

/**
 * Все конкретные предметы объявляются и регистрируются в подмодулях:
 *  - item/weapons/Weapons
 *  - item/armor/Armors
 *  - item/materials/Materials
 *  - item/jewelry/Jewelry
 */

public final class ModItems {
    private ModItems() {}

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, Grimfate.MODID);

    public static void init(IEventBus modBus) {
        ITEMS.register(modBus);
        // Подмодулям не требуется отдельный вызов — их поля регистрируются статически через ModItems.ITEMS
        // (Метод оставлен на будущее, если появится дополнительная логика инициализации.)
    }
}
