package com.doomspire.grimcore.datapack.codec;

import com.doomspire.grimcore.stat.Attributes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;

/**
 * Баланс атрибутов: коэффициенты на 1 очко атрибута, + опциональная базовая секция.
 * Загружается из datapack: data/<ns>/balance/attributes.json
 */
public record AttributesBalance(Map<String, Rule> byAttr, Base base) {

    public static final Codec<AttributesBalance> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.unboundedMap(Codec.STRING, Rule.CODEC).fieldOf("by_attr").forGetter(AttributesBalance::byAttr),
            Base.CODEC.optionalFieldOf("base", Base.DEFAULT).forGetter(AttributesBalance::base)
    ).apply(i, AttributesBalance::new));

    /** Безопасные дефолты, если файл отсутствует. */
    public static AttributesBalance defaults() {
        return new AttributesBalance(Collections.emptyMap(), Base.DEFAULT);
    }

    // +++ НОВОЕ: удобный доступ по enum'у
    public Rule get(Attributes attr) {
        if (attr == null || byAttr == null) return null;
        // пробуем ключи: lower-case, UPPER, как есть
        String k1 = attr.name().toLowerCase(Locale.ROOT);
        Rule r = byAttr.get(k1);
        if (r != null) return r;
        Rule r2 = byAttr.get(attr.name());
        if (r2 != null) return r2;
        return byAttr.get(attr.toString());
    }

    // +++ НОВОЕ: cap() для PlayerStatsAttachment
    public int cap(Attributes attr) {
        Rule r = get(attr);
        return r != null ? r.cap() : 99;
    }

    // +++ НОВОЕ: summary() для BalanceData
    public String summary() {
        int n = (byAttr != null) ? byAttr.size() : 0;
        return "AttributesBalance{rules=" + n
                + ", baseHP=" + (base != null ? base.baseMaxHealth() : Base.DEFAULT.baseMaxHealth())
                + ", baseMP=" + (base != null ? base.baseMaxMana() : Base.DEFAULT.baseMaxMana())
                + "}";
    }

    /** Коэффициенты для конкретного атрибута (на 1 очко). */
    public record Rule(
            int cap,
            double maxHealthPer,
            double regenHpPer,
            double maxManaPer,
            double regenMpPer,

            float physMeleePer,
            float physRangedPer,
            float elemDamagePer,
            float moveSpeedPctPer,

            float baseCritChance,
            float baseCritDamage,
            float baseLifesteal,
            float baseManasteal,
            float evasionChancePer
    ) {
        public static final Codec<Rule> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.INT.optionalFieldOf("cap", 99).forGetter(Rule::cap),

                Codec.DOUBLE.optionalFieldOf("max_health_per", 0.0).forGetter(Rule::maxHealthPer),
                Codec.DOUBLE.optionalFieldOf("regen_hp_per",   0.0).forGetter(Rule::regenHpPer),

                Codec.DOUBLE.optionalFieldOf("max_mana_per",  0.0).forGetter(Rule::maxManaPer),
                Codec.DOUBLE.optionalFieldOf("regen_mp_per",  0.0).forGetter(Rule::regenMpPer),

                Codec.FLOAT.optionalFieldOf("phys_melee_per",     0.0f).forGetter(Rule::physMeleePer),
                Codec.FLOAT.optionalFieldOf("phys_ranged_per",    0.0f).forGetter(Rule::physRangedPer),
                Codec.FLOAT.optionalFieldOf("elem_damage_per",    0.0f).forGetter(Rule::elemDamagePer),
                Codec.FLOAT.optionalFieldOf("move_speed_pct_per", 0.0f).forGetter(Rule::moveSpeedPctPer),

                Codec.FLOAT.optionalFieldOf("base_crit_chance",   0.0f).forGetter(Rule::baseCritChance),
                Codec.FLOAT.optionalFieldOf("base_crit_damage",   0.5f).forGetter(Rule::baseCritDamage),
                Codec.FLOAT.optionalFieldOf("base_lifesteal",     0.0f).forGetter(Rule::baseLifesteal),
                Codec.FLOAT.optionalFieldOf("base_manasteal",     0.0f).forGetter(Rule::baseManasteal),
                Codec.FLOAT.optionalFieldOf("evasion_chance_per", 0.01f).forGetter(Rule::evasionChancePer)
        ).apply(i, Rule::new));
    }

    /** Базовые (additive) значения, не зависящие от атрибутов. */
    public record Base(
            float baseMaxHealth,
            float baseRegenHealth,
            float baseMaxMana,
            float baseRegenMana
    ) {
        public static final Base DEFAULT = new Base(100f, 1f, 100f, 1f);

        public static final Codec<Base> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.FLOAT.optionalFieldOf("max_health",   100f).forGetter(Base::baseMaxHealth),
                Codec.FLOAT.optionalFieldOf("regen_health", 1f).forGetter(Base::baseRegenHealth),
                Codec.FLOAT.optionalFieldOf("max_mana",     100f).forGetter(Base::baseMaxMana),
                Codec.FLOAT.optionalFieldOf("regen_mana",   1f).forGetter(Base::baseRegenMana)
        ).apply(i, Base::new));
    }
}
