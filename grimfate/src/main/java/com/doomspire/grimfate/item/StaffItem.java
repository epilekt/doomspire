package com.doomspire.grimfate.item;

import com.doomspire.grimfate.network.payload.C2SCastAutoBoltPayload;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

public class StaffItem extends Item {
    public StaffItem(Properties props) { super(props); }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000; // удержание ПКМ
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        // анимация «натягивания» как у лука, чтобы была понятна механика удержания
        return UseAnim.BOW;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand); // удержание ПКМ
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    private static long lastMsMain = 0, lastMsOff = 0;

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingUseDuration) {
        if (!level.isClientSide) return;

        // нам нужна рука и рейтконтроль — только для игроков
        if (!(entity instanceof Player player)) return;
        InteractionHand hand = player.getUsedItemHand();
        if (hand == null) return;

        int elapsed = getUseDuration(stack, entity) - remainingUseDuration;
        if (elapsed <= 0) return;

        long now = System.currentTimeMillis();
        long last = (hand == InteractionHand.MAIN_HAND) ? lastMsMain : lastMsOff;
        if (now - last < 80) return; // ~каждые 5–6 тиков
        if (hand == InteractionHand.MAIN_HAND) lastMsMain = now; else lastMsOff = now;

        PacketDistributor.sendToServer(new C2SCastAutoBoltPayload(hand));
    }
}
