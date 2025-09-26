package com.doomspire.grimfate.network;

import com.doomspire.grimcore.attach.PlayerLoadoutAttachment;
import com.doomspire.grimcore.attach.PlayerStatsAttachment;
import com.doomspire.grimcore.net.GrimcoreNetworking;
import com.doomspire.grimcore.spell.GrimSpells;
import com.doomspire.grimcore.spell.api.CastResult;
import com.doomspire.grimcore.spell.api.SpellContext;
import com.doomspire.grimcore.stat.Attributes;
import com.doomspire.grimcore.stat.ModAttachments;
import com.doomspire.grimcore.stat.StatEffects;
import com.doomspire.grimfate.entity.BoltProjectileEntity;
import com.doomspire.grimfate.network.payload.C2SAllocatePointPayload;
import com.doomspire.grimfate.network.payload.C2SCastSpellSlotPayload;
import com.doomspire.grimfate.network.payload.C2SCastStaffBoltPayload;
import com.doomspire.grimfate.network.payload.S2CAllocateResultPayload;
import com.doomspire.grimfate.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public final class ModNetworking {
    private ModNetworking() {}

    public static void register(RegisterPayloadHandlersEvent e) {
        var reg = e.registrar("grimfate");

        reg.playToServer(C2SAllocatePointPayload.TYPE, C2SAllocatePointPayload.STREAM_CODEC,
                ModNetworking::handleAllocatePoint);

        reg.playToClient(S2CAllocateResultPayload.TYPE, S2CAllocateResultPayload.STREAM_CODEC,
                ModNetworking::handleAllocateResult);

        reg.playToServer(C2SCastStaffBoltPayload.TYPE, C2SCastStaffBoltPayload.STREAM_CODEC,
                ModNetworking::handleCastStaffBolt);

        reg.playToServer(C2SCastSpellSlotPayload.TYPE, C2SCastSpellSlotPayload.STREAM_CODEC,
                ModNetworking::handleCastSpellSlot);
    }

    /** Клиентский хелпер для отправки каста болта. */
    public static void sendCastStaffBolt() {
        PacketDistributor.sendToServer(new C2SCastStaffBoltPayload());
    }

    public static void sendAllocatePoint(String attrId) {
        PacketDistributor.sendToServer(new C2SAllocatePointPayload(attrId));
    }

    // === handlers ===

    private static void handleAllocatePoint(C2SAllocatePointPayload msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer sp = (ServerPlayer) ctx.player();
            if (sp == null) return;

            PlayerStatsAttachment att = sp.getData(ModAttachments.PLAYER_STATS.get());
            if (att == null) return;

            Attributes attr = PlayerStatsAttachment.parseAttrId(msg.attributeId());
            if (attr == null) return;

            boolean ok = att.tryAllocatePoint(attr);
            int allocated = att.getAttribute(attr);
            int unspent   = att.getUnspentPoints();

            PacketDistributor.sendToPlayer(sp, new S2CAllocateResultPayload(attr.name(), allocated, unspent));

            att.markDirty();
            GrimcoreNetworking.syncPlayerStats(sp, att);

            // ⬇️ ТОЛЬКО вызов ядра — никаких модификаторов в ModNetworking
            StatEffects.applyAll(sp);
        });
    }

    private static void handleAllocateResult(S2CAllocateResultPayload msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            var mc = Minecraft.getInstance();
            if (mc.player == null) return;

            var att = mc.player.getData(ModAttachments.PLAYER_STATS.get());
            if (att != null) {
                Attributes attr = PlayerStatsAttachment.parseAttrId(msg.attributeId());
                if (attr != null) {
                    att.setAttribute(attr, msg.newAllocated());
                }
                att.setUnspentPoints(msg.unspent());
                att.markDirty();
            }
            // UI обновится сам — экран читает свежие данные из Attachment.
        });
    }

    private static void handleCastStaffBolt(C2SCastStaffBoltPayload msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer sp = (ServerPlayer) ctx.player();
            if (sp == null) return;

            var stack = sp.getMainHandItem();
            if (!stack.is(ModItems.STAFF.get())) return;

            if (sp.getCooldowns().isOnCooldown(ModItems.STAFF.get())) return;

            var att = sp.getData(ModAttachments.PLAYER_STATS.get());
            if (att == null) return;

            if (att.getCurrentMana() < 2) return;

            att.setCurrentMana(att.getCurrentMana() - 2);
            att.markDirty();
            GrimcoreNetworking.syncPlayerStats(sp, att);

            var bolt = new BoltProjectileEntity(sp.level(), sp);
            bolt.shootForward(sp, 1.8f);
            sp.level().addFreshEntity(bolt);

            sp.getCooldowns().addCooldown(ModItems.STAFF.get(), 20);
            sp.level().playSound(null, sp.getX(), sp.getY(), sp.getZ(),
                    SoundEvents.WITHER_SHOOT, SoundSource.PLAYERS, 0.6f, 1.0f);
        });
    }

    private static void handleCastSpellSlot(C2SCastSpellSlotPayload msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer sp = (ServerPlayer) ctx.player();
            if (sp == null) return;

            int slot = msg.slot();
            if (slot < 0 || slot >= PlayerLoadoutAttachment.SLOTS) return;

            PlayerLoadoutAttachment loadout = sp.getData(ModAttachments.PLAYER_LOADOUT.get());
            if (loadout == null) return;

            if (loadout.getCooldown(slot) > 0) return;

            ResourceLocation spellId = loadout.get(slot);
            if (spellId == null) return;

            var spell = GrimSpells.get(spellId);
            if (spell == null) return;

            var lvl = sp.serverLevel();
            var ctxSpell = new SpellContext(lvl, sp, slot, 0, 0, 0, null);

            int cost = Math.max(0, spell.manaCost(ctxSpell));
            int cd   = Math.max(0, spell.cooldownTicks(ctxSpell));

            var stats = sp.getData(ModAttachments.PLAYER_STATS.get());
            if (stats == null) return;
            if (stats.getCurrentMana() < cost) return;

            CastResult result = spell.cast(ctxSpell);
            if (result == CastResult.OK) {
                stats.setCurrentMana(stats.getCurrentMana() - cost);
                stats.markDirty();
                GrimcoreNetworking.syncPlayerStats(sp, stats);

                loadout.setCooldown(slot, cd);
                sp.setData(ModAttachments.PLAYER_LOADOUT.get(), loadout);
            }
        });
    }

    public static void sendCastSpellSlot(int slot) {
        PacketDistributor.sendToServer(new C2SCastSpellSlotPayload(slot));
    }
}
