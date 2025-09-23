package com.doomspire.grimcore.datapack.codec;

import com.doomspire.grimcore.spell.api.SpellSchool;
import com.doomspire.grimcore.spell.api.SpellTag;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Map;

public record SpellTuning(Map<ResourceLocation, Entry> byId) {

    public static final Codec<ResourceLocation> RL_CODEC = ResourceLocation.CODEC;

    public static final Codec<SpellSchool> SCHOOL_CODEC =
            Codec.STRING.xmap(s -> SpellSchool.valueOf(s.toUpperCase()), v -> v.name().toLowerCase());

    public static final Codec<SpellTag> TAG_CODEC =
            Codec.STRING.xmap(s -> SpellTag.valueOf(s.toUpperCase()), v -> v.name().toLowerCase());

    public static final Codec<Scaling> SCALING_CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.DOUBLE.optionalFieldOf("vitality", 0.0).forGetter(Scaling::vitality),
            Codec.DOUBLE.optionalFieldOf("strength", 0.0).forGetter(Scaling::strength),
            Codec.DOUBLE.optionalFieldOf("intelligence", 0.0).forGetter(Scaling::intelligence),
            Codec.DOUBLE.optionalFieldOf("spirit", 0.0).forGetter(Scaling::spirit),
            Codec.DOUBLE.optionalFieldOf("dexterity", 0.0).forGetter(Scaling::dexterity),
            Codec.DOUBLE.optionalFieldOf("evasion", 0.0).forGetter(Scaling::evasion)
    ).apply(i, Scaling::new));

    public static final Codec<Entry> ENTRY_CODEC = RecordCodecBuilder.create(i -> i.group(
            SCHOOL_CODEC.fieldOf("school").forGetter(Entry::school),
            Codec.list(TAG_CODEC).optionalFieldOf("tags", List.of()).forGetter(Entry::tags),
            Codec.INT.optionalFieldOf("base_cost", 0).forGetter(Entry::baseCost),
            Codec.INT.optionalFieldOf("base_cooldown", 0).forGetter(Entry::baseCooldown),
            SCALING_CODEC.optionalFieldOf("scaling", Scaling.ZERO).forGetter(Entry::scaling),
            Codec.list(Codec.STRING).optionalFieldOf("forbidden_weapons", List.of()).forGetter(Entry::forbiddenWeapons),
            Codec.list(Codec.STRING).optionalFieldOf("allowed_armor_tags", List.of()).forGetter(Entry::allowedArmorTags)
    ).apply(i, Entry::new));

    public static final Codec<Map<ResourceLocation, Entry>> MAP_CODEC =
            Codec.unboundedMap(RL_CODEC, ENTRY_CODEC);

    public static final Codec<SpellTuning> CODEC =
            RecordCodecBuilder.create(i -> i.group(
                    MAP_CODEC.fieldOf("spells").forGetter(SpellTuning::byId)
            ).apply(i, SpellTuning::new));

    /** Значения по умолчанию — пустой набор. */
    public static SpellTuning defaults() {
        return new SpellTuning(Map.of());
    }

    /** Короткий summary для логов. */
    public String summary() {
        return "spells=" + (byId != null ? byId.size() : 0);
    }

    // --- types ---

    public record Entry(
            SpellSchool school,
            List<SpellTag> tags,
            int baseCost,
            int baseCooldown,
            Scaling scaling,
            List<String> forbiddenWeapons,
            List<String> allowedArmorTags
    ) {}

    public record Scaling(
            double vitality,
            double strength,
            double intelligence,
            double spirit,
            double dexterity,
            double evasion
    ) {
        public static final Scaling ZERO = new Scaling(0,0,0,0,0,0);
    }
}
