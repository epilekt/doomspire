package com.doomspire.grimfate.events;

import com.doomspire.grimfate.network.payload.C2SCastStaffBoltPayload;
import com.doomspire.grimfate.registry.ModItems;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;

public final class StaffAttackEvents {
    private StaffAttackEvents(){}

    public static void register() {
        NeoForge.EVENT_BUS.register(StaffAttackEvents.class);
    }

    /** ЛКМ по сущности → отменяем ванильный удар и шлём наш каст (если в руке посох). */
    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent e) {
        var p = e.getEntity();
        ItemStack main = p.getMainHandItem();
        if (main.is(ModItems.STAFF.get())) {
            e.setCanceled(true);
            PacketDistributor.sendToServer(new C2SCastStaffBoltPayload());
            p.swing(InteractionHand.MAIN_HAND); // анимация на клиенте, звук играет сервер при спавне
        }
    }

    /** ЛКМ в воздухе → тоже каст (если посох). ЛКМ по блоку оставляем ванили (копание). */
    @SubscribeEvent
    public static void onLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty e) {
        var p = e.getEntity();
        ItemStack main = p.getMainHandItem();
        if (main.is(ModItems.STAFF.get())) {
            PacketDistributor.sendToServer(new C2SCastStaffBoltPayload());
            p.swing(InteractionHand.MAIN_HAND);
        }
    }
}
