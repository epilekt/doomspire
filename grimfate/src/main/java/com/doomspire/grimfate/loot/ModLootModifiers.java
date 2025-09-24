package com.doomspire.grimfate.loot;

import com.doomspire.grimfate.core.Grimfate;
import com.mojang.serialization.MapCodec;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class ModLootModifiers {
    private ModLootModifiers() {}

    // Регистрируем КОДЕКИ модификаторов лута
    public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> GLM_SERIALIZERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, Grimfate.MODID);

    public static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<RustyRingDropModifier>> RUSTY_RING =
            GLM_SERIALIZERS.register("rusty_ring", () -> RustyRingDropModifier.CODEC);

    public static void init(net.neoforged.bus.api.IEventBus modBus) {
        GLM_SERIALIZERS.register(modBus);
    }
}
