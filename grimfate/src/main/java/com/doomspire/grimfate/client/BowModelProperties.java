package com.doomspire.grimfate.client;

import com.doomspire.grimfate.item.weapons.Weapons;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public final class BowModelProperties {
    private BowModelProperties() {}

    public static void register() {
        // Зарегистрируй для всех своих луков:
        registerBow(Weapons.WEAKLING_BOW.get());
        // Когда появятся другие луки — добавь сюда registerBow(...)
    }

    private static void registerBow(net.minecraft.world.item.Item item) {
        // "pull" — 0..1, насколько натянута тетива
        ItemProperties.register(item, ResourceLocation.withDefaultNamespace("pull"),
                (ItemStack stack, ClientLevel level, LivingEntity entity, int seed) -> {
                    if (entity == null) return 0.0F;
                    if (entity.getUseItem() != stack) return 0.0F;
                    // стабильная формула: сколько тиков тянем / 20.0F
                    int used = entity.getTicksUsingItem();
                    return Math.min(1.0F, used / 20.0F);
                });


        // "pulling" — 1.0 когда игрок тянет тетиву
        ItemProperties.register(item, ResourceLocation.withDefaultNamespace("pulling"),
                (ItemStack stack, ClientLevel level, LivingEntity entity, int seed) ->
                        (entity != null && entity.isUsingItem() && entity.getUseItem() == stack) ? 1.0F : 0.0F);
    }
}
