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
    private static NoAnimGeoArmorItem.Visual copperVisual() {
        return new GenericGeoArmorItem.Visual(
                Grimfate.rl("geo/armor/copper_armor_set.geo.json"),
                Grimfate.rl("textures/armor/copper_armor_set_texture.png")
        );
    }

    private static NoAnimGeoArmorItem.Visual rawhideVisual() {
        return new GenericGeoArmorItem.Visual(
                Grimfate.rl("geo/armor/rawhide_armor_set.geo.json"),
                Grimfate.rl("textures/armor/rawhide_armor_set.png")
        );
    }

    private static NoAnimGeoArmorItem.Visual linenVisual() {
        return new GenericGeoArmorItem.Visual(
                Grimfate.rl("geo/armor/linen_armor_set.geo.json"),
                Grimfate.rl("textures/armor/linen_armor_set.png")
        );
    }

    // ===== COPPER =====
    public static final DeferredHolder<Item, Item> COPPER_HELMET =
            ModItems.ITEMS.register("copper_helmet",
                    () -> new NoAnimGeoArmorItem(
                            ModArmorMaterials.copperHolder(),
                            ArmorItem.Type.HELMET, new Item.Properties(), copperVisual()));

    public static final DeferredHolder<Item, Item> COPPER_CHESTPLATE =
            ModItems.ITEMS.register("copper_chestplate",
                    () -> new NoAnimGeoArmorItem(
                            ModArmorMaterials.copperHolder(),
                            ArmorItem.Type.CHESTPLATE, new Item.Properties(), copperVisual()));

    public static final DeferredHolder<Item, Item> COPPER_LEGGINGS =
            ModItems.ITEMS.register("copper_leggings",
                    () -> new NoAnimGeoArmorItem(
                            ModArmorMaterials.copperHolder(),
                            ArmorItem.Type.LEGGINGS, new Item.Properties(), copperVisual()));

    public static final DeferredHolder<Item, Item> COPPER_BOOTS =
            ModItems.ITEMS.register("copper_boots",
                    () -> new NoAnimGeoArmorItem(
                            ModArmorMaterials.copperHolder(),
                            ArmorItem.Type.BOOTS, new Item.Properties(), copperVisual()));

    // ===== RAWHIDE =====
    public static final DeferredHolder<Item, Item> RAWHIDE_HOOD =
            ModItems.ITEMS.register("rawhide_hood",
                    () -> new NoAnimGeoArmorItem(
                            ModArmorMaterials.rawhideHolder(),
                            ArmorItem.Type.HELMET, new Item.Properties(), rawhideVisual()));

    public static final DeferredHolder<Item, Item> RAWHIDE_JACKET =
            ModItems.ITEMS.register("rawhide_jacket",
                    () -> new NoAnimGeoArmorItem(
                            ModArmorMaterials.rawhideHolder(),
                            ArmorItem.Type.CHESTPLATE, new Item.Properties(), rawhideVisual()));

    public static final DeferredHolder<Item, Item> RAWHIDE_WRAPS =
            ModItems.ITEMS.register("rawhide_wraps",
                    () -> new NoAnimGeoArmorItem(
                            ModArmorMaterials.rawhideHolder(),
                            ArmorItem.Type.LEGGINGS, new Item.Properties(), rawhideVisual()));

    public static final DeferredHolder<Item, Item> RAWHIDE_BOOTS =
            ModItems.ITEMS.register("rawhide_boots",
                    () -> new NoAnimGeoArmorItem(
                            ModArmorMaterials.rawhideHolder(),
                            ArmorItem.Type.BOOTS, new Item.Properties(), rawhideVisual()));

    // ===== LINEN =====
    public static final DeferredHolder<Item, Item> LINEN_HAT =
            ModItems.ITEMS.register("linen_hat",
                    () -> new NoAnimGeoArmorItem(
                            ModArmorMaterials.linenHolder(),
                            ArmorItem.Type.HELMET, new Item.Properties(), linenVisual()));

    public static final DeferredHolder<Item, Item> LINEN_CAPE =
            ModItems.ITEMS.register("linen_cape",
                    () -> new NoAnimGeoArmorItem(
                            ModArmorMaterials.linenHolder(),
                            ArmorItem.Type.CHESTPLATE, new Item.Properties(), linenVisual()));

    public static final DeferredHolder<Item, Item> LINEN_PANTS =
            ModItems.ITEMS.register("linen_pants",
                    () -> new NoAnimGeoArmorItem(
                            ModArmorMaterials.linenHolder(),
                            ArmorItem.Type.LEGGINGS, new Item.Properties(), linenVisual()));

    public static final DeferredHolder<Item, Item> LINEN_SHOES =
            ModItems.ITEMS.register("linen_shoes",
                    () -> new NoAnimGeoArmorItem(
                            ModArmorMaterials.linenHolder(),
                            ArmorItem.Type.BOOTS, new Item.Properties(), linenVisual()));

    /** Вызывается из ModItems.init(modBus) при необходимости; регистрации статичны. */
    public static void init(IEventBus modBus) {}
}
