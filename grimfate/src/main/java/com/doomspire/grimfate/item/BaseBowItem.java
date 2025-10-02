package com.doomspire.grimfate.item;

import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;

//NOTE: База для всех луков Grimfate
/**
 * Поведение:
 * - Стрельба, натяжение, зачарования (Infinity и т.п.) — как у ванильного BowItem.
 * - Визуальные стадии натяжения (pull0/1/2) настраиваются ТОЛЬКО ресурсами (JSON overrides).
 * - 3D-в руках и 2D-в инвентаре задаются через loader "neoforge:separate_transforms".
 *
 * Зачем класс:
 * - Точка для будущих расширений (подключение WeaponProfileComponent, статов, перков).
 * - Единый тип для наших реестров/тегов (если понадобится специфичное поведение позже).
 */
public class BaseBowItem extends BowItem {
    public BaseBowItem(Properties props) { super(props); }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        // Оставляем анимацию как у обычного лука (нужно для предикатов "pull"/"pulling")
        return UseAnim.BOW;
    }

    // Если понадобится — здесь будет хук под WeaponProfileComponent/аффиксы/прочее.
}
