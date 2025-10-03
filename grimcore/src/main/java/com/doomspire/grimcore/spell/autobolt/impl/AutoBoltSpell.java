package com.doomspire.grimcore.spell.autobolt.impl;

import com.doomspire.grimcore.attach.PlayerStatsAttachment;
import com.doomspire.grimcore.spell.api.CastResult;
import com.doomspire.grimcore.spell.api.Spell;
import com.doomspire.grimcore.spell.api.SpellContext;
import com.doomspire.grimcore.spell.api.SpellSchool;
import com.doomspire.grimcore.spell.api.SpellTag;
import com.doomspire.grimcore.spell.api.WeaponGate;
import com.doomspire.grimcore.spell.api.WeaponRequirement;
import com.doomspire.grimcore.spell.autobolt.AutoBoltService;
import com.doomspire.grimcore.spell.autobolt.AutoBoltTuning;
import com.doomspire.grimcore.stat.StatSnapshot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;
import java.util.Set;

/**
 * Ядровой спелл авто-болта для посохов:
 * - требования к оружию: только #grimfate:staves;
 * - расход/кулдаун берутся из AutoBoltTuning;
 * - при cast выполняется только проверка + списание ресурсов через AutoBoltService;
 *   сам спавн снаряда делается на стороне grimfate.
 */
public final class AutoBoltSpell implements Spell {

    private static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath("grimfate", "auto_bolt_staff");

    private static final WeaponRequirement REQ_STAFF = WeaponRequirement.stavesOnly();
    private static final AutoBoltTuning TUNING = AutoBoltTuning.get();

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public SpellSchool school() {
        // Подбираем подходящую школу (можно завести отдельную SCHOOL:ARCANE или "STAFF").
        return SpellSchool.ARCANE;
    }

    @Override
    public Set<SpellTag> tags() {
        // Теги для фильтров/баланса — пока пусто, можно добавить LMB_SPELL / BASIC_ATTACK и т.п.
        return Collections.emptySet();
    }

    @Override
    public int manaCost(SpellContext ctx) {
        Player p = asPlayer(ctx.caster);
        if (p == null) return 0;

        ItemStack staff = findStaffInHands(p);
        StatSnapshot snap = PlayerStatsAttachment.get(p).getSnapshot();
        return TUNING.manaCost(snap, staff);
    }

    @Override
    public int cooldownTicks(SpellContext ctx) {
        Player p = asPlayer(ctx.caster);
        if (p == null) return 0;

        ItemStack staff = findStaffInHands(p);
        StatSnapshot snap = PlayerStatsAttachment.get(p).getSnapshot();
        return TUNING.cooldownTicks(snap, staff);
    }

    @Override
    public CastResult cast(SpellContext ctx) {
        if (!(ctx.caster instanceof ServerPlayer sp)) {
            return CastResult.FAIL;
        }

        WeaponGate.Result gate = WeaponGate.check(sp, REQ_STAFF);
        if (!gate.ok) return CastResult.FAIL;

        ItemStack staff = sp.getItemInHand(
                gate.usedHand != null ? gate.usedHand : InteractionHand.MAIN_HAND);

        var res = AutoBoltService.computeAndConsume(sp, staff);
        return res.ok() ? CastResult.OK : CastResult.FAIL;
    }

    // ---------- helpers ----------

    private static Player asPlayer(LivingEntity e) {
        return (e instanceof Player pl) ? pl : null;
    }

    private static ItemStack findStaffInHands(Player p) {
        ItemStack main = p.getMainHandItem();
        if (!main.isEmpty() && isStaff(main)) return main;
        ItemStack off = p.getOffhandItem();
        if (!off.isEmpty() && isStaff(off)) return off;
        return ItemStack.EMPTY;
    }

    private static boolean isStaff(ItemStack stack) {
        // Заглушка, заменим на тег #grimfate:staves
        return true;
    }
}
