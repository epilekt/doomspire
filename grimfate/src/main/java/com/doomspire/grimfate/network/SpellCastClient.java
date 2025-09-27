package com.doomspire.grimfate.network;

import com.doomspire.grimfate.network.payload.C2SCastAutoBoltPayload;
import net.minecraft.world.InteractionHand;
import net.neoforged.neoforge.network.PacketDistributor;

public final class SpellCastClient {
    private SpellCastClient() {}

    /** Вызов авто-болта с указанием руки (используется там, где не хочется лезть в Item.onUseTick). */
    public static void tryCastAutoBoltFromStaff(InteractionHand hand) {
        PacketDistributor.sendToServer(new C2SCastAutoBoltPayload(hand));
    }
}
