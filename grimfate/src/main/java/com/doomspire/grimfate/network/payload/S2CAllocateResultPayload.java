package com.doomspire.grimfate.network.payload;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record S2CAllocateResultPayload(String attributeId, int newAllocated, int unspent)
        implements CustomPacketPayload {

    public static final Type<S2CAllocateResultPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("grimfate", "s2c_allocate_result"));

    public static final StreamCodec<RegistryFriendlyByteBuf, S2CAllocateResultPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, S2CAllocateResultPayload::attributeId,
                    ByteBufCodecs.VAR_INT,    S2CAllocateResultPayload::newAllocated,
                    ByteBufCodecs.VAR_INT,    S2CAllocateResultPayload::unspent,
                    S2CAllocateResultPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
