package com.doomspire.grimfate.network;

import com.doomspire.grimfate.entity.BoltProjectileEntity;
import com.doomspire.grimcore.spell.autobolt.AutoBoltResult;
import com.doomspire.grimcore.spell.autobolt.AutoBoltService;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class AutoBoltServer {
    private AutoBoltServer() {}

    public static void tryCast(ServerPlayer sp, InteractionHand hand) {
        if (sp == null) return;

        ServerLevel level = sp.serverLevel();
        ItemStack used = sp.getItemInHand(hand);
        if (used.isEmpty()) return;

        // Все проверки/расходы делает сервис ядра
        AutoBoltResult result = AutoBoltService.computeAndConsume(sp, used);
        if (result == null || !result.ok()) {
            // при желании можно отправить локализованный отказ по result.reason()
            return;
        }

        // Спавним снаряд
        BoltProjectileEntity bolt = new BoltProjectileEntity(level, sp);
        bolt.shootFromRotation(sp, sp.getXRot(), sp.getYRot(), 0.0f, result.projectileSpeed(), 0.0f);
        level.addFreshEntity(bolt);

        // Кулдаун на использованный предмет
        Item usedItem = used.getItem();
        int cd = result.cooldownTicks();
        if (cd > 0) {
            sp.getCooldowns().addCooldown(usedItem, cd);
        }

        // Звук
        level.playSound(null, sp.getX(), sp.getY(), sp.getZ(),
                SoundEvents.WITHER_SHOOT, SoundSource.PLAYERS, 0.6f, 1.0f);
    }
}
