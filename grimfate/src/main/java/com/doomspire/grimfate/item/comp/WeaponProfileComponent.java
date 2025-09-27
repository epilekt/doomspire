package com.doomspire.grimfate.item.comp;

import com.doomspire.grimfate.combat.WeaponType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record WeaponProfileComponent(
        WeaponType type,
        boolean twoHanded,
        float baseSpeed,
        float baseDamageMod,
        int affixSlots
) {
    public static final Codec<WeaponProfileComponent> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.xmap(WeaponType::valueOf, WeaponType::name).fieldOf("type").forGetter(WeaponProfileComponent::type),
            Codec.BOOL.fieldOf("two_handed").forGetter(WeaponProfileComponent::twoHanded),
            Codec.FLOAT.fieldOf("base_speed").forGetter(WeaponProfileComponent::baseSpeed),
            Codec.FLOAT.fieldOf("base_damage_mod").forGetter(WeaponProfileComponent::baseDamageMod),
            Codec.INT.fieldOf("affix_slots").forGetter(WeaponProfileComponent::affixSlots)
    ).apply(i, WeaponProfileComponent::new));

    /** Для сетевой синхронизации data-component (NeoForge 1.21.1). */
    public static final StreamCodec<RegistryFriendlyByteBuf, WeaponProfileComponent> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public WeaponProfileComponent decode(RegistryFriendlyByteBuf buf) {
                    WeaponType t = WeaponType.valueOf(buf.readUtf());
                    boolean two = buf.readBoolean();
                    float sp = buf.readFloat();
                    float dm = buf.readFloat();
                    int slots = buf.readVarInt();
                    return new WeaponProfileComponent(t, two, sp, dm, slots);
                }
                @Override
                public void encode(RegistryFriendlyByteBuf buf, WeaponProfileComponent v) {
                    buf.writeUtf(v.type().name());
                    buf.writeBoolean(v.twoHanded());
                    buf.writeFloat(v.baseSpeed());
                    buf.writeFloat(v.baseDamageMod());
                    buf.writeVarInt(v.affixSlots());
                }
            };
}
