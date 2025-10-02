package com.doomspire.grimcore.spell.autobolt;

import com.doomspire.grimcore.datapack.Balance;
import com.doomspire.grimcore.datapack.codec.SpellTuning;
import com.doomspire.grimcore.stat.StatSnapshot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

//NOTE: Обёртка над data-тюнингом авто-болта.
/**
 * Все данные берутся из Balance/SpellTuning (datapack).
 *
 * ВАЖНО: поле projectile_speed читается «мягко»:
 *  - если SpellTuning.Entry уже поддерживает projectileSpeed() — используем его;
 *  - иначе пытаемся найти параметр "projectile_speed" в доп.полях (если таковые есть);
 *  - иначе берём безопасный дефолт (как у ванильной стрелы).
 *
 * Datapack-пример (data/grimfate/spells.json):
 * {
 *   "id": "grimfate:auto_bolt_staff",
 *   "baseCost": 3,
 *   "baseCooldown": 8,
 *   "projectile_speed": 1.6
 * }
 */
public final class AutoBoltTuning {

    /** ID записи авто-атаки посоха в spells.json */
    public static final ResourceLocation AUTO_BOLT_ID =
            ResourceLocation.fromNamespaceAndPath("grimfate", "auto_bolt_staff");

    /** Дефолт близок к ванильной стреле (+-). */
    private static final float DEFAULT_PROJECTILE_SPEED = 1.6f;

    /** Нижний «разумный» порог — чтобы болт не зависал в воздухе при багах данных. */
    private static final float MIN_PROJECTILE_SPEED = 0.4f;

    /** Верхний ограничитель на случай перебора в датапаке. */
    private static final float MAX_PROJECTILE_SPEED = 3.5f;

    public int manaCost(StatSnapshot snap, ItemStack staff) {
        SpellTuning.Entry e = Balance.getSpellEntry(AUTO_BOLT_ID);
        // Позже: модификаторы стоимости от статов/аффиксов.
        return (e != null) ? Math.max(0, e.baseCost()) : 0;
    }

    public int cooldownTicks(StatSnapshot snap, ItemStack staff) {
        SpellTuning.Entry e = Balance.getSpellEntry(AUTO_BOLT_ID);
        // Позже: учёт CDR/скорости атаки поверх baseCooldown.
        return (e != null) ? Math.max(0, e.baseCooldown()) : 0;
    }

    /**
     * Скорость снаряда (базовая). Баллистика (гравитация/drag) настраивается в сущности проектайла.
     * Здесь только базовый модуль скорости вылета.
     */
    public float projectileSpeed(StatSnapshot snap, ItemStack staff) {
        SpellTuning.Entry e = Balance.getSpellEntry(AUTO_BOLT_ID);

        float v = DEFAULT_PROJECTILE_SPEED;

        if (e != null) {
            // 1) Если в кодеке уже появилось строгое поле projectileSpeed()
            try {
                // Вызов через метод, если он существует (без жёсткой зависимости).
                var m = e.getClass().getMethod("projectileSpeed");
                Object ret = m.invoke(e);
                if (ret instanceof Number n) v = n.floatValue();
            } catch (NoSuchMethodException ignored) {
                // 2) Альтернатива: попытка вытащить из «экстра»-поля, если кодек хранит map/extra
                try {
                    var mExtra = e.getClass().getMethod("extra");
                    Object extra = mExtra.invoke(e); // ожидаем Map<String,Object> либо подобное
                    if (extra instanceof java.util.Map<?,?> map) {
                        Object val = map.get("projectile_speed");
                        if (val instanceof Number n) v = n.floatValue();
                        else if (val instanceof String s) v = Float.parseFloat(s);
                    }
                } catch (Throwable ignored2) {
                    // нет extra — ок
                }
            } catch (Throwable ignored) {
                // любые рефлексивные ошибки глушим — упадём на дефолт
            }
        }

        // Позже: модификаторы скорости от статов/аффиксов (ranged_speed, staff_mastery и т.п.)
        // v *= snap.getMul(Stats.PROJECTILE_SPEED); // пример

        if (Float.isNaN(v) || Float.isInfinite(v)) v = DEFAULT_PROJECTILE_SPEED;
        if (v < MIN_PROJECTILE_SPEED) v = MIN_PROJECTILE_SPEED;
        if (v > MAX_PROJECTILE_SPEED) v = MAX_PROJECTILE_SPEED;
        return v;
    }

    public static AutoBoltTuning get() { return new AutoBoltTuning(); }
    private AutoBoltTuning() {}
}
