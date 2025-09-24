package com.doomspire.grimfate.network.payload;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record C2SCastSpellSlotPayload(int slot) implements CustomPacketPayload {
    public static final Type<C2SCastSpellSlotPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("grimfate", "c2s_cast_spell_slot"));
    public static final StreamCodec<RegistryFriendlyByteBuf, C2SCastSpellSlotPayload> STREAM_CODEC =
            StreamCodec.of((buf, msg) -> ByteBufCodecs.VAR_INT.encode(buf, msg.slot),
                    buf -> new C2SCastSpellSlotPayload(ByteBufCodecs.VAR_INT.decode(buf)));

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}

