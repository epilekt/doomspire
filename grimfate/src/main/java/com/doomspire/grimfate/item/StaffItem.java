package com.doomspire.grimfate.item;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

public class StaffItem extends Item {
    public StaffItem(Properties props) {
        super(props);
    }
    // Атака происходит не тут, а через ивенты (см. StaffAttackEvents) — так мы сохраняем копание блоков по ЛКМ
}
