package com.doomspire.grimcore.stat;

import com.doomspire.grimcore.attach.PlayerStatsAttachment;


//NOTE:Пересчитывает агрегированные статы (StatSnapshot) на основе атрибутов, предметов и бонусов.
//NOTE:Вызов: при изменении атрибутов/уровня/экипировки/эффектов.

public class StatCalculator {

    public static StatSnapshot calculate(PlayerStatsAttachment att) {
        StatSnapshot snapshot = new StatSnapshot();

        // Атрибуты
        int vit = att.getAttribute(Attributes.VITALITY);
        int str = att.getAttribute(Attributes.STRENGTH);
        int intl = att.getAttribute(Attributes.INTELLIGENCE);
        int spirit = att.getAttribute(Attributes.SPIRIT);
        int dex = att.getAttribute(Attributes.DEXTERITY);
        int eva = att.getAttribute(Attributes.EVASION);

        // Примеры формул (значения можно вынести в JSON balance/attributes.json)
        snapshot.maxHealth   = 100 + vit * 20;
        snapshot.regenHealth = 1 + vit * 1;

        snapshot.maxMana   = 100 + spirit * 30;
        snapshot.regenMana = 1 + spirit * 3;

        snapshot.damage.put(DamageTypes.PHYS_MELEE,  str * 0.03f);
        snapshot.damage.put(DamageTypes.PHYS_RANGED, dex * 0.03f);
        snapshot.damage.put(DamageTypes.FIRE,        intl * 0.02f);
        snapshot.damage.put(DamageTypes.FROST,       intl * 0.02f);
        snapshot.damage.put(DamageTypes.LIGHTNING,   intl * 0.02f);
        snapshot.damage.put(DamageTypes.POISON,      intl * 0.02f);

        snapshot.critChance     = 0f;       // 1% за очко — добавим позже при балансировке
        snapshot.critDamage     = 0.5f;     // +50% базово
        snapshot.lifesteal      = 0f;
        snapshot.manasteal      = 0f;
        snapshot.evasionChance  = eva * 0.01f;
        snapshot.moveSpeedPct   = dex * 0.25D; // 0.25% за 1 DEX

        // Глобальная редукция входящего урона.
        // Пока = 0, позже сюда добавим:
        //  - AffixAggregator (из экипировки/бижутерии/аур),
        //  - классовые/баф-эффекты,
        //  - возможные бонусы от атрибутов/скилл-дерева.
        snapshot.damageReductionAll = 0f;

        // TODO: сюда позже подключим бонусы предметов и аффиксы через агрегатор статов

        // Клампы безопасных диапазонов (если потребуется)
        snapshot.evasionChance = clamp01(snapshot.evasionChance);
        snapshot.damageReductionAll = clamp01(snapshot.damageReductionAll);

        return snapshot;
    }

    private static float clamp01(float v) {
        if (Float.isNaN(v) || Float.isInfinite(v)) return 0f;
        return Math.max(0f, Math.min(1f, v));
    }
}
