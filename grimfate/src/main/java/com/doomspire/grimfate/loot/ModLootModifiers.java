package com.doomspire.grimfate.loot;

import com.doomspire.grimfate.core.Grimfate;
import com.mojang.serialization.MapCodec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/*
//NOTE: Регистрация Global Loot Modifiers для NeoForge 1.21.1 (реестр ожидает MapCodec<? extends IGlobalLootModifier>).
*/
public final class ModLootModifiers {
    private ModLootModifiers() {}

    // Единый реестр кодеков GLM (MapCodec)
    public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> GLM_SERIALIZERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, Grimfate.MODID);

    // grimfate:roll_affixes — регистрируем сам MapCodec
    public static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<? extends IGlobalLootModifier>> ROLL_AFFIXES =
            GLM_SERIALIZERS.register("roll_affixes", () -> RollAffixesLootModifier.CODEC);

    public static final net.neoforged.neoforge.registries.DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<? extends IGlobalLootModifier>>
            INJECT_TAG_ITEMS = GLM_SERIALIZERS.register("inject_tag_items", () -> InjectTagItemsLootModifier.CODEC);

    /** Привязать к MOD-басу из инициализации мода grimfate. */
    public static void init(IEventBus modBus) {
        GLM_SERIALIZERS.register(modBus);
    }
}
