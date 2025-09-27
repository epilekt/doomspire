package com.doomspire.grimfate.network;

import com.doomspire.grimcore.attach.PlayerStatsAttachment;
import com.doomspire.grimcore.net.GrimcoreNetworking;
import com.doomspire.grimcore.stat.ModAttachments;
import com.doomspire.grimfate.combat.WeaponPredicates;
import com.doomspire.grimfate.entity.BoltProjectileEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** Серверная реализация авто-болта (ПКМ). ЛКМ — ванильное «копание». */
public final class AutoBoltServer {
    private AutoBoltServer() {}

    public static void tryCast(ServerPlayer sp) {
        ItemStack main = sp.getMainHandItem();
        ItemStack off  = sp.getOffhandItem();

        ItemStack used = ItemStack.EMPTY;
        if (WeaponPredicates.isStaff(main)) used = main;
        else if (WeaponPredicates.isStaff(off)) used = off;
        if (used.isEmpty()) return;

        Item usedItem = used.getItem();
        if (sp.getCooldowns().isOnCooldown(usedItem)) return;

        PlayerStatsAttachment stats = sp.getData(ModAttachments.PLAYER_STATS.get());
        if (stats == null) return;

        // Баланс №0 (в дальнейшем читаем из spells.json: auto_bolt_staff)
        final int manaCost = 8;
        final int cdTicks  = 40;
        final float speed  = 1.6f;

        if (stats.getCurrentMana() < manaCost) return;

        stats.setCurrentMana(stats.getCurrentMana() - manaCost);
        stats.markDirty();
        GrimcoreNetworking.syncPlayerStats(sp, stats);

        var bolt = new BoltProjectileEntity(sp.level(), sp);
        bolt.shootForward(sp, speed);
        sp.level().addFreshEntity(bolt);

        sp.getCooldowns().addCooldown(usedItem, cdTicks);
        sp.level().playSound(null, sp.getX(), sp.getY(), sp.getZ(),
                SoundEvents.WITHER_SHOOT, SoundSource.PLAYERS, 0.6f, 1.0f);
    }
}
