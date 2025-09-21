package com.doomspire.grimcore.attach;

import com.doomspire.grimcore.stat.*;

public final class MobStatCalculator {
    private MobStatCalculator(){}

    public static StatSnapshot calculate(MobStatsAttachment att) {
        StatSnapshot s = new StatSnapshot();

        int vit  = att.getAttribute(Attributes.VITALITY);
        int str  = att.getAttribute(Attributes.STRENGTH);
        int intl = att.getAttribute(Attributes.INTELLIGENCE);
        int dex  = att.getAttribute(Attributes.DEXTERITY);
        int eva  = att.getAttribute(Attributes.EVASION);

        // База для мобов (потом вынесем в датапак balance)
        s.maxHealth   = 60 + vit * 20;
        s.regenHealth = 0 + vit * 1;

        // у мобов нет маны
        s.maxMana   = 0;
        s.regenMana = 0;

        // Базовый урон (только физика для старта)
        s.damage.put(DamageTypes.PHYS_MELEE, 3f + str * 0.5f);
        s.damage.put(DamageTypes.PHYS_RANGED, dex * 0.5f);

        // Элементы оставим 0 по умолчанию (будут у кастомных мобов)
        s.damage.putIfAbsent(DamageTypes.FIRE, 0f);
        s.damage.putIfAbsent(DamageTypes.FROST, 0f);
        s.damage.putIfAbsent(DamageTypes.LIGHTNING, 0f);
        s.damage.putIfAbsent(DamageTypes.POISON, 0f);

        // Защита/уклон/крит как старт
        s.resistances.put(ResistTypes.PHYS, 0.0f);
        s.resistances.put(ResistTypes.FIRE, 0.0f);
        s.resistances.put(ResistTypes.FROST, 0.0f);
        s.resistances.put(ResistTypes.LIGHTNING, 0.0f);
        s.resistances.put(ResistTypes.POISON, 0.0f);

        s.evasionChance = Math.min(0.5f, eva * 0.005f); // 0.5% за очко, кап 50%
        s.critChance = 0f;
        s.critDamage = 0.25f;

        return s;
    }
}

