package com.doomspire.grimfate.client.render.armor;

import com.doomspire.grimfate.client.model.armor.BaseArmorGeoModel;
import com.doomspire.grimfate.item.armor.GenericGeoArmorItem;
import com.doomspire.grimfate.item.armor.NoAnimGeoArmorItem;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

/**
 * Базовый GeckoLib-рендерер брони для всех сетов Grimfate.
 * Работает с {@link GenericGeoArmorItem} и читает пути к geo/texture из его Visual.
 *
 * ВАЖНО: имена костей (bone ids) должны совпадать с теми, что в .geo.json.
 * Ниже указаны дефолтные/распространённые идентификаторы. Если в ваших моделях другие,
 * просто поправьте строки на ваши bone ids.
 */
public class BaseArmorRenderer extends GeoArmorRenderer<NoAnimGeoArmorItem> {

    public BaseArmorRenderer() {
        super(new BaseArmorGeoModel());
    }
}
