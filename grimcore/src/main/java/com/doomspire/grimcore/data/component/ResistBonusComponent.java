package com.doomspire.grimcore.data.component;

import com.doomspire.grimcore.stat.DamageTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.EnumMap;
import java.util.Map;

public record ResistBonusComponent(Map<DamageTypes, Float> byType) {
    public static final Codec<DamageTypes> TYPE_CODEC =
            Codec.STRING.xmap(s -> DamageTypes.valueOf(s.toUpperCase()), t -> t.name().toLowerCase());

    public static final Codec<Map<DamageTypes, Float>> MAP_CODEC = Codec.unboundedMap(TYPE_CODEC, Codec.FLOAT);

    public static final Codec<ResistBonusComponent> CODEC = RecordCodecBuilder.create(i -> i.group(
            MAP_CODEC.fieldOf("by_type").forGetter(ResistBonusComponent::byType)
    ).apply(i, ResistBonusComponent::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ResistBonusComponent> STREAM_CODEC =
            new StreamCodec<>() {
                @Override public ResistBonusComponent decode(RegistryFriendlyByteBuf buf) {
                    int n = buf.readVarInt();
                    Map<DamageTypes, Float> map = new EnumMap<>(DamageTypes.class);
                    for (int k = 0; k < n; k++) {
                        DamageTypes t = DamageTypes.valueOf(buf.readUtf().toUpperCase());
                        map.put(t, buf.readFloat());
                    }
                    return new ResistBonusComponent(map);
                }
                @Override public void encode(RegistryFriendlyByteBuf buf, ResistBonusComponent v) {
                    var m = v.byType();
                    buf.writeVarInt(m.size());
                    m.forEach((t, f) -> { buf.writeUtf(t.name().toLowerCase()); buf.writeFloat(f); });
                }
            };
}

