package com.doomspire.grimcore.datapack.codec;

import com.doomspire.grimcore.stat.Attributes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.EnumMap;
import java.util.Map;

public record AttributesBalance(Map<Attributes, Rule> byAttr) {

    public static final Codec<Rule> RULE_CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.INT.optionalFieldOf("cap", 99).forGetter(Rule::cap),
            Codec.DOUBLE.optionalFieldOf("max_health_per", 5.0).forGetter(Rule::maxHealthPer),
            Codec.DOUBLE.optionalFieldOf("max_mana_per", 5.0).forGetter(Rule::maxManaPer),
            Codec.DOUBLE.optionalFieldOf("regen_hp_per", 0.05).forGetter(Rule::regenHpPer),
            Codec.DOUBLE.optionalFieldOf("regen_mp_per", 0.05).forGetter(Rule::regenMpPer),
            Codec.DOUBLE.optionalFieldOf("crit_chance_per", 0.0).forGetter(Rule::critChancePer),
            Codec.DOUBLE.optionalFieldOf("evasion_per", 0.0).forGetter(Rule::evasionPer),
            Codec.DOUBLE.optionalFieldOf("melee_damage_per", 0.0).forGetter(Rule::meleeDamagePer),
            Codec.DOUBLE.optionalFieldOf("spell_power_per", 0.0).forGetter(Rule::spellPowerPer),
            Codec.DOUBLE.optionalFieldOf("cast_speed_per", 0.0).forGetter(Rule::castSpeedPer)
    ).apply(i, Rule::new));

    public static final Codec<Attributes> ATTR_CODEC =
            Codec.STRING.xmap(s -> Attributes.valueOf(s.toUpperCase()), a -> a.name().toLowerCase());

    public static final Codec<Map<Attributes, Rule>> MAP_CODEC =
            Codec.unboundedMap(ATTR_CODEC, RULE_CODEC).xmap(m -> {
                EnumMap<Attributes, Rule> map = new EnumMap<>(Attributes.class);
                map.putAll(m);
                return map;
            }, m -> m);

    public static final Codec<AttributesBalance> CODEC =
            RecordCodecBuilder.create(i -> i.group(
                    MAP_CODEC.fieldOf("attributes").forGetter(AttributesBalance::byAttr)
            ).apply(i, AttributesBalance::new));

    public static AttributesBalance defaults() {
        EnumMap<Attributes, Rule> def = new EnumMap<>(Attributes.class);
        for (Attributes a : Attributes.values()) {
            int cap = (a == Attributes.EVASION) ? 100 : 99;

            double maxHealthPer = (a == Attributes.VITALITY) ? 6.0 : 0.0;
            double regenHpPer   = (a == Attributes.VITALITY) ? 0.06 : 0.0;

            double maxManaPer   = (a == Attributes.SPIRIT) ? 10.0 : 0.0;
            double regenMpPer   = (a == Attributes.SPIRIT) ? 0.08 : 0.0;

            double meleePer     = (a == Attributes.STRENGTH) ? 0.7 : 0.0;
            double spellPer     = (a == Attributes.INTELLIGENCE) ? 0.7 : 0.0;
            double castPer      = (a == Attributes.DEXTERITY) ? 0.5 : 0.0;
            double evasionPer   = (a == Attributes.EVASION) ? 0.5 : 0.0;

            def.put(a, new Rule(cap, maxHealthPer, maxManaPer, regenHpPer, regenMpPer,
                    0.0, evasionPer, meleePer, spellPer, castPer));
        }
        return new AttributesBalance(def);
    }

    public int cap(Attributes a) { return byAttr.getOrDefault(a, defaults().byAttr.get(a)).cap; }

    public String summary() { return "attrs=" + byAttr.size(); }

    public record Rule(
            int cap,
            double maxHealthPer,
            double maxManaPer,
            double regenHpPer,
            double regenMpPer,
            double critChancePer,
            double evasionPer,
            double meleeDamagePer,
            double spellPowerPer,
            double castSpeedPer
    ) {}
}
