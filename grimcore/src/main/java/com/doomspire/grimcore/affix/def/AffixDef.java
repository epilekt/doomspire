package com.doomspire.grimcore.affix.def;

import com.doomspire.grimcore.affix.Affix;
import com.doomspire.grimcore.affix.codec.AffixSourceSetCodec;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

/*
//NOTE: Data-driven описание одного аффикса (границы величины, единицы, кривые ролла, ограничения по источникам).
 */
public record AffixDef(
        ResourceLocation id,
        String displayKey,
        ValueUnit unit,
        float min,
        float max,
        Curve curve,
        Set<Affix.Source> allowSources,
        boolean stackable,
        boolean clampTotal
) {
    // ---- Codecs ----

    public static final Codec<AffixDef> CODEC = RecordCodecBuilder.create(i -> i.group(
            ResourceLocation.CODEC.fieldOf("id").forGetter(AffixDef::id),
            Codec.STRING.fieldOf("display_key").forGetter(AffixDef::displayKey),
            ValueUnit.CODEC.fieldOf("unit").forGetter(AffixDef::unit),
            Codec.FLOAT.fieldOf("min").forGetter(AffixDef::min),
            Codec.FLOAT.fieldOf("max").forGetter(AffixDef::max),
            Curve.CODEC.fieldOf("curve").forGetter(AffixDef::curve),
            AffixSourceSetCodec.INSTANCE.optionalFieldOf("allow_sources", allSources()).forGetter(AffixDef::allowSources),
            Codec.BOOL.optionalFieldOf("stackable", false).forGetter(AffixDef::stackable),
            Codec.BOOL.optionalFieldOf("clamp_total", true).forGetter(AffixDef::clampTotal)
    ).apply(i, AffixDef::new));

    private static Set<Affix.Source> allSources() {
        EnumSet<Affix.Source> s = EnumSet.noneOf(Affix.Source.class);
        for (Affix.Source v : Affix.Source.values()) s.add(v);
        return s;
    }

    // ---- Invariants ----

    public AffixDef {
        if (displayKey == null || displayKey.isBlank())
            throw new IllegalArgumentException("display_key must be non-empty for " + id);
        if (Float.isNaN(min) || Float.isNaN(max) || Float.isInfinite(min) || Float.isInfinite(max))
            throw new IllegalArgumentException("min/max must be finite for " + id + " (min=" + min + ", max=" + max + ")");
        if (max < min)
            throw new IllegalArgumentException("max < min for " + id + " (min=" + min + ", max=" + max + ")");
        if (allowSources == null || allowSources.isEmpty())
            throw new IllegalArgumentException("allow_sources cannot be empty for " + id);
    }

    // ---- Sampling helpers ----

    /** Семпл «сырого» значения в [min; max] по заданной кривой. */
    public float sampleBase(RandomSource rng) {
        final float t = Mth.clamp(curve.sample01(rng), 0f, 1f);
        return min + (max - min) * t;
    }

    /** Кламп суммарного значения (например после масштабирования редкостью). */
    public float clampTotal(float value) {
        return clampTotal ? Mth.clamp(value, min, max) : value;
    }

    // ---- Units ----

    public enum ValueUnit {
        FLAT,
        PERCENT;

        public static final Codec<ValueUnit> CODEC =
                Codec.STRING.xmap(
                        s -> ValueUnit.valueOf(s.trim().toUpperCase(Locale.ROOT)),
                        e -> e.name().toLowerCase(Locale.ROOT)
                );
    }

    // ---- Curves ----

    /**
     * Кривая распределения t в [0..1], чтобы управлять «редкостью» высоких/низких роллов.
     * Реализовано через dispatch по полю "type" (важно: вызываем ИМЕННО С КОДЕКА СТРОК — Codec.STRING.dispatch(...)).
     */
    public sealed interface Curve permits Curve.Uniform, Curve.Gaussian, Curve.Logistic, Curve.BiasLow, Curve.BiasHigh {
        public static final com.mojang.serialization.Codec<Curve> CODEC = com.mojang.serialization.Codec.STRING.dispatch(
                "type",
                Curve::typeKey,
                key -> switch (key) {
                    case "UNIFORM" -> Uniform.CODEC;
                    case "GAUSSIAN" -> Gaussian.CODEC;
                    case "LOGISTIC" -> Logistic.CODEC;
                    case "BIAS_LOW" -> BiasLow.CODEC;
                    case "BIAS_HIGH" -> BiasHigh.CODEC;
                    default -> Uniform.CODEC; // безопасный дефолт
                }
        );

        /** Строковый идентификатор типа (например, "UNIFORM"). */
        String typeKey();

        /** Вернуть значение t в диапазоне [0..1]. */
        float sample01(RandomSource rng);

        // --- Реализации ---

        /** Равномерное распределение. { "type": "UNIFORM" } */
        record Uniform() implements Curve {
            static final com.mojang.serialization.MapCodec<Uniform> CODEC =
                    com.mojang.serialization.MapCodec.unit(new Uniform());
            @Override public String typeKey() { return "UNIFORM"; }
            @Override public float sample01(RandomSource rng) { return rng.nextFloat(); }
        }

        /** Гауссово распределение (центр 0.5). { "type":"GAUSSIAN", "spread": 1.0 } */
        record Gaussian(float spread) implements Curve {
            static final com.mojang.serialization.MapCodec<Gaussian> CODEC =
                    RecordCodecBuilder.mapCodec(i -> i.group(
                            Codec.FLOAT.optionalFieldOf("spread", 1.0f).forGetter(Gaussian::spread)
                    ).apply(i, Gaussian::new));
            @Override public String typeKey() { return "GAUSSIAN"; }
            @Override public float sample01(RandomSource rng) {
                double u1 = Math.max(1e-7, rng.nextDouble());
                double u2 = rng.nextDouble();
                double z0 = Math.sqrt(-2.0 * Math.log(u1)) * Math.cos(2.0 * Math.PI * u2);
                double sigma = Math.max(1e-4, spread) / 6.0;
                double x = 0.5 + z0 * sigma;
                return (float) Mth.clamp(x, 0.0, 1.0);
            }
        }

        /** Логистическое распределение (S-curve). { "type":"LOGISTIC", "spread": 1.0 } */
        record Logistic(float spread) implements Curve {
            static final com.mojang.serialization.MapCodec<Logistic> CODEC =
                    RecordCodecBuilder.mapCodec(i -> i.group(
                            Codec.FLOAT.optionalFieldOf("spread", 1.0f).forGetter(Logistic::spread)
                    ).apply(i, Logistic::new));
            @Override public String typeKey() { return "LOGISTIC"; }
            @Override public float sample01(RandomSource rng) {
                double u = Mth.clamp(rng.nextDouble(), 1e-7, 1 - 1e-7);
                double k = Math.max(1e-4, spread);
                double t = 1.0 / (1.0 + Math.pow(1.0 / u - 1.0, 1.0 / k));
                return (float) Mth.clamp(t, 0.0, 1.0);
            }
        }

        /** Смещение к низким значениям. { "type":"BIAS_LOW", "power": 2.0 } */
        record BiasLow(float power) implements Curve {
            static final com.mojang.serialization.MapCodec<BiasLow> CODEC =
                    RecordCodecBuilder.mapCodec(i -> i.group(
                            Codec.FLOAT.optionalFieldOf("power", 2.0f).forGetter(BiasLow::power)
                    ).apply(i, BiasLow::new));
            @Override public String typeKey() { return "BIAS_LOW"; }
            @Override public float sample01(RandomSource rng) {
                float u = rng.nextFloat();
                float p = Math.max(1.0f, power);
                return (float) Math.pow(u, p);
            }
        }

        /** Смещение к высоким значениям. { "type":"BIAS_HIGH", "power": 2.0 } */
        record BiasHigh(float power) implements Curve {
            static final com.mojang.serialization.MapCodec<BiasHigh> CODEC =
                    RecordCodecBuilder.mapCodec(i -> i.group(
                            Codec.FLOAT.optionalFieldOf("power", 2.0f).forGetter(BiasHigh::power)
                    ).apply(i, BiasHigh::new));
            @Override public String typeKey() { return "BIAS_HIGH"; }
            @Override public float sample01(RandomSource rng) {
                float u = rng.nextFloat();
                float p = Math.max(1.0f, power);
                return 1.0f - (float) Math.pow(1.0f - u, p);
            }
        }
    }
}
