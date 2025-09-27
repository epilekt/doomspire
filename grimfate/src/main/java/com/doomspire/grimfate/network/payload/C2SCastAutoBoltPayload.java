package com.doomspire.grimfate.network.payload;

import com.doomspire.grimfate.core.Grimfate;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;

public record C2SCastAutoBoltPayload(InteractionHand hand) implements CustomPacketPayload {
    public static final Type<C2SCastAutoBoltPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Grimfate.MODID, "c2s_cast_auto_bolt"));

    public static final StreamCodec<RegistryFriendlyByteBuf, C2SCastAutoBoltPayload> STREAM_CODEC =
            new StreamCodec<>() {
                @Override public C2SCastAutoBoltPayload decode(RegistryFriendlyByteBuf buf) {
                    return new C2SCastAutoBoltPayload(buf.readEnum(InteractionHand.class));
                }
                @Override public void encode(RegistryFriendlyByteBuf buf, C2SCastAutoBoltPayload v) {
                    buf.writeEnum(v.hand());
                }
            };

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
