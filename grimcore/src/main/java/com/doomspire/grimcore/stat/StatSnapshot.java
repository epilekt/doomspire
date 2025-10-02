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

    public StatSnapshot() {
        for (DamageTypes type : DamageTypes.values()) {
            damage.put(type, 0f);
        }
        for (ResistTypes type : ResistTypes.values()) {
            resistances.put(type, 0f);
        }
        damageReductionAll = 0f;
    }
}
