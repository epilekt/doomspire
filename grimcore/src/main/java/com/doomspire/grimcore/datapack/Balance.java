package com.doomspire.grimcore.datapack;

import com.doomspire.grimcore.datapack.codec.AttributesBalance;
import com.doomspire.grimcore.datapack.codec.LevelsCurve;
import com.doomspire.grimcore.datapack.codec.SpellTuning;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

/** Глобальный снимок data-driven баланса (обновляется при перезагрузке датапаков). */
public final class Balance {
    private Balance() {}

    private static volatile LevelsCurve levels = LevelsCurve.defaults();
    private static volatile AttributesBalance attributes = AttributesBalance.defaults();
    private static volatile SpellTuning spells = SpellTuning.defaults();

    public static void set(LevelsCurve l, AttributesBalance a, SpellTuning s) {
        levels = (l != null) ? l : LevelsCurve.defaults();
        attributes = (a != null) ? a : AttributesBalance.defaults();
        spells = (s != null) ? s : SpellTuning.defaults();
    }

    public static LevelsCurve levels() { return levels; }
    public static AttributesBalance attributes() { return attributes; }
    public static SpellTuning spells() { return spells; }

    public static @Nullable SpellTuning.Entry getSpellEntry(ResourceLocation id) {
        return spells.byId().get(id);
    }
}

