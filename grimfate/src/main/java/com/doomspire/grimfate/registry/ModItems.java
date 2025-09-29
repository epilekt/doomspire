package com.doomspire.grimfate.registry;

import com.doomspire.grimcore.data.component.StatBonusComponent;
import com.doomspire.grimcore.stat.Attributes;
import com.doomspire.grimcore.stat.DamageTypes;
import com.doomspire.grimfate.combat.WeaponType;
import com.doomspire.grimfate.core.Grimfate;
import com.doomspire.grimfate.item.StaffItem;
import com.doomspire.grimfate.item.armor.GenericGeoArmorItem;
import com.doomspire.grimfate.item.comp.WeaponProfileComponent;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Map;

import static com.doomspire.grimfate.core.Grimfate.rl;

public final class ModItems {
    private ModItems(){}

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, Grimfate.MODID);

    // Материалы
    public static final DeferredHolder<Item, Item> HARDWOOD_SHAFT = ITEMS.register("hardwood_shaft", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> LINEN_CLOTH    = ITEMS.register("linen_cloth",    () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> COPPER_RIVET   = ITEMS.register("copper_rivet",   () -> new Item(new Item.Properties()));

    private static GenericGeoArmorItem.Visual copperVisual() {
        return new GenericGeoArmorItem.Visual(
                rl("geo/armor/copper_armor_set.geo.json"),
                rl("textures/armor/copper_armor_set_texture.png")
        );
    }

    // ===== Jewelry =====
    public static final DeferredItem<Item> COPPER_RING = ITEMS.register("copper_ring",
            () -> new Item(new Item.Properties()
                    .stacksTo(1)
                    .component(ModDataComponents.STAT_BONUS.get(),
                            // +1 INT
                            new StatBonusComponent(Map.of(Attributes.INTELLIGENCE, 1)))
            ));

    public static final DeferredItem<Item> SCHOLAR_RING = ITEMS.register("scholar_ring",
            () -> new Item(new Item.Properties()
                    .stacksTo(1)
                    .component(ModDataComponents.STAT_BONUS.get(),
                            // +1 INT, +1 SPIRIT
                            new StatBonusComponent(Map.of(Attributes.INTELLIGENCE, 1, Attributes.SPIRIT, 1)))
            ));

    public static final DeferredItem<Item> SIMPLE_AMULET = ITEMS.register("simple_amulet",
            () -> new Item(new Item.Properties()
                    .stacksTo(1)
                    .component(ModDataComponents.STAT_BONUS.get(),
                            // +1 SPIRIT (= условная «мана» на старте)
                            new StatBonusComponent(Map.of(Attributes.SPIRIT, 1)))
            ));

    public static final DeferredItem<Item> HUNTER_AMULET = ITEMS.register("hunter_amulet",
            () -> new Item(new Item.Properties()
                    .stacksTo(1)
                    .component(ModDataComponents.STAT_BONUS.get(),
                            // +1 DEX
                            new StatBonusComponent(Map.of(Attributes.DEXTERITY, 1)))
            ));

    public static final DeferredItem<Item> LEATHER_BELT = ITEMS.register("leather_belt",
            () -> new Item(new Item.Properties()
                    .stacksTo(1)
                    .component(ModDataComponents.STAT_BONUS.get(),
                            // +1 VIT
                            new StatBonusComponent(Map.of(Attributes.VITALITY, 1)))
            ));

    public static final DeferredItem<Item> TOTEM_SHARD = ITEMS.register("totem_shard",
            () -> new Item(new Item.Properties()
                    .stacksTo(1)
                    .component(ModDataComponents.RESIST_BONUS.get(),
                            // см. компонент ниже: +5% ко всем резистам как старт
                            new ResistBonusComponent(Map.of(
                                    DamageTypes.FIRE, 0.05f,
                                    DamageTypes.FROST, 0.05f,
                                    DamageTypes.LIGHTNING, 0.05f,
                                    DamageTypes.POISON, 0.05f,
                                    DamageTypes.PHYS_MELEE, 0.05f)))
            ));

    // Броня (каждый предмет — Item с humanoidArmor + GeckoLib визуалом)
    public static final DeferredHolder<Item, Item> COPPER_HELMET = ITEMS.register("copper_helmet",
            () -> new GenericGeoArmorItem(ModArmorMaterials.copperHolder(), ArmorItem.Type.HELMET, new Item.Properties(), copperVisual()));
    public static final DeferredHolder<Item, Item> COPPER_CHESTPLATE = ITEMS.register("copper_chestplate",
            () -> new GenericGeoArmorItem(ModArmorMaterials.copperHolder(), ArmorItem.Type.CHESTPLATE, new Item.Properties(), copperVisual()));
    public static final DeferredHolder<Item, Item> COPPER_LEGGINGS = ITEMS.register("copper_leggings",
            () -> new GenericGeoArmorItem(ModArmorMaterials.copperHolder(), ArmorItem.Type.LEGGINGS, new Item.Properties(), copperVisual()));
    public static final DeferredHolder<Item, Item> COPPER_BOOTS = ITEMS.register("copper_boots",
            () -> new GenericGeoArmorItem(ModArmorMaterials.copperHolder(), ArmorItem.Type.BOOTS, new Item.Properties(), copperVisual()));

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

