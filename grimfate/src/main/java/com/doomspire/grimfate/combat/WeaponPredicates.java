package com.doomspire.grimfate.combat;

import com.doomspire.grimfate.registry.ModDataComponents;
import com.doomspire.grimfate.registry.ModItemTags;
import net.minecraft.world.item.ItemStack;

public final class WeaponPredicates {
    public static boolean isType(ItemStack stack, WeaponType t) {
        var prof = stack.get(ModDataComponents.WEAPON_PROFILE.get());
        if (prof != null) return prof.type() == t;
        return switch (t) {
            case STAFF  -> stack.is(ModItemTags.STAVES);
            case BOW    -> stack.is(ModItemTags.RANGED_WEAPONS);
            case DAGGER -> stack.is(ModItemTags.DAGGERS);
            case SWORD  -> stack.is(ModItemTags.MELEE_WEAPONS);
            case SHIELD -> stack.is(ModItemTags.SHIELDS);
            case GREATSWORD -> stack.is(ModItemTags.MELEE_WEAPONS);
            case HAMMER -> stack.is(ModItemTags.MELEE_WEAPONS);
            default     -> false;
        };
    }
    public static boolean isStaff(ItemStack s)  { return isType(s, WeaponType.STAFF); }
    public static boolean isRanged(ItemStack s) { return isType(s, WeaponType.BOW); }
    public static boolean isMelee(ItemStack s)  { return isType(s, WeaponType.SWORD) || isType(s, WeaponType.DAGGER) || isType(s, WeaponType.AXE)
            || isType(s, WeaponType.GREATSWORD) || isType(s, WeaponType.HAMMER); }
    public static boolean isShield(ItemStack s) { return isType(s, WeaponType.SHIELD); }
    private WeaponPredicates() {}
}
