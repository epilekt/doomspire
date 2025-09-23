package com.doomspire.grimcore.data;

import com.doomspire.grimcore.Grimcore;
import com.doomspire.grimcore.data.component.ClassRestrictionComponent;
import com.doomspire.grimcore.data.component.StatBonusComponent;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.component.DataComponentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModDataComponents {
    private ModDataComponents() {}

    public static final DeferredRegister<DataComponentType<?>> COMPONENT_TYPES =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, Grimcore.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<StatBonusComponent>> STAT_BONUS =
            COMPONENT_TYPES.register("stat_bonus", () ->
                    DataComponentType.<StatBonusComponent>builder()
                            .persistent(StatBonusComponent.CODEC)
                            .networkSynchronized(StatBonusComponent.STREAM_CODEC)
                            .build()
            );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ClassRestrictionComponent>> CLASS_RESTRICTION =
            COMPONENT_TYPES.register("class_restriction", () ->
                    DataComponentType.<ClassRestrictionComponent>builder()
                            .persistent(ClassRestrictionComponent.CODEC)
                            .networkSynchronized(ClassRestrictionComponent.STREAM_CODEC)
                            .build()
            );

    public static void init(net.neoforged.bus.api.IEventBus modBus) {
        COMPONENT_TYPES.register(modBus);
    }
}

