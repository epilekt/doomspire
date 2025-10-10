package com.doomspire.grimfate.affix;

import com.doomspire.grimcore.affix.Affix;
import com.doomspire.grimcore.affix.ModAffixes;
import com.doomspire.grimcore.datapack.Balance;
import com.doomspire.grimcore.datapack.codec.AttributesBalance;
import com.doomspire.grimcore.stat.DamageTypes;
import com.doomspire.grimcore.stat.ResistTypes;
import com.doomspire.grimcore.stat.StatSnapshot;
import net.minecraft.resources.ResourceLocation;

import java.lang.reflect.Field;

/**
 * Регистрация поведения аффиксов контента grimfate.
 * Данные (defs/pools/rarities) остаются в grimcore; тут только "что делает" каждый affix.id со StatSnapshot.
 */
public final class GrimfateAffixes {
    private GrimfateAffixes(){}

    private static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath("grimfate", path);
    }

    /** Вызывай в commonSetup() ПОСЛЕ ModAffixes.bootstrap(). */
    public static void registerAll() {
        // ====== core из предыдущего блока (оставляем) ======
        // 1) +Vitality (ед.) -> конвертируем по balance/attributes.json
        ModAffixes.register(new Simple(rl("plus_vitality"), (snap, mag, src) -> {
            var rule = Balance.attributes().byAttr().get(com.doomspire.grimcore.stat.Attributes.VITALITY);
            double hpPer = (rule != null ? rule.maxHealthPer() : 6.0);
            double regen = (rule != null ? rule.regenHpPer()   : 0.0);
            float before = snap.maxHealth;
            snap.maxHealth   += (float) (mag * hpPer);
            snap.regenHealth += (float) (mag * regen);
            org.slf4j.LoggerFactory.getLogger("Grim/Affix")
                    .info("[Affix] plus_vitality mag={} → HP {} -> {}", mag, before, snap.maxHealth);
        }));


        // 2) +Strength (ед.) -> усиливаем физический ближний урон (примерная калибровка 3%/ед.)
        ModAffixes.register(new Simple(rl("plus_strength"), (snap, mag, src) ->
                snap.damage.merge(DamageTypes.PHYS_MELEE, (float)(mag * 0.03f), Float::sum)
        ));

        // 3) +% Physical Damage (доля) -> добавляем к обоим физ. типам
        ModAffixes.register(new Simple(rl("phys_damage_percent"), (snap, mag, src) -> {
            snap.damage.merge(DamageTypes.PHYS_MELEE,  (float)mag, Float::sum);
            snap.damage.merge(DamageTypes.PHYS_RANGED, (float)mag, Float::sum);
        }));

        // --- НОВЫЕ ПОДКЛЮЧЕННЫЕ СТАТЫ ---

        // attack_speed (доля): 0.10 = +10%
        ModAffixes.register(new Simple(rl("attack_speed"), (snap, mag, src) -> {
            snap.attackSpeed += (float)mag;
        }));

        // armor_flat (плоско)
        ModAffixes.register(new Simple(rl("armor_flat"), (snap, mag, src) -> {
            snap.armorFlat += (float)mag;
        }));

        // resist_all_percent (доля ко всем резистам)
        ModAffixes.register(new Simple(rl("resist_all_percent"), (snap, mag, src) -> {
            snap.resistAll += (float)mag;
        }));

        // lifesteal (доля)
        ModAffixes.register(new Simple(rl("lifesteal"), (snap, mag, src) -> {
            snap.lifesteal += (float)mag;
        }));

        // manasteal (доля)
        ModAffixes.register(new Simple(rl("manasteal"), (snap, mag, src) -> {
            snap.manasteal += (float)mag;
        }));

        // evasion_chance (доля)
        ModAffixes.register(new Simple(rl("evasion_chance"), (snap, mag, src) -> {
            snap.evasionChance += (float)mag;
        }));

        // crit_chance_percent — оставляем
        ModAffixes.register(new Simple(rl("crit_chance_percent"), (snap, mag, src) ->
                snap.critChance += (float)mag
        ));
    }

    // ---------------- helpers ----------------

    private static void addAttackSpeed(StatSnapshot snap, float v) {
        try {
            // наиболее вероятное имя поля
            Field f = StatSnapshot.class.getField("attackSpeed");
            f.setFloat(snap, f.getFloat(snap) + v);
        } catch (NoSuchFieldException nf) {
            // если поля нет — ничего не делаем (чтобы не падать). Можем позже свести в общий "multipliers" если появится.
        } catch (Throwable t) {
            // игнорим, чтобы не сломать применение остальных аффиксов
        }
    }

    private static void addArmorFlat(StatSnapshot snap, float v) {
        // пробуем «armorFlat», затем «armor»
        if (!tryIncFloatField(snap, "armorFlat", v)) {
            if (!tryIncFloatField(snap, "armor", v)) {
                // как fallback можно сместить в damageReductionAll через кривую, но это уже другая модель; оставим как no-op
            }
        }
    }

    private static void addResistAllPercent(StatSnapshot snap, float v) {
        // 1) если есть общий агрегат resistAll — используем его
        if (tryIncFloatField(snap, "resistAll", v)) return;

        // 2) иначе разнесём по всем типам из enum ResistTypes
        try {
            for (ResistTypes type : ResistTypes.values()) {
                snap.resistances.merge(type, v, Float::sum);
            }
        } catch (Throwable t) {
            // игнор — в худшем случае ни на что не повлияем, но и не упадём
        }
    }

    private static boolean tryIncFloatField(Object obj, String field, float add) {
        try {
            Field f = obj.getClass().getField(field);
            f.setFloat(obj, f.getFloat(obj) + add);
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    /** Упрощённый Affix-носитель. */
    private record Simple(ResourceLocation id, Applier fn) implements Affix {
        @Override public ResourceLocation id() { return id; }
        @Override public void apply(StatSnapshot out, float magnitude, Source source) { fn.apply(out, magnitude, source); }
        @FunctionalInterface private interface Applier { void apply(StatSnapshot out, float mag, Source src); }
    }
}
