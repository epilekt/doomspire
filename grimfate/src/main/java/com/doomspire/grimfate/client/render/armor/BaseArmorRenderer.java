package com.doomspire.grimfate.client.render.armor;

import com.doomspire.grimfate.client.model.armor.BaseArmorGeoModel;
import com.doomspire.grimfate.item.armor.GenericGeoArmorItem;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class BaseArmorRenderer extends GeoArmorRenderer<GenericGeoArmorItem> {
    public BaseArmorRenderer() {
        super(new BaseArmorGeoModel());
        // При необходимости можно связать кости:
        //this.headBone = "armorHead";
        //this.bodyBone = "armorBody";
        //this.rightArmBone = "armorRightArm";
        //this.leftArmBone = "armorLeftArm";
        //this.rightLegBone = "armorRightLeg";
        //this.leftLegBone = "armorLeftLeg";
        //this.rightBootBone = "armorRightBoot";
        //this.leftBootBone = "armorLeftBoot";
    }
}
