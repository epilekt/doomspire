package com.doomspire.grimcore.data.component;

import com.doomspire.grimcore.spell.api.SpellSchool;
import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

/**
 * Разрешённые дисциплины (школы), при которых предмет активирует бонусы/эффект.
 * Например: только WARCRY/TECHNIQUE.
 *
 * Иммутабельный компонент с корректными equals/hashCode.
 */
public final class ClassRestrictionComponent {
    private final EnumSet<SpellSchool> allowed; // внутреннее хранилище

    public ClassRestrictionComponent(Set<SpellSchool> allowed) {
        EnumSet<SpellSchool> s = allowed == null || allowed.isEmpty()
                ? EnumSet.noneOf(SpellSchool.class)
                : EnumSet.copyOf(allowed);
        this.allowed = s;
    }

    /** Пустой набор = разрешено всем. */
    public boolean isAllowed(SpellSchool school) {
        return allowed.isEmpty() || allowed.contains(school);
    }

    /** Неизменяемое представление. */
    public Set<SpellSchool> allowed() {
        return Collections.unmodifiableSet(allowed);
    }

    public static final Codec<SpellSchool> SCHOOL_CODEC =
            Codec.STRING.xmap(s -> SpellSchool.valueOf(s.toUpperCase()), v -> v.name().toLowerCase());

    public static final Codec<ClassRestrictionComponent> CODEC =
            SCHOOL_CODEC.listOf().xmap(list -> new ClassRestrictionComponent(Set.copyOf(list)),
                    c -> java.util.List.copyOf(c.allowed()));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClassRestrictionComponent> STREAM_CODEC =
            StreamCodec.of(
                    (buf, c) -> {
                        ByteBufCodecs.VAR_INT.encode(buf, c.allowed.size());
                        for (var s : c.allowed) ByteBufCodecs.fromCodec(SCHOOL_CODEC).encode(buf, s);
                    },
                    buf -> {
                        int n = ByteBufCodecs.VAR_INT.decode(buf);
                        EnumSet<SpellSchool> set = EnumSet.noneOf(SpellSchool.class);
                        for (int i = 0; i < n; i++) set.add(ByteBufCodecs.fromCodec(SCHOOL_CODEC).decode(buf));
                        return new ClassRestrictionComponent(set);
                    }
            );

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClassRestrictionComponent that)) return false;
        return Objects.equals(allowed, that.allowed);
    }

    @Override
    public int hashCode() {
        return Objects.hash(allowed);
    }

    @Override
    public String toString() {
        return "ClassRestrictionComponent" + allowed;
    }
}
