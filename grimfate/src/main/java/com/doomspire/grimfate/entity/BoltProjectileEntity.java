package com.doomspire.grimfate.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class BoltProjectileEntity extends ThrowableItemProjectile {

    public BoltProjectileEntity(EntityType<? extends BoltProjectileEntity> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
    }

    public BoltProjectileEntity(Level level, LivingEntity shooter) {
        this(com.doomspire.grimfate.registry.ModEntityTypes.BOLT.get(), level);
        this.setOwner(shooter);
        this.setNoGravity(true);
    }

    /** Отдаём клиенту предмет для ThrownItemRenderer. */
    @Override
    protected Item getDefaultItem() {
        // TODO: заменить на собственный bolt_item для посоха
        return net.minecraft.world.item.Items.AIR;
    }

    /** Старт с небольшим смещением от глаз + задание скорости. */
    public void shootForward(LivingEntity shooter, float speed) {
        Vec3 look = shooter.getLookAngle();
        // смещение на полблока вперёд, чтобы не цеплять свой хитбокс
        this.setPos(
                shooter.getX() + look.x * 0.5,
                shooter.getEyeY() - 0.1 + look.y * 0.5,
                shooter.getZ() + look.z * 0.5
        );
        this.setDeltaMovement(look.normalize().scale(speed));
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);

        if (!level().isClientSide) {
            // Небольшая частица в месте попадания
            ((ServerLevel) level()).sendParticles(ParticleTypes.CRIT, getX(), getY(), getZ(), 4, 0, 0, 0, 0.0);

            // Урон по цели, если есть «живая» цель
            if (result.getType() == HitResult.Type.ENTITY && getOwner() instanceof LivingEntity owner) {
                var hit = ((net.minecraft.world.phys.EntityHitResult) result).getEntity();
                if (hit instanceof LivingEntity target) {
                    // наш физ-ранжед, простой урон через ванильный DamageSource (ядро боя подключим позже)
                    DamageSource src = level().damageSources().indirectMagic(this, owner); // пока нейтральный DS
                    target.hurt(src, 5.0f); // базовый урон; позже подменим на DamageEngine
                }
            }
            discard();
        }
    }

    @Override
    public void tick() {
        super.tick();
        // Жизнь ~36 блоков при скорости ~1.8 => 20 тиков * 2 сек достаточно
        if (this.tickCount > 40) this.discard();
        if (level().isClientSide) {
            level().addParticle(ParticleTypes.CRIT, getX(), getY(), getZ(), 0, 0, 0);
        }
    }
}
