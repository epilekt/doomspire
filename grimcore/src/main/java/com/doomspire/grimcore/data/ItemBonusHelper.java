package com.doomspire.grimcore.data;

import com.doomspire.grimcore.data.component.StatBonusComponent;
import com.doomspire.grimcore.stat.Attributes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;

public final class ItemBonusHelper {
    private ItemBonusHelper(){}

    public static int sum(Player p, Attributes attr) {
        int total = 0;
        for (var slot : EquipmentSlot.values()) {
            var stack = p.getItemBySlot(slot);
            if (stack.isEmpty()) continue;
            var comp = stack.get(ModDataComponents.STAT_BONUS.get());
            if (comp != null) total += comp.get(attr);
        }
        return total;
    }
}

