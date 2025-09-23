package com.doomspire.grimcore.datapack.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record LevelsCurve(int maxLevel, double base, double growth) {

    public static final Codec<LevelsCurve> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.INT.fieldOf("max_level").forGetter(LevelsCurve::maxLevel),
            Codec.DOUBLE.fieldOf("base").forGetter(LevelsCurve::base),
            Codec.DOUBLE.fieldOf("growth").forGetter(LevelsCurve::growth)
    ).apply(i, LevelsCurve::new));

    public static LevelsCurve defaults() {
        return new LevelsCurve(50, 100.0, 1.10); // прежние дефолты, пока не пришли данные из датапака
    }

    public String summary() {
        return "max=" + maxLevel + ", base=" + base + ", growth=" + growth;
    }
}

