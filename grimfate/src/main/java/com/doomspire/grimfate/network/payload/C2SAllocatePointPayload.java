package com.doomspire.grimfate.network.payload;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record C2SAllocatePointPayload(String attributeId) implements CustomPacketPayload {
    public static final Type<C2SAllocatePointPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("grimfate", "c2s_allocate_point"));

    public static final StreamCodec<RegistryFriendlyByteBuf, C2SAllocatePointPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, C2SAllocatePointPayload::attributeId,
                    C2SAllocatePointPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
