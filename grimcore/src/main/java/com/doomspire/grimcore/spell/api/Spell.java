package com.doomspire.grimcore.spell.api;

import net.minecraft.resources.ResourceLocation;

import java.util.Set;

/** Базовый контракт любого спелла. Реализации — stateless-singleton. */
public interface Spell {
    /** Уникальный id, например grimfate:fire_bolt */
    ResourceLocation id();

    /** Школа (для фильтров/баланса/синергий). */
    SpellSchool school();

    /** Стоимость маны (базовая, до модификаторов). */
    int manaCost(SpellContext ctx);

    /** Кулдаун в тиках (базовый, до модификаторов). */
    int cooldownTicks(SpellContext ctx);

    /** Основной вызов кастования. Возвращает результат (успешно/недостаточно ресурса/на кулдауне...). */
    CastResult cast(SpellContext ctx);

    Set<SpellTag> tags();
}
