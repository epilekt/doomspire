// src/main/java/com/doomspire/grimcore/spell/autobolt/AutoBoltTuning.java
package com.doomspire.grimcore.spell.autobolt;

import com.doomspire.grimcore.datapack.Balance;
import com.doomspire.grimcore.datapack.codec.SpellTuning;
import com.doomspire.grimcore.stat.StatSnapshot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
//TODO ДОБАВИТЬ PROJECTILE SPEED
/**
 * Обёртка над data-тюнингом авто-болта.
 * Все данные берутся из Balance/SpellTuning (datapack).
 */
public final class AutoBoltTuning {

    /** ID записи авто-атаки посоха в spells.json */
    public static final ResourceLocation AUTO_BOLT_ID =
            ResourceLocation.fromNamespaceAndPath("grimfate", "auto_bolt_staff");

    /** Временный дефолт, пока projectile_speed не вынесен в SpellTuning. */
    private static final float DEFAULT_PROJECTILE_SPEED = 1.6f;

    public int manaCost(StatSnapshot snap, ItemStack staff) {
        SpellTuning.Entry e = Balance.getSpellEntry(AUTO_BOLT_ID);
        // В дальнейшем сюда можно добавить сниж. стоимости от статов/аффиксов.
        return (e != null) ? Math.max(0, e.baseCost()) : 0;
    }

    public int cooldownTicks(StatSnapshot snap, ItemStack staff) {
        SpellTuning.Entry e = Balance.getSpellEntry(AUTO_BOLT_ID);
        // При необходимости учесть CDR/скорость атаки – делаем это на уровне ядра поверх baseCooldown.
        return (e != null) ? Math.max(0, e.baseCooldown()) : 0;
    }

    public float projectileSpeed(StatSnapshot snap, ItemStack staff) {
        // В SpellTuning projectile_speed пока нет → возвращаем дефолт.
        // Как только поле появится в кодеке/datapack – читаем его отсюда.
        return DEFAULT_PROJECTILE_SPEED;
    }

    public static AutoBoltTuning get() { return new AutoBoltTuning(); }
    private AutoBoltTuning() {}
}


