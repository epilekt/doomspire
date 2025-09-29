package com.doomspire.grimfate.registry;

import com.doomspire.grimfate.core.Grimfate;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public final class ModArmorMaterials {
    public static final DeferredRegister<ArmorMaterial> ARMOR_MATERIALS =
            DeferredRegister.create(Registries.ARMOR_MATERIAL, Grimfate.MODID);

    private static Map<ArmorItem.Type, Integer> def(int helm, int chest, int legs, int boots) {
        var m = new EnumMap<ArmorItem.Type, Integer>(ArmorItem.Type.class);
        m.put(ArmorItem.Type.HELMET, helm);
        m.put(ArmorItem.Type.CHESTPLATE, chest);
        m.put(ArmorItem.Type.LEGGINGS, legs);
        m.put(ArmorItem.Type.BOOTS, boots);
        return m;
    }
//TODO: Сбалансировать
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> COPPER = ARMOR_MATERIALS.register("copper",
            () -> new ArmorMaterial(
                    /* defense map */ def(2, 6, 5, 2),
                    /* enchantability */ 12,
                    /* equip sound (Holder) */ SoundEvents.ARMOR_EQUIP_GENERIC,
                    /* repair ingredient (Supplier) */ (Supplier<Ingredient>) () -> Ingredient.of(Items.COPPER_INGOT),
                    /* vanilla 2D layers (не нужны для GeckoLib) */ List.<ArmorMaterial.Layer>of(),
                    /* toughness */ 0.0f,
                    /* knockback  */ 0.0f
            ));

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> LINEN = ARMOR_MATERIALS.register(
            "linen", () -> new ArmorMaterial(
                    /* defense map */ def(2, 6, 5, 2),
                    /* enchantability */ 12,
                    /* equip sound (Holder) */ SoundEvents.ARMOR_EQUIP_GENERIC,
                    /* repair ingredient (Supplier) */ (Supplier<Ingredient>) () -> Ingredient.of(Items.COPPER_INGOT),
                    /* vanilla 2D layers (не нужны для GeckoLib) */ List.<ArmorMaterial.Layer>of(),
                    /* toughness */ 0.0f,
                    /* knockback  */ 0.0f
            ));

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> RAWHIDE = ARMOR_MATERIALS.register(
            "rawhide", () -> new ArmorMaterial(
                    /* defense map */ def(2, 6, 5, 2),
                    /* enchantability */ 12,
                    /* equip sound (Holder) */ SoundEvents.ARMOR_EQUIP_GENERIC,
                    /* repair ingredient (Supplier) */ (Supplier<Ingredient>) () -> Ingredient.of(Items.COPPER_INGOT),
                    /* vanilla 2D layers (не нужны для GeckoLib) */ List.<ArmorMaterial.Layer>of(),
                    /* toughness */ 0.0f,
                    /* knockback  */ 0.0f
            ));

    // Возвращаем Holder<ArmorMaterial> для humanoidArmor(...)
    public static Holder<ArmorMaterial> copperHolder() {
        // В NeoForge 21.1 это Holder.Reference<T>
        return COPPER.getDelegate();
        // Если у вас есть getHolder(): return COPPER.getHolder().orElseThrow();
    }
}
