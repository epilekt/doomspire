package com.doomspire.grimfate.client.model.armor;

import com.doomspire.grimfate.item.armor.NoAnimGeoArmorItem;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.model.GeoModel;

public class BaseArmorGeoModel extends GeoModel<NoAnimGeoArmorItem> {

    @Override public ResourceLocation getModelResource(NoAnimGeoArmorItem a)   { return a.visual().geo(); }
    @Override public ResourceLocation getTextureResource(NoAnimGeoArmorItem a) { return a.visual().texture(); }
    @Override public @Nullable ResourceLocation getAnimationResource(NoAnimGeoArmorItem a) { return null; }

}
