package com.doomspire.grimfate.affix;

import com.doomspire.grimcore.affix.Affix;
import com.doomspire.grimcore.affix.AffixAggregator;
import com.doomspire.grimfate.compat.curios.CuriosCompat;
import com.doomspire.grimfate.registry.ModDataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.List;

//NOTE: Сторона контента: собираем аффиксы со всех источников сущности.
/**
 * Источники:
 *  - main hand / offhand (WEAPON/SHIELD),
 *  - броня по слотам (ARMOR),
 *  - Curios (JEWELRY), если мод загружен,
 *  - временные бафы/ауры (позже).
 */
public final class GrimfateAffixExtraction {
    private GrimfateAffixExtraction() {}

    public static List<AffixAggregator.AffixEntry> extractFromEntity(LivingEntity entity) {
        var b = new AffixAggregator.ListBuilder();

        // 1) Оружие/щит
        appendFromStack(entity.getMainHandItem(), b, Affix.Source.WEAPON);
        appendFromStack(entity.getOffhandItem(), b, guessOffhandSource(entity.getOffhandItem()));

        // 2) Броня
        for (ItemStack armor : entity.getArmorSlots()) {
            appendFromStack(armor, b, Affix.Source.ARMOR);
        }

        // 3) Curios (бижутерия), если мод загружен
        if (CuriosCompat.isLoaded()) {
            // как только появится реальный обход — раскомментировать и использовать
            // CuriosCompat.forEachEquipped(entity, (stack, slotId) -> {
            //     appendFromStack(stack, b, Affix.Source.JEWELRY);
            // });
        }

        // 4) Бафы/ауры — TODO позже

        return b.build();
    }

    private static Affix.Source guessOffhandSource(ItemStack offhand) {
        // Пока считаем оффхенд щитом, если предмет помечен тегом grimfate:shields — добавим позже.
        // До ввода тегов возвращаем SHIELD как безопасную эвристику.
        return Affix.Source.SHIELD;
    }

    private static void appendFromStack(ItemStack stack, AffixAggregator.ListBuilder b, Affix.Source src) {
        if (stack == null || stack.isEmpty()) return;
        readFromStack(stack, src, b);
    }

    /**
     * Чтение аффиксов из data-component'а {@link com.doomspire.grimfate.item.comp.AffixListComponent}.
     * Суммируем все роллы аффикса в единую величину (magnitude) и добавляем запись для агрегатора.
     *
     * Формат компонента:
     *   entries: [{ id: "namespace:affix_id", rolls: [ ...floats... ] }, ...]
     */
    private static void readFromStack(ItemStack stack, Affix.Source src, AffixAggregator.ListBuilder b) {
        var comp = stack.get(ModDataComponents.AFFIX_LIST.get());
        if (comp == null) return;

        for (var e : comp.entries()) {
            if (e == null) continue;

            // Парсим ID
            ResourceLocation id = ResourceLocation.tryParse(e.id());
            if (id == null) continue; // некорректный id — пропускаем

            // Сводим список роллов к одной величине (по умолчанию — сумма)
            float mag = 0f;
            var rolls = e.rolls();
            if (rolls != null) {
                for (Float r : rolls) {
                    if (r != null && !Float.isNaN(r) && !Float.isInfinite(r)) {
                        mag += r;
                    }
                }
            }

            // Пропускаем «пустые» величины
            if (mag == 0f) continue;

            b.add(id, mag, src);
        }
    }
}

