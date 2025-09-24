package com.doomspire.grimfate.registry;

import com.doomspire.grimfate.core.Grimfate;
import com.doomspire.grimfate.entity.BoltProjectileEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModEntityTypes {
    private ModEntityTypes(){}

    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(Registries.ENTITY_TYPE, Grimfate.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<BoltProjectileEntity>> BOLT =
            ENTITIES.register("bolt", () -> EntityType.Builder
                    .<BoltProjectileEntity>of(BoltProjectileEntity::new, MobCategory.MISC)
                    .sized(0.25f, 0.25f)
                    .clientTrackingRange(64)
                    .updateInterval(2)
                    .build(Grimfate.MODID + ":bolt"));

    public static void init(IEventBus modBus) {
        ENTITIES.register(modBus);
    }
}


