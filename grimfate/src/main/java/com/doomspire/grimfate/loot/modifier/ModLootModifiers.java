package com.doomspire.grimfate.loot.modifier;

import com.doomspire.grimfate.core.Grimfate;
import com.mojang.serialization.MapCodec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class ModLootModifiers {
    private ModLootModifiers() {}

    public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> REG =
            DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, Grimfate.MODID);

    public static final net.neoforged.neoforge.registries.DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<? extends IGlobalLootModifier>>
            ADD_ITEM       = REG.register("add_item",       () -> AddItemModifier.CODEC.get());
    public static final net.neoforged.neoforge.registries.DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<? extends IGlobalLootModifier>>
            ADD_TAG_ITEMS  = REG.register("add_tag_items",  () -> AddTagItemsModifier.CODEC.get());
    public static final net.neoforged.neoforge.registries.DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<? extends IGlobalLootModifier>>
            REPLACE_ITEM   = REG.register("replace_item",   () -> ReplaceItemModifier.CODEC.get());
    public static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<? extends IGlobalLootModifier>>
            ROLL_AFFIXES  = REG.register("roll_affixes", () -> com.doomspire.grimfate.loot.RollAffixesLootModifier.CODEC);

    public static void init(IEventBus modBus) { REG.register(modBus); }
}
