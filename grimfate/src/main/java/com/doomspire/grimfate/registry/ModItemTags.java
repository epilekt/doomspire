package com.doomspire.grimfate.registry;

import com.doomspire.grimfate.core.Grimfate;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
//TODO: Привести к существующим в data тэгам, поправить WeaponPredicates.java
public final class ModItemTags {
    public static final TagKey<Item> MELEE_WEAPONS  = tag("melee_weapons");
    public static final TagKey<Item> STAVES         = tag("staves");
    public static final TagKey<Item> RANGED_WEAPONS = tag("ranged_weapons");
    public static final TagKey<Item> DAGGERS        = tag("daggers");
    public static final TagKey<Item> SHIELDS        = tag("shields");

    private static TagKey<Item> tag(String path) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(Grimfate.MODID, path));
    }
    private ModItemTags() {}
}
