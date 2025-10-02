package com.doomspire.grimcore.affix;

import com.doomspire.grimcore.stat.StatSnapshot;
import net.minecraft.resources.ResourceLocation;

//NOTE: Базовый контракт аффикса.
/**
 * Ядро (grimcore) не знает, как хранятся аффиксы на предметах — это задача контент-модуля.
 * Здесь — только применение к StatSnapshot и метаданные.
 *
 * Допущения:
 * - "magnitude" — числовая сила/вес аффикса, уже нормализованная контентом (например, 0.12f для +12%).
 * - "source" — откуда пришёл аффикс (броня, оружие, бижутерия/curios) — на случай разных формул.
 * - Применение идемпотентно и НЕ должно читать/менять состояние предметов, только модифицировать снапшот.
 */
public interface Affix {

    /**
     * Уникальный идентификатор аффикса (например, grimcore:dr_all, grimcore:fire_resist).
     */
    ResourceLocation id();

    /**
     * Применить эффект аффикса к снапшоту.
     *
     * @param outSnapshot целевой снапшот, в который суммируются эффекты
     * @param magnitude   сила аффикса (уже приведённая в доли/единицы)
     * @param source      источник (тип носителя аффикса)
     */
    void apply(StatSnapshot outSnapshot, float magnitude, Source source);

    /**
     * Короткий человекочитаемый ключ для тултипа/логов (без локализации).
     * Полноценный локализованный текст делаем на стороне клиента в grimfate.
     */
    default String tooltipKey() {
        return id().toString();
    }

    /**
     * Источник аффикса — может влиять на формулу (например, бонусы с оружия чаще «оффенсивные»).
     */
    enum Source {
        WEAPON,
        ARMOR,
        JEWELRY,   // кольца/амулеты (включая Curios)
        SHIELD,
        OTHER
    }
}

