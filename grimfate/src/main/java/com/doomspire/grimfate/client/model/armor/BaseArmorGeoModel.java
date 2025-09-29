package com.doomspire.grimfate.client.model.armor;

import com.doomspire.grimfate.item.armor.GenericGeoArmorItem;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.model.GeoModel;

public class BaseArmorGeoModel extends GeoModel<GenericGeoArmorItem> {

    @Override
    public ResourceLocation getModelResource(GenericGeoArmorItem animatable) {
        // Берём путь к GEO из Visual (пример: assets/grimfate/geo/armor/copper_armor_set.geo.json)
        return animatable.visual().geo();
    }

    @Override
    public ResourceLocation getTextureResource(GenericGeoArmorItem animatable) {
        // Берём путь к TEXTURE из Visual (пример: assets/grimfate/textures/armor/copper_set.png)
        return animatable.visual().texture();
    }

    @Override
    public @Nullable ResourceLocation getAnimationResource(GenericGeoArmorItem animatable) {
        // Если анимаций нет — можно вернуть null
        return null;
    }
}
