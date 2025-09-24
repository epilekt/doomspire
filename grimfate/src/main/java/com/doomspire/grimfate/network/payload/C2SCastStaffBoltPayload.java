package com.doomspire.grimfate.network.payload;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record C2SCastStaffBoltPayload() implements CustomPacketPayload {
    public static final Type<C2SCastStaffBoltPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("grimfate", "c2s_cast_staff_bolt"));

    // Пустой payload — всё считаем на сервере
    public static final StreamCodec<RegistryFriendlyByteBuf, C2SCastStaffBoltPayload> STREAM_CODEC =
            StreamCodec.of((buf, msg) -> {}, buf -> new C2SCastStaffBoltPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}

