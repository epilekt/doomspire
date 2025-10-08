package com.doomspire.grimfate.registry;

import com.doomspire.grimfate.core.Grimfate;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.SimpleTier;

/**
 * Оружейные материалы (Tier) для инструментов/мечей.
 * 1.21.1: используем SimpleTier (NeoForge) и передаём его в SwordItem/прочие TieredItem.
 *
 * Как применять (пример для меча):
 *   new SwordItem(
 *       // сам tier
 *       ModWeaponMaterials.COPPER,
 *       // свойства предмета + атрибуты урона/скорости через createAttributes
 *       new Item.Properties().attributes(
 *           net.minecraft.world.item.SwordItem.createAttributes(
 *               ModWeaponMaterials.COPPER,
 *                type-specific damage bonus 3,
 *                attack speed -2.4f
 *           )
 *       )
 *   );
 *
 * Примечание: в 1.21.1 конструкторы инструментов не принимают raw-значения урона/скорости;
 * их нужно класть в Properties#attributes(...) через соответствующий createAttributes(...) у класса инструмента.
 */
public final class ModWeaponMaterials {

    private ModWeaponMaterials() {}

    /**
     * Медный tier для мечей/инструментов.
     * Размещаем «между камнем и железом».
     *
     * Пояснение параметров SimpleTier:
     *  - incorrectBlocksForDrops: какой тег блоков НЕЛЬЗЯ добывать этим tier (берём каменный как базу).
     *  - uses: прочность (stone=131, iron=250). Ставим 200.
     *  - speed: скорость копания (stone=4.0f, iron=6.0f). Ставим 5.0f.
     *  - attackDamageBonus: бонус к урону tier (для меча финальный = 4 + это значение).
     *  - enchantmentValue: «лучше золота» = 22, «хуже золота» — ставим 20 или 12 по балансу. Пусть 12 как в твоей броне.
     *  - repairIngredient: чем чинить (медь).
     */
    public static final Tier COPPER = new SimpleTier(
            // какие блоки НЕ дропаются при этом tier — берём как у камня
            BlockTags.INCORRECT_FOR_STONE_TOOL,
            // uses (durability)
            300,
            // speed (копание)
            5.0f,
            // attackDamageBonus (для меча добавится к базовым 4)
            0.0f,
            // enchantability
            12,
            // repair ingredient
            () -> Ingredient.of(Items.COPPER_INGOT)
    );

    /**
     * Деревянный tier для посохов.
     * Логика: по прочности и энчантабилити близко к WOOD, но это твой самостоятельный tier.
     * Если посохи — не «инструменты» в смысле добычи, всё равно удобно хранить их боевой tier тут.
     */
    public static final Tier WOOD_STAFF = new SimpleTier(
            // для дерева есть готовый тег «некорректных» блоков
            BlockTags.INCORRECT_FOR_WOODEN_TOOL,
            // uses
            59,           // как у дерев. инструментов
            // speed
            2.0f,         // дерево = медленно
            // attackDamageBonus
            0.0f,         // базовый бонус минимальный, добираем уроны атрибутами предмета
            // enchantability
            15,           // как у ванильного дерева
            // repair: любые доски (tag)
            () -> Ingredient.of(Items.STICK)
    );

    // Если захочешь «свой» тег для логики добычи — создай свой TagKey<Block> и подставь сюда вместо BlockTags.INCORRECT_FOR_*:
    // public static final TagKey<Block> INCORRECT_FOR_COPPER_TOOL = TagKey.create(Registries.BLOCK, new ResourceLocation(Grimfate.MODID, "incorrect_for_copper_tool"));
    // и опиши его в data/<modid>/tags/blocks/incorrect_for_copper_tool.json
}

