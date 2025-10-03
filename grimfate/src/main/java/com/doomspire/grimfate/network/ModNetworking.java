package com.doomspire.grimfate.network;

import com.doomspire.grimcore.attach.PlayerLoadoutAttachment;
import com.doomspire.grimcore.attach.PlayerStatsAttachment;
import com.doomspire.grimcore.net.GrimcoreNetworking;
import com.doomspire.grimcore.spell.GrimSpells;
import com.doomspire.grimcore.spell.api.CastResult;
import com.doomspire.grimcore.spell.api.SpellContext;
import com.doomspire.grimcore.spell.api.WeaponGate;
import com.doomspire.grimcore.spell.api.WeaponRequirement;
import com.doomspire.grimcore.stat.Attributes;
import com.doomspire.grimcore.stat.ModAttachments;
import com.doomspire.grimcore.stat.StatEffects;
import com.doomspire.grimfate.network.payload.*;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class ModNetworking {
    private ModNetworking() {}
    private static boolean REGISTERED = false;

    public static void register(RegisterPayloadHandlersEvent e) {
        if (REGISTERED) return;
        REGISTERED = true;
        var reg = e.registrar("grimfate");

        reg.playToServer(C2SAllocatePointPayload.TYPE, C2SAllocatePointPayload.STREAM_CODEC,
                ModNetworking::handleAllocatePoint);

        reg.playToClient(S2CAllocateResultPayload.TYPE, S2CAllocateResultPayload.STREAM_CODEC,
                ModNetworking::handleAllocateResult);

        reg.playToServer(C2SCastSpellSlotPayload.TYPE, C2SCastSpellSlotPayload.STREAM_CODEC,
                ModNetworking::handleCastSpellSlot);

        // ПКМ авто-болт со стаффа
        reg.playToServer(C2SCastAutoBoltPayload.TYPE, C2SCastAutoBoltPayload.STREAM_CODEC, (msg, ctx) -> {
            ctx.enqueueWork(() -> {
                ServerPlayer sp = (ServerPlayer) ctx.player();
                if (sp == null) return;

                // Требуется посох (WeaponGate + локализация причины)
                var gate = WeaponGate.check(sp, WeaponRequirement.stavesOnly());
                if (!gate.ok) {
                    showGateFailure(sp, gate);
                    return;
                }

                // Ядро: ресурсы/кд/скорость
                var hand = gate.usedHand != null ? gate.usedHand : net.minecraft.world.InteractionHand.MAIN_HAND;
                var stack = sp.getItemInHand(hand);
                var result = com.doomspire.grimcore.spell.autobolt.AutoBoltService.computeAndConsume(sp, stack);
                if (!result.ok()) return;

                // Контент: прожектайл
                var proj = new com.doomspire.grimfate.entity.BoltProjectileEntity(sp.level(), sp);
                proj.shootForward(sp, result.projectileSpeed());
                sp.level().addFreshEntity(proj);

                // Звук
                sp.level().playSound(null, sp.getX(), sp.getY(), sp.getZ(),
                        SoundEvents.WITHER_SHOOT, SoundSource.PLAYERS, 0.6f, 1.0f);

                // КД — на предмет
                sp.getCooldowns().addCooldown(stack.getItem(), result.cooldownTicks());
            });
        });
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

            // ядро-эффекты
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

            // Оружейные требования спелла
            var req = com.doomspire.grimfate.spell.SpellRequirements.require(spellId);
            if (req != null) {
                var gate = WeaponGate.check(sp, req);
                if (!gate.ok) {
                    showGateFailure(sp, gate);
                    return;
                }
            }

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

    // === UX helper ===
    private static void showGateFailure(ServerPlayer sp, WeaponGate.Result gate) {
        if (gate.reasonKey != null) {
            sp.displayClientMessage(Component.translatable(gate.reasonKey), true);
        } else if (gate.reason != null && !gate.reason.isBlank()) {
            sp.displayClientMessage(Component.literal(gate.reason), true);
        } else {
            sp.displayClientMessage(Component.translatable("grimfate.msg.weapon_required"), true);
        }
        sp.level().playSound(null, sp.getX(), sp.getY(), sp.getZ(),
                SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 0.7f, 1.0f);
    }
}
