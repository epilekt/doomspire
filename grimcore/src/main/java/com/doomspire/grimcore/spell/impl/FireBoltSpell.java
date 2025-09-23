package com.doomspire.grimcore.spell.impl;

import com.doomspire.grimcore.datapack.Balance;
import com.doomspire.grimcore.datapack.codec.SpellTuning;
import com.doomspire.grimcore.spell.api.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

import java.util.Set;

public final class FireBoltSpell implements Spell {
    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("grimfate", "fire_bolt");

    @Override public ResourceLocation id() { return ID; }
    @Override public SpellSchool school() { return SpellSchool.FIRE; }
    @Override public Set<SpellTag> tags() { return Set.of(SpellTag.PROJECTILE, SpellTag.RANGED); }

    @Override public int manaCost(SpellContext ctx) {
        SpellTuning.Entry e = Balance.getSpellEntry(ID);
        return e != null ? Math.max(0, e.baseCost()) : 6;
    }
    @Override public int cooldownTicks(SpellContext ctx) {
        SpellTuning.Entry e = Balance.getSpellEntry(ID);
        return e != null ? Math.max(0, e.baseCooldown()) : 20;
    }

    @Override
    public CastResult cast(SpellContext ctx) {
        var p = ctx.caster;
        var lvl = ctx.level;

        // самый простой «трэйсер»: 16 блоков, пока без собственной сущности
        var from = p.getEyePosition();
        var look = p.getLookAngle();
        var to   = from.add(look.scale(16.0));
        var clip = lvl.clip(new net.minecraft.world.level.ClipContext(
                from, to,
                net.minecraft.world.level.ClipContext.Block.COLLIDER,
                net.minecraft.world.level.ClipContext.Fluid.NONE,
                p));

        var hitPos = clip.getLocation() != null ? clip.getLocation() : to;

        var target = lvl.getEntities(p, p.getBoundingBox().inflate(16.0),
                        e -> e instanceof net.minecraft.world.entity.LivingEntity && e != p).stream()
                .min(java.util.Comparator.comparingDouble(e -> e.distanceToSqr(hitPos.x, hitPos.y, hitPos.z)))
                .orElse(null);

        if (target instanceof net.minecraft.world.entity.LivingEntity living) {
            var src = lvl.damageSources().indirectMagic(p, p);
            living.hurt(src, 5.0f);
            lvl.playSound(null, p.blockPosition(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 0.6f, 1.2f);
            return CastResult.OK;
        }

        // холостой звук
        lvl.playSound(null, p.blockPosition(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 0.4f, 1.2f);
        return CastResult.OK;
    }
}
