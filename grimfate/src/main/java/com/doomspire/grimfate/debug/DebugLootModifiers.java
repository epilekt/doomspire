package com.doomspire.grimfate.debug;

import com.doomspire.grimfate.core.Grimfate;
import com.mojang.serialization.MapCodec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DebugLootModifiers {
    private static final Logger LOG = LoggerFactory.getLogger("Grimfate/GLM-DEBUG");

    public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> REG =
            DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, Grimfate.MODID);

    public static final net.neoforged.neoforge.registries.DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<? extends IGlobalLootModifier>>
            ALWAYS_LOG_AND_ADD_BREAD = REG.register("always_log_and_add_bread", () -> AlwaysLogAndAddBread.CODEC);

    public static void init(IEventBus modBus) {
        LOG.info("[GLM-DEBUG] registering codec grimfate:always_log_and_add_bread");
        REG.register(modBus);
    }
}
