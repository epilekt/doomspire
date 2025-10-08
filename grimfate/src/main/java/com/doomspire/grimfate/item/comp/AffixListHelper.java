package com.doomspire.grimfate.item.comp;

import com.doomspire.grimcore.affix.Affix;
import com.doomspire.grimfate.affix.RollService;
import com.doomspire.grimfate.registry.ModDataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

/*
//NOTE: Утилиты для чтения/записи компонента аффиксов на ItemStack и удобный «имбью» (ролл + запись).
/**
 * Работает поверх data-component'а {@link AffixListComponent}, зарегистрированного как
 * {@code ModDataComponents.AFFIX_LIST}. Сторона контента (grimfate). См. регистрацию компонентов: ModDataComponents.
 *
 * Поток использования:
 *  - {@link #get(ItemStack)} / {@link #has(ItemStack)} — проверка и чтение.
 *  - {@link #set(ItemStack, AffixListComponent)} / {@link #clear(ItemStack)} — запись/очистка.
 *  - {@link #rollAndApply(ItemStack, Affix.Source, int, RandomSource)} — единый «имбью»: выбрать аффиксы и записать их в предмет.
 *
 * Совместимость:
 *  - Чтение используется в GrimfateAffixExtraction.readFromStack() (ядро агрегатора читает аффиксы из компонента).
 */
public final class AffixListHelper {
    private AffixListHelper() {}

    /** Есть ли компонент аффиксов на предмете. */
    public static boolean has(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        return stack.get(ModDataComponents.AFFIX_LIST.get()) != null;
    }

    /** Прочитать компонент аффиксов. */
    public static Optional<AffixListComponent> get(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return Optional.empty();
        return Optional.ofNullable(stack.get(ModDataComponents.AFFIX_LIST.get()));
    }

    /** Записать компонент аффиксов. */
    public static void set(ItemStack stack, AffixListComponent comp) {
        if (stack == null || stack.isEmpty() || comp == null) return;
        stack.set(ModDataComponents.AFFIX_LIST.get(), comp);
    }

    /** Удалить компонент аффиксов. */
    public static void clear(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return;
        stack.remove(ModDataComponents.AFFIX_LIST.get());
    }

    /** Собрать набор item-тегов предмета (используется при фильтрации пулов RollService'ом). */
    public static Set<TagKey<Item>> collectItemTags(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return Collections.emptySet();
        var holder = stack.getItem().builtInRegistryHolder();
        return holder.tags()
                .filter(t -> t.registry().location().equals(Registries.ITEM.location()))
                .map(t -> (TagKey<Item>) t)
                .collect(Collectors.toCollection(HashSet::new));
    }

    /** Пустой ли компонент (нет записей). */
    private static boolean isEmptyComponent(AffixListComponent comp) {
        if (comp == null) return true;
        var entries = comp.entries();
        return (entries == null || entries.isEmpty());
    }

    /**
     * Имбью: сгенерировать аффиксы и записать их в предмет.
     * ВАЖНО: если результат пустой — компонент не пишется (или удаляется), чтобы не ломать стакинг материалов.
     */
    public static void rollAndApply(ItemStack stack, Affix.Source source, int itemLevel, RandomSource random) {
        if (stack == null || stack.isEmpty()) return;

        var tags = collectItemTags(stack);
        var comp = RollService.roll(stack.getItem(), tags, source, itemLevel, random);

        if (isEmptyComponent(comp)) {
            clear(stack);        // не храним пустой компонент — сохраняем нормальный стакинг
        } else {
            set(stack, comp);
        }
    }
}
