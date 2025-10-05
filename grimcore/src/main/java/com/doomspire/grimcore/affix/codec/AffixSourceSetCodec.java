package com.doomspire.grimcore.affix.codec;

import com.doomspire.grimcore.affix.Affix;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

/*
//NOTE: Codec для множеств Affix.Source. Поддерживает: строку, массив строк, спец-значение "ALL".
 *
 * Поддерживаемые формы JSON:
 *   "allow_sources": "WEAPON"
 *   "allow_sources": ["WEAPON", "ARMOR", "JEWELRY"]
 *   "allow_sources": "ALL"
 *
 * Регистр не важен (weapon == WEAPON).
 */
public final class AffixSourceSetCodec implements Codec<Set<Affix.Source>> {

    public static final AffixSourceSetCodec INSTANCE = new AffixSourceSetCodec();

    private static final Codec<Set<Affix.Source>> ARRAY =
            Codec.STRING.listOf().flatXmap(
                    list -> {
                        try {
                            final EnumSet<Affix.Source> out = EnumSet.noneOf(Affix.Source.class);
                            for (String s : list) {
                                parseTokenInto(out, s);
                            }
                            return DataResult.success(out);
                        } catch (IllegalArgumentException ex) {
                            return DataResult.error(() -> "Affix.Source parse error: " + ex.getMessage());
                        }
                    },
                    set -> DataResult.success(set.stream().map(Enum::name).toList())
            );

    private static final Codec<Set<Affix.Source>> SINGLE =
            Codec.STRING.flatXmap(
                    s -> {
                        try {
                            final EnumSet<Affix.Source> out = EnumSet.noneOf(Affix.Source.class);
                            parseTokenInto(out, s);
                            return DataResult.success(out);
                        } catch (IllegalArgumentException ex) {
                            return DataResult.error(() -> "Affix.Source parse error: " + ex.getMessage());
                        }
                    },
                    set -> {
                        // для сериализации одной строкой берём либо "ALL", либо первую константу
                        if (set.size() == Affix.Source.values().length) {
                            return DataResult.success("ALL");
                        }
                        final String first = set.iterator().next().name();
                        return DataResult.success(first);
                    }
            );

    private AffixSourceSetCodec() {}

    @Override
    public <T> DataResult<T> encode(Set<Affix.Source> input, com.mojang.serialization.DynamicOps<T> ops, T prefix) {
        // если выбраны все значения — сериализуем как "ALL" (короче и нагляднее)
        if (input != null && input.size() == Affix.Source.values().length) {
            return Codec.STRING.encode("ALL", ops, prefix);
        }
        // иначе — массив строк
        return ARRAY.encode(input == null ? EnumSet.noneOf(Affix.Source.class) : input, ops, prefix);
    }

    @Override
    public <T> DataResult<Pair<Set<Affix.Source>, T>> decode(DynamicOps<T> ops, T input) {
        // пробуем как строку; если не строка — как массив
        final DataResult<String> asString = Codec.STRING.parse(ops, input);
        if (asString.result().isPresent()) {
            return SINGLE.decode(ops, input);
        }
        return ARRAY.decode(ops, input);
    }

    private static void parseTokenInto(EnumSet<Affix.Source> out, String token) {
        final String t = token.trim().toUpperCase(Locale.ROOT);
        if (t.isEmpty()) throw new IllegalArgumentException("empty token");

        if ("ALL".equals(t)) {
            out.addAll(Arrays.asList(Affix.Source.values()));
            return;
        }
        try {
            out.add(Affix.Source.valueOf(t));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("unknown source '" + token + "'. Allowed: " +
                    "'ALL', " + Arrays.toString(Affix.Source.values()));
        }
    }
}
