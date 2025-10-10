package com.doomspire.grimcore.stat;

import com.doomspire.grimcore.affix.AffixAggregator;
import com.doomspire.grimcore.attach.PlayerStatsAttachment;
import com.doomspire.grimcore.datapack.Balance;
import com.doomspire.grimcore.datapack.codec.AttributesBalance;
import net.minecraft.world.entity.LivingEntity;

import java.util.function.Function;

// NOTE: Пересчитывает агрегированные статы (StatSnapshot) на основе атрибутов, предметов и бонусов.
// NOTE: Баланс (коэффициенты на 1 очко атрибута) берётся из datapack: data/<ns>/balance/attributes.json.
//       Все константы ниже имеют фолбэки, если в датапаке поля не определены.

public class StatCalculator {

    // ---- helpers to read rules from Balance ----
    private static AttributesBalance.Rule rule(Attributes attr) {
        var m = Balance.attributes().byAttr();
        return m != null ? m.get(attr) : null;
    }

    // double getter -> float result (для *_per c типом double в кодеке)
    private static float readD(AttributesBalance.Rule r,
                               java.util.function.ToDoubleFunction<AttributesBalance.Rule> getter,
                               float def) {
        if (r == null) return def;
        return (float) getter.applyAsDouble(r);
    }

    // float getter -> float result (для новых полей с типом float в кодеке)
    private static float readF(AttributesBalance.Rule r,
                               java.util.function.Function<AttributesBalance.Rule, Float> getter,
                               float def) {
        if (r == null) return def;
        Float v = getter.apply(r);
        return v != null ? v : def;
    }

    public static StatSnapshot calculate(PlayerStatsAttachment att) {
        StatSnapshot snapshot = new StatSnapshot();

        int vit    = att.getAttribute(Attributes.VITALITY);
        int str    = att.getAttribute(Attributes.STRENGTH);
        int intel  = att.getAttribute(Attributes.INTELLIGENCE);
        int spirit = att.getAttribute(Attributes.SPIRIT);
        int dex    = att.getAttribute(Attributes.DEXTERITY);
        int eva    = att.getAttribute(Attributes.EVASION);

        var rVit = rule(Attributes.VITALITY);
        var rSpr = rule(Attributes.SPIRIT);
        var rDex = rule(Attributes.DEXTERITY);
        var rStr = rule(Attributes.STRENGTH);
        var rInt = rule(Attributes.INTELLIGENCE);
        var rEva = rule(Attributes.EVASION);

        // --------- БАЗА: можно читать из datapack (см. комментарий-шаблон ниже) ---------
        // ⚠ Если хочешь настраивать базу из datapack, раскомментируй блок ниже
        //   и удали хардкоды:
        //
        // var base = Balance.attributes().base();
        // final float BASE_HP  = base.baseMaxHealth();
        // final float BASE_HPR = base.baseRegenHealth();
        // final float BASE_MP  = base.baseMaxMana();
        // final float BASE_MPR = base.baseRegenMana();
        //
        final float BASE_HP  = 100f;
        final float BASE_HPR = 1f;
        final float BASE_MP  = 100f;
        final float BASE_MPR = 1f;

        // Vitality → HP/HP-regen
        float hpPerVit  = readD(rVit, AttributesBalance.Rule::maxHealthPer, 20f);
        float hprPerVit = readD(rVit, AttributesBalance.Rule::regenHpPer,   1f);

        snapshot.maxHealth   = BASE_HP  + vit * hpPerVit;
        snapshot.regenHealth = BASE_HPR + vit * hprPerVit;

        // Spirit → MP/MP-regen
        float mpPerSpr  = readD(rSpr, AttributesBalance.Rule::maxManaPer, 30f);
        float mprPerSpr = readD(rSpr, AttributesBalance.Rule::regenMpPer,  3f);

        snapshot.maxMana   = BASE_MP  + spirit * mpPerSpr;
        snapshot.regenMana = BASE_MPR + spirit * mprPerSpr;

        // Уроновые коэффициенты
        float physMeleePerStr  = readF(rStr, AttributesBalance.Rule::physMeleePer,   0.03f);
        float physRangedPerDex = readF(rDex, AttributesBalance.Rule::physRangedPer,  0.03f);
        float elemPerInt       = readF(rInt, AttributesBalance.Rule::elemDamagePer,  0.02f);

        snapshot.damage.put(DamageTypes.PHYS_MELEE,  str * physMeleePerStr);
        snapshot.damage.put(DamageTypes.PHYS_RANGED, dex * physRangedPerDex);
        snapshot.damage.put(DamageTypes.FIRE,        intel * elemPerInt);
        snapshot.damage.put(DamageTypes.FROST,       intel * elemPerInt);
        snapshot.damage.put(DamageTypes.LIGHTNING,   intel * elemPerInt);
        snapshot.damage.put(DamageTypes.POISON,      intel * elemPerInt);

        // Базовые боёвые параметры (аффиксы прилетят позже через Aggregator)
        snapshot.critChance    = readF(rDex, AttributesBalance.Rule::baseCritChance, 0f);
        snapshot.critDamage    = readF(rDex, AttributesBalance.Rule::baseCritDamage, 0.5f);
        snapshot.lifesteal     = readF(rStr, AttributesBalance.Rule::baseLifesteal,  0f);
        snapshot.manasteal     = readF(rSpr, AttributesBalance.Rule::baseManasteal,  0f);
        snapshot.evasionChance = eva * readF(rEva, AttributesBalance.Rule::evasionChancePer, 0.01f);

        // Скорость передвижения (в процентах для StatEffects → MOVEMENT_SPEED)
        snapshot.moveSpeedPct  = dex * readF(rDex, AttributesBalance.Rule::moveSpeedPctPer, 0.25f);

        snapshot.damageReductionAll = clamp01(snapshot.damageReductionAll);
        snapshot.evasionChance      = clamp01(snapshot.evasionChance);
        return snapshot;
    }

    private static void applyResistAll(StatSnapshot s) {
        if (s.resistAll == 0f) return;
        float m = 1f + s.resistAll;
        for (var k : s.resistances.keySet()) {
            s.resistances.computeIfPresent(k, (kk, v) -> v * m);
        }
    }

    /**
     * Полный расчёт со всеми модификаторами предметов/аффиксов.
     * ВНИМАНИЕ: owner может быть null — тогда вернётся чистая версия без аффиксов.
     */
    public static StatSnapshot calculateWithAffixes(PlayerStatsAttachment att, LivingEntity owner) {
        StatSnapshot snap = calculate(att);
        if (owner != null) {
            AffixAggregator.applyAll(snap, owner);
            applyResistAll(snap);
        }
        return snap;
    }

    private static float clamp01(float v) {
        if (Float.isNaN(v) || Float.isInfinite(v)) return 0f;
        return Math.max(0f, Math.min(1f, v));
    }
}
