package com.doomspire.grimcore.data.component;

import com.doomspire.grimcore.stat.Attributes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * Иммутабельный data-component с бонусами к атрибутам, напр.:
 * {"strength": 3, "intelligence": 2}
 *
 * Требования NeoForge к компонентам:
 *  - иммутабельность
 *  - корректные equals/hashCode
 */
public final class StatBonusComponent {

    private final EnumMap<Attributes, Integer> bonus; // иммутабельное содержимое

    public StatBonusComponent(Map<Attributes, Integer> map) {
        EnumMap<Attributes, Integer> tmp = new EnumMap<>(Attributes.class);
        if (map != null) {
            for (var e : map.entrySet()) {
                if (e.getKey() != null) {
                    int v = e.getValue() == null ? 0 : e.getValue();
                    if (v != 0) tmp.put(e.getKey(), v);
                }
            }
        }
        this.bonus = tmp.isEmpty() ? new EnumMap<>(Attributes.class) : new EnumMap<>(tmp);
    }

    /** Возвращает бонус для заданного атрибута (0 если не задан). */
    public int get(Attributes a) {
        Integer v = bonus.get(a);
        return v == null ? 0 : v;
    }

    /** Неизменяемое представление всех бонусов. */
    public Map<Attributes, Integer> all() {
        return Collections.unmodifiableMap(bonus);
    }

    // --- Codec/StreamCodec ---
    public static final Codec<Attributes> ATTR_CODEC =
            Codec.STRING.xmap(s -> Attributes.valueOf(s.toUpperCase()), a -> a.name().toLowerCase());

    public static final Codec<StatBonusComponent> CODEC =
            Codec.unboundedMap(ATTR_CODEC, Codec.INT)
                    .xmap(StatBonusComponent::new, c -> c.all());

    public static final StreamCodec<RegistryFriendlyByteBuf, StatBonusComponent> STREAM_CODEC =
            StreamCodec.of(
                    (buf, c) -> {
                        var map = c.bonus;
                        ByteBufCodecs.VAR_INT.encode(buf, map.size());
                        for (var e : map.entrySet()) {
                            ByteBufCodecs.fromCodec(ATTR_CODEC).encode(buf, e.getKey());
                            ByteBufCodecs.VAR_INT.encode(buf, e.getValue());
                        }
                    },
                    buf -> {
                        int n = ByteBufCodecs.VAR_INT.decode(buf);
                        EnumMap<Attributes, Integer> m = new EnumMap<>(Attributes.class);
                        for (int i = 0; i < n; i++) {
                            Attributes a = ByteBufCodecs.fromCodec(ATTR_CODEC).decode(buf);
                            int v = ByteBufCodecs.VAR_INT.decode(buf);
                            if (v != 0) m.put(a, v);
                        }
                        return new StatBonusComponent(m);
                    }
            );

    // --- equals/hashCode (по содержимому карты) ---
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StatBonusComponent that)) return false;
        return Objects.equals(bonus, that.bonus);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bonus);
    }

    @Override
    public String toString() {
        return "StatBonusComponent" + bonus;
    }
}
