package com.doomspire.grimfate.item.armor;

import net.minecraft.core.Holder;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animation.AnimatableManager; // ВАЖНО: этот пакет
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.util.GeckoLibUtil;

//NOTE: Чтобы не спамило в лог про отсутствие анимации для Geo брони. Всю броню регаем через NoAnim

public class NoAnimGeoArmorItem extends GenericGeoArmorItem implements GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // Пробрасываем Visual, т.к. GenericGeoArmorItem его требует
    public NoAnimGeoArmorItem(Holder<ArmorMaterial> material, ArmorItem.Type type, Item.Properties props, Visual visual) {
        super(material, type, props, visual);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // Пусто: контроллеров нет — никто не будет искать "misc.idle"
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
