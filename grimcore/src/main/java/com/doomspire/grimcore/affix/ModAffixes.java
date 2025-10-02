package com.doomspire.grimcore.affix;

import com.doomspire.grimcore.stat.StatSnapshot;
import net.minecraft.resources.ResourceLocation;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

//NOTE: Глобальный реестр всех доступных аффиксов.
/**
 * Grimcore не знает, какие предметы их содержат — только предоставляет фабрики и реализацию.
 * Контент-модуль (grimfate) может расширять реестр через register().
 */
public final class ModAffixes {

    private static final Map<ResourceLocation, Affix> REGISTRY = new HashMap<>();

    private ModAffixes() {}

    // ------------------- API -------------------

    public static void register(Affix affix) {
        if (REGISTRY.containsKey(affix.id())) {
            throw new IllegalStateException("Affix id already registered: " + affix.id());
        }
        REGISTRY.put(affix.id(), affix);
    }

    public static Affix get(ResourceLocation id) {
        return REGISTRY.get(id);
    }

    public static Map<ResourceLocation, Affix> all() {
        return Collections.unmodifiableMap(REGISTRY);
    }

    /**
     * Вызвать один раз при старте мода (например, в common-инициализации).
     * Регистрирует базовые аффиксы ядра.
     * Контент может добавить свои дополнительно.
     */
    public static void bootstrap() {
        // Примеры: эти аффиксы базовые и нужны ядру для работы
        register(new SimpleAffix(
                ResourceLocation.fromNamespaceAndPath("grimcore", "damage_reduction_all"),
                (snap, mag, src) -> snap.damageReductionAll += mag
        ));

        register(new SimpleAffix(
                ResourceLocation.fromNamespaceAndPath("grimcore", "max_mana_flat"),
                (snap, mag, src) -> snap.maxMana += mag
        ));

        register(new SimpleAffix(
                ResourceLocation.fromNamespaceAndPath("grimcore", "fire_resist"),
                (snap, mag, src) -> snap.resistances.merge(
                        com.doomspire.grimcore.stat.ResistTypes.FIRE,
                        mag, Float::sum)
        ));
    }

    // ------------------- Реализация базового простого аффикса -------------------

    /**
     * Упрощённый аффикс, для случаев где достаточно «применить лямбду».
     */
    private record SimpleAffix(ResourceLocation id, Applier fn) implements Affix {
        @Override
        public void apply(StatSnapshot outSnapshot, float magnitude, Source source) {
            fn.apply(outSnapshot, magnitude, source);
        }

        @FunctionalInterface
        private interface Applier {
            void apply(StatSnapshot out, float mag, Source src);
        }
    }
}

