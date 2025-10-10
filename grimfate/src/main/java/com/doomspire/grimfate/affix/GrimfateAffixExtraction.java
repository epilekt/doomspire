package com.doomspire.grimfate.affix;

import com.doomspire.grimcore.affix.Affix;
import com.doomspire.grimcore.affix.AffixAggregator;
import com.doomspire.grimfate.compat.curios.CuriosCompat;
import com.doomspire.grimfate.registry.ModDataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
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

    private static final String MODID = "grimfate";

    // Теги категорий
    private static final TagKey<Item> TAG_WEAPONS =
            TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MODID, "loot/weapons"));
    private static final TagKey<Item> TAG_SHIELDS =
            TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MODID, "loot/shields"));
    private static final TagKey<Item> TAG_ARMORS =
            TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MODID, "loot/armors"));

    public static List<AffixAggregator.AffixEntry> extractFromEntity(LivingEntity entity) {
        var b = new AffixAggregator.ListBuilder();

        // 1) MAIN-HAND — ТОЛЬКО оружие
        ItemStack main = entity.getMainHandItem();
        if (isWeapon(main)) {
            appendFromStack(main, b, Affix.Source.WEAPON);
        }

        // 2) OFF-HAND — щит (или оружие, если ты этого хочешь; по умолчанию — только щит)
        ItemStack off = entity.getOffhandItem();
        if (isShield(off)) {
            appendFromStack(off, b, Affix.Source.SHIELD);
        }

        // 3) Броня — строго из armor-слотов
        for (ItemStack armor : entity.getArmorSlots()) {
            if (!armor.isEmpty()) {
                appendFromStack(armor, b, Affix.Source.ARMOR);
            }
        }

        // 4) Curios — бижутерия
        if (CuriosCompat.isLoaded()) {
            CuriosCompat.forEachEquipped(entity, (stack, slotId) -> {
                if (!stack.isEmpty()) appendFromStack(stack, b, Affix.Source.JEWELRY);
            });
        }

        // 5) Бафы/ауры — TODO позже

        return b.build();
    }

    // === типизация ===

    private static boolean isWeapon(ItemStack st) {
        if (st == null || st.isEmpty()) return false;
        Item it = st.getItem();
        // теги твоего мода
        if (st.is(TAG_WEAPONS)) return true;
        // ванильные классы-оружие
        return it instanceof SwordItem
                || it instanceof AxeItem       // многие «секиры» — оружие
                || it instanceof BowItem
                || it instanceof CrossbowItem
                || it instanceof TridentItem;
    }

    private static boolean isShield(ItemStack st) {
        if (st == null || st.isEmpty()) return false;
        if (st.is(TAG_SHIELDS)) return true;
        return st.getItem() instanceof ShieldItem;
    }

    // === чтение компонента и добавление в агрегатор ===

    private static void appendFromStack(ItemStack stack, AffixAggregator.ListBuilder b, Affix.Source src) {
        if (stack == null || stack.isEmpty()) return;
        readFromStack(stack, src, b);
    }

    /**
     * Чтение аффиксов из data-component'а ModDataComponents.AFFIX_LIST.
     * Суммируем роллы в единую величину и добавляем запись для агрегатора.
     */
    private static void readFromStack(ItemStack stack, Affix.Source src, AffixAggregator.ListBuilder b) {
        var comp = stack.get(ModDataComponents.AFFIX_LIST.get());
        if (comp == null) return;

        var list = comp.entries();
        if (list == null || list.isEmpty()) return;

        for (var e : list) {
            if (e == null) continue;

            ResourceLocation id = ResourceLocation.tryParse(e.id());
            if (id == null) continue;

            float mag = 0f;
            var rolls = e.rolls();
            if (rolls != null && !rolls.isEmpty()) {
                for (Float r : rolls) {
                    if (r == null) continue;
                    if (Float.isNaN(r) || Float.isInfinite(r) || r == 0f) continue;
                    mag += r;
                }
            }
            if (mag == 0f) continue;

            b.add(id, mag, src);
        }
    }
}
