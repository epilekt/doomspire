package com.doomspire.grimfate.item.armor;

import com.doomspire.grimfate.core.Grimfate;
import com.doomspire.grimfate.item.armor.GenericGeoArmorItem;
import com.doomspire.grimfate.registry.ModArmorMaterials;
import com.doomspire.grimfate.registry.ModItems;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;

//NOTE: Подмодуль регистрации брони.

public final class Armors {
    private Armors() {}

    // ===== VISUALS =====
    private static GenericGeoArmorItem.Visual copperVisual() {
        return new GenericGeoArmorItem.Visual(
                Grimfate.rl("geo/armor/copper_armor_set.geo.json"),
                Grimfate.rl("textures/armor/copper_armor_set_texture.png")
        );
    }

    private static GenericGeoArmorItem.Visual rawhideVisual() {
        return new GenericGeoArmorItem.Visual(
                Grimfate.rl("geo/armor/rawhide_armor_set.geo.json"),
                Grimfate.rl("textures/armor/rawhide_armor_set.png")
        );
    }

    private static GenericGeoArmorItem.Visual linenVisual() {
        return new GenericGeoArmorItem.Visual(
                Grimfate.rl("geo/armor/linen_armor_set.geo.json"),
                Grimfate.rl("textures/armor/linen_armor_set.png")
        );
    }

    // ===== COPPER =====
    public static final DeferredHolder<Item, Item> COPPER_HELMET =
            ModItems.ITEMS.register("copper_helmet",
                    () -> new GenericGeoArmorItem(
                            ModArmorMaterials.copperHolder(),
                            ArmorItem.Type.HELMET, new Item.Properties(), copperVisual()));

    public static final DeferredHolder<Item, Item> COPPER_CHESTPLATE =
            ModItems.ITEMS.register("copper_chestplate",
                    () -> new GenericGeoArmorItem(
                            ModArmorMaterials.copperHolder(),
                            ArmorItem.Type.CHESTPLATE, new Item.Properties(), copperVisual()));

    public static final DeferredHolder<Item, Item> COPPER_LEGGINGS =
            ModItems.ITEMS.register("copper_leggings",
                    () -> new GenericGeoArmorItem(
                            ModArmorMaterials.copperHolder(),
                            ArmorItem.Type.LEGGINGS, new Item.Properties(), copperVisual()));

    public static final DeferredHolder<Item, Item> COPPER_BOOTS =
            ModItems.ITEMS.register("copper_boots",
                    () -> new GenericGeoArmorItem(
                            ModArmorMaterials.copperHolder(),
                            ArmorItem.Type.BOOTS, new Item.Properties(), copperVisual()));

    // ===== RAWHIDE =====
    public static final DeferredHolder<Item, Item> RAWHIDE_HELMET =
            ModItems.ITEMS.register("rawhide_helmet",
                    () -> new GenericGeoArmorItem(
                            ModArmorMaterials.rawhideHolder(),
                            ArmorItem.Type.HELMET, new Item.Properties(), rawhideVisual()));

    public static final DeferredHolder<Item, Item> RAWHIDE_CHESTPLATE =
            ModItems.ITEMS.register("rawhide_chestplate",
                    () -> new GenericGeoArmorItem(
                            ModArmorMaterials.rawhideHolder(),
                            ArmorItem.Type.CHESTPLATE, new Item.Properties(), rawhideVisual()));

    public static final DeferredHolder<Item, Item> RAWHIDE_LEGGINGS =
            ModItems.ITEMS.register("rawhide_leggings",
                    () -> new GenericGeoArmorItem(
                            ModArmorMaterials.rawhideHolder(),
                            ArmorItem.Type.LEGGINGS, new Item.Properties(), rawhideVisual()));

    public static final DeferredHolder<Item, Item> RAWHIDE_BOOTS =
            ModItems.ITEMS.register("rawhide_boots",
                    () -> new GenericGeoArmorItem(
                            ModArmorMaterials.rawhideHolder(),
                            ArmorItem.Type.BOOTS, new Item.Properties(), rawhideVisual()));

    // ===== LINEN =====
    public static final DeferredHolder<Item, Item> LINEN_HELMET =
            ModItems.ITEMS.register("linen_helmet",
                    () -> new GenericGeoArmorItem(
                            ModArmorMaterials.linenHolder(),
                            ArmorItem.Type.HELMET, new Item.Properties(), linenVisual()));

    public static final DeferredHolder<Item, Item> LINEN_CHESTPLATE =
            ModItems.ITEMS.register("linen_chestplate",
                    () -> new GenericGeoArmorItem(
                            ModArmorMaterials.linenHolder(),
                            ArmorItem.Type.CHESTPLATE, new Item.Properties(), linenVisual()));

    public static final DeferredHolder<Item, Item> LINEN_LEGGINGS =
            ModItems.ITEMS.register("linen_leggings",
                    () -> new GenericGeoArmorItem(
                            ModArmorMaterials.linenHolder(),
                            ArmorItem.Type.LEGGINGS, new Item.Properties(), linenVisual()));

    public static final DeferredHolder<Item, Item> LINEN_BOOTS =
            ModItems.ITEMS.register("linen_boots",
                    () -> new GenericGeoArmorItem(
                            ModArmorMaterials.linenHolder(),
                            ArmorItem.Type.BOOTS, new Item.Properties(), linenVisual()));

    /** Вызывается из ModItems.init(modBus) при необходимости; регистрации статичны. */
    public static void init(IEventBus modBus) {}
}
