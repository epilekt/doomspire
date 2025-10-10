package com.doomspire.grimcore.stat;

import java.util.EnumMap;
//NOTE: Кэшированное состояние всех характеристик игрока/моба
/**
 * Используется в боевом движке и GUI.
 */
public class StatSnapshot {
    // Ресурсы
    public float maxHealth;
    public float regenHealth;
    public float maxMana;
    public float regenMana;

    // Урон
    public EnumMap<DamageTypes, Float> damage = new EnumMap<>(DamageTypes.class);

    // Резисты
    public EnumMap<ResistTypes, Float> resistances = new EnumMap<>(ResistTypes.class);

    // Боевые модификаторы
    public float critChance;
    public float critDamage;
    public float lifesteal;
    public float manasteal;
    public float evasionChance;
    public double moveSpeedPct; // бонус к скорости в процентах (например 7.5 = +7.5%)

    /**
     * Глобальная редукция входящего урона (после резистов), 0..1.
     * Пример: 0.20f = -20% ко всему входящему урону.
     * Рассчитывается в StatCalculator из аффиксов/бафов/источников защиты.
     */
    public float damageReductionAll;

    // ===== НОВЫЕ поля (добавляем) =====
    /** Плоская броня: уменьшает входящий урон по своей формуле (см. калькулятор). */
    public float armorFlat = 0f;

    /** Общий множитель ко всем резистам, если хочешь «резист ко всем стихиям» одной ручкой. */
    public float resistAll = 0f;            // доля: 0.10 = +10% ко всем resistances

    /** Мультипликатор скорости атаки: 0.10 = +10% к базовой скорости. */
    public float attackSpeed = 0f;


    // при необходимости: blockChance, moveSpeed, critDamage, etc.

    public StatSnapshot copy() {
        StatSnapshot out = new StatSnapshot();
        out.maxHealth = this.maxHealth;
        out.regenHealth = this.regenHealth;
        out.maxMana = this.maxMana;
        out.regenMana = this.regenMana;
        out.critChance = this.critChance;
        out.damageReductionAll = this.damageReductionAll;

        out.damage.putAll(this.damage);
        out.resistances.putAll(this.resistances);

        // новые
        out.armorFlat = this.armorFlat;
        out.resistAll = this.resistAll;
        out.attackSpeed = this.attackSpeed;
        out.lifesteal = this.lifesteal;
        out.manasteal = this.manasteal;
        out.evasionChance = this.evasionChance;

        return out;
    }

    public void reset() {
        maxHealth = regenHealth = 0f;
        maxMana = regenMana = 0f;
        critChance = 0f;
        damageReductionAll = 0f;
        damage.clear();
        resistances.clear();

        armorFlat = 0f;
        resistAll = 0f;
        attackSpeed = 0f;
        lifesteal = 0f;
        manasteal = 0f;
        evasionChance = 0f;
    }

    /** Сложение снапшотов (аккумуляция эффектов). */
    public void add(StatSnapshot other) {
        if (other == null) return;
        maxHealth += other.maxHealth;
        regenHealth += other.regenHealth;
        maxMana += other.maxMana;
        regenMana += other.regenMana;
        critChance += other.critChance;
        damageReductionAll += other.damageReductionAll;

        for (var e : other.damage.entrySet()) {
            damage.merge(e.getKey(), e.getValue(), Float::sum);
        }
        for (var e : other.resistances.entrySet()) {
            resistances.merge(e.getKey(), e.getValue(), Float::sum);
        }

        // новые
        armorFlat += other.armorFlat;
        resistAll += other.resistAll;
        attackSpeed += other.attackSpeed;
        lifesteal += other.lifesteal;
        manasteal += other.manasteal;
        evasionChance += other.evasionChance;
    }
}

    //public StatSnapshot() {
    //    for (DamageTypes type : DamageTypes.values()) {
    //        damage.put(type, 0f);
    //    }
    //    for (ResistTypes type : ResistTypes.values()) {
    //        resistances.put(type, 0f);
    //    }
    //    damageReductionAll = 0f;
    //}

