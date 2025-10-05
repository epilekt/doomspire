package com.doomspire.grimcore.affix.rarity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;

/*
//NOTE: Data-driven описание тира редкости для системы аффиксов.
// Хранится и грузится через JSON (датапак), без UI-зависимостей.
//
// Ожидаемый JSON (совместим с примерами, что мы добавили):
// {
//   "id": "grimfate:rare",
//   "weight": 10,
//   "max_affixes": { "type": "uniform", "min_inclusive": 2, "max_inclusive": 3 },
//   "rolls_per_affix": { "type": "constant", "value": 1 },
//   "magnitude_scale": 1.15,
//   "display_key": "rarity.grimfate.rare",
//   "text_color": 16755200,
//   "allow_duplicates": false
// }
*/
public record RarityDef(
        ResourceLocation id,
        int weight,
        IntProvider maxAffixes,
        IntProvider rollsPerAffix,
        float magnitudeScale,
        String displayKey,
        int textColor,
        boolean allowDuplicates
) {
    public static final Codec<RarityDef> CODEC = RecordCodecBuilder.create(i -> i.group(
            ResourceLocation.CODEC.fieldOf("id").forGetter(RarityDef::id),
            Codec.intRange(0, Integer.MAX_VALUE).fieldOf("weight").forGetter(RarityDef::weight),
            IntProvider.CODEC.optionalFieldOf("max_affixes", ConstantInt.of(0)).forGetter(RarityDef::maxAffixes),
            IntProvider.CODEC.optionalFieldOf("rolls_per_affix", ConstantInt.of(1)).forGetter(RarityDef::rollsPerAffix),
            Codec.FLOAT.optionalFieldOf("magnitude_scale", 1.0f).forGetter(RarityDef::magnitudeScale),
            Codec.STRING.optionalFieldOf("display_key", "").forGetter(RarityDef::displayKey),
            Codec.INT.optionalFieldOf("text_color", 0xFFFFFF).forGetter(RarityDef::textColor),
            Codec.BOOL.optionalFieldOf("allow_duplicates", false).forGetter(RarityDef::allowDuplicates)
    ).apply(i, RarityDef::new));

    // --- Invariants / guards ---
    public RarityDef {
        if (id == null) throw new IllegalArgumentException("rarity.id is null");
        if (weight < 0) throw new IllegalArgumentException("rarity.weight < 0 for " + id);
        if (Float.isNaN(magnitudeScale) || Float.isInfinite(magnitudeScale)) {
            throw new IllegalArgumentException("rarity.magnitude_scale must be finite for " + id);
        }
    }

    // --- Helpers used by RollService ---

    /** Выбрать «сколько аффиксов» даёт эта редкость. */
    public int sampleMaxAffixes(RandomSource rng) {
        int v = maxAffixes.sample(rng);
        return Math.max(0, v);
    }

    /** Выбрать «сколько роллов» делать на один аффикс при генерации (среднее увеличивает точность/сглаженность). */
    public int sampleRollsPerAffix(RandomSource rng) {
        int v = rollsPerAffix.sample(rng);
        return Mth.clamp(v, 1, 16); // разумный предел
    }

    /** Применить множитель «силы» редкости к сэмплу аффикса. */
    public float scaleMagnitude(float value) {
        return value * magnitudeScale;
    }
}
