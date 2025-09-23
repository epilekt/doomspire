package com.doomspire.grimcore.spell.api;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

/** Контекст вызова спелла (сервер). */
public final class SpellContext {
    public final ServerLevel level;
    public final Player caster;
    public final int slot;                  // слот хотбара спеллов; -1 если вне хотбара
    public final double aimX, aimY, aimZ;   // произвольные «наводочные» параметры
    @Nullable public final Object tuning;   // объект tюнинга (деcериализованный из SpellTuning), типизируем позже

    public SpellContext(ServerLevel level, Player caster, int slot, double aimX, double aimY, double aimZ, @Nullable Object tuning) {
        this.level = level;
        this.caster = caster;
        this.slot = slot;
        this.aimX = aimX;
        this.aimY = aimY;
        this.aimZ = aimZ;
        this.tuning = tuning;
    }

    public static SpellContext simple(ServerLevel lvl, Player caster) {
        return new SpellContext(lvl, caster, -1, 0, 0, 0, null);
    }
}
