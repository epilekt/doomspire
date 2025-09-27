package com.doomspire.grimfate.registry;

import com.doomspire.grimfate.core.Grimfate;
import com.doomspire.grimfate.item.comp.AffixListComponent;
import com.doomspire.grimfate.item.comp.WeaponProfileComponent;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModDataComponents {
    private ModDataComponents() {}

    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, Grimfate.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<WeaponProfileComponent>> WEAPON_PROFILE =
            DATA_COMPONENT_TYPES.register("weapon_profile",
                    () -> DataComponentType.<WeaponProfileComponent>builder()
                            .persistent(WeaponProfileComponent.CODEC)
                            .networkSynchronized(WeaponProfileComponent.STREAM_CODEC)
                            .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<AffixListComponent>> AFFIX_LIST =
            DATA_COMPONENT_TYPES.register("affix_list",
                    () -> DataComponentType.<AffixListComponent>builder()
                            .persistent(AffixListComponent.CODEC)
                            .networkSynchronized(AffixListComponent.STREAM_CODEC)
                            .build());

    public static void init(IEventBus modBus) {
        DATA_COMPONENT_TYPES.register(modBus);
    }
}
