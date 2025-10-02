package com.doomspire.grimfate.compat.bettercombat;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.neoforged.fml.ModList;
import org.slf4j.Logger;

//NOTE: Мягкая интеграция с Better Combat (без жёсткой зависимости)
/**
 * Задача «моста» — централизовать:
 *  1) проверку наличия мода,
 *  2) константы с их тегами (Item Tags), чтобы удобно маппить наши теги в data-ресурсах,
 *  3) место для будущих утилит, если понадобится.
 *
 * ВАЖНО:
 *  - Реальная привязка к анимациям происходит через data-теги (JSON),
 *    где мы включаем наши теги (#grimfate:daggers, #grimfate:swords, #grimfate:two_handed …)
 *    в их теги (bettercombat:daggers, bettercombat:swords, …).
 *  - Этот класс НЕ тянет никаких API Better Combat во время компиляции.
 */
public final class BetterCombatBridge {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final String MODID = "bettercombat";

    private BetterCombatBridge() {}

    /** Есть ли установлен Better Combat. */
    public static boolean isLoaded() {
        return ModList.get().isLoaded(MODID);
    }

    /** Вызвать из commonSetup для логов диагностики. */
    public static void init() {
        if (isLoaded()) {
            LOGGER.info("[Grimfate] Better Combat detected. Weapon tags will be bridged via data pack.");
        } else {
            LOGGER.warn("[Grimfate] Better Combat NOT detected. Using vanilla/Gecko animations only.");
        }
    }

    // ---------- Их основные item-теги (называем ровно как у них; используем только как ссылки в ресурсах) ----------

    public static final TagKey<Item> BC_SWORDS      = itemTag("swords");
    public static final TagKey<Item> BC_DAGGERS     = itemTag("daggers");
    public static final TagKey<Item> BC_AXES        = itemTag("axes");
    public static final TagKey<Item> BC_MACES       = itemTag("maces");
    public static final TagKey<Item> BC_HAMMERS     = itemTag("hammers");
    public static final TagKey<Item> BC_SPEARS      = itemTag("spears");
    public static final TagKey<Item> BC_POLEARMS    = itemTag("polearms");
    public static final TagKey<Item> BC_GREATSWORDS = itemTag("greatswords");
    public static final TagKey<Item> BC_GREATAXES   = itemTag("greataxes");
    public static final TagKey<Item> BC_STAVES      = itemTag("staves"); // если у них есть такой набор (покрываем на будущее)

    /** Наш общий флаг-свойство (двуручность) — маппится в группы greatswords/greataxes/spears/… через data. */
    public static final TagKey<Item> GF_TWO_HANDED  =
            TagKey.create(net.minecraft.core.registries.Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath("grimfate", "two_handed"));

    private static TagKey<Item> itemTag(String path) {
        return TagKey.create(net.minecraft.core.registries.Registries.ITEM,
                ResourceLocation.fromNamespaceAndPath(MODID, path));
    }
}
