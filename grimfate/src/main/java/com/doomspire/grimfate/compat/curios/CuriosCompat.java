package com.doomspire.grimfate.compat.curios;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;

import java.util.function.BiConsumer;

/**
 * Мягкая интеграция с Curios. Сейчас содержит только проверку наличия.
 * Позже сюда добавим реальное чтение экипированных curios-предметов,
 * чтобы складывать их StatBonus-компоненты в агрегатор статов.
 */
public final class CuriosCompat {
    public static final String MODID = "curios";

    private CuriosCompat() {}

    public static boolean isLoaded() {
        return ModList.get().isLoaded(MODID);
    }

    /**
     * Заглушка: когда подключим API-вызовы Curios, сюда добавим обход всех слотов Curios
     * и вызов consumer.accept(stack, slotId).
     */
    public static void forEachEquipped(LivingEntity entity, BiConsumer<ItemStack, String> consumer) {
        if (!isLoaded()) return;
        // TODO: реализовать через CuriosApi при добавлении API в компиляцию агрегатора
    }
}

