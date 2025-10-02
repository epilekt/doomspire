package com.doomspire.grimcore.affix;

import com.doomspire.grimcore.stat.StatSnapshot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

//NOTE: Сборщик эффектов аффиксов в общий StatSnapshot
/**
 * ВАЖНО:
 *  - grimcore НЕ знает, где хранятся аффиксы (компоненты предметов, Curios, теги и т.д.).
 *  - Контент-модуль (grimfate) обязан предоставить экстрактор через {@link #setExtractor(Extractor)}.
 *  - Экстрактор возвращает список "AffixEntry" (id, сила, источник) для КОНКРЕТНОЙ сущности.
 *
 * Алгоритм:
 *  1) Вызываем внешний Extractor → получаем список аффиксов сущности.
 *  2) Для каждого id резолвим Affix из {@link ModAffixes} и вызываем {@link Affix#apply}.
 *  3) Все эффекты суммируются в переданный снапшот.
 *
 * Безопасность:
 *  - Пустые/неизвестные id пропускаются молча.
 *  - Неверные или NaN величины "magnitude" приводятся к 0.
 */
public final class AffixAggregator {

    private AffixAggregator() {}

    /** Внешний экстрактор аффиксов (grimfate должен установить его на старте). */
    private static volatile Extractor EXTRACTOR = entity -> Collections.emptyList();

    /**
     * Установить внешний экстрактор аффиксов.
     * Вызывайте из grimfate при common-инициализации, когда готов чтение компонентов.
     */
    public static void setExtractor(Extractor extractor) {
        EXTRACTOR = Objects.requireNonNull(extractor, "AffixAggregator extractor");
    }

    /**
     * Собрать и применить ВСЕ аффиксы сущности к снапшоту.
     * Снапшот должен быть уже частично посчитан (атрибуты и т.п.) — аффиксы добавятся поверх.
     */
    public static void applyAll(StatSnapshot outSnapshot, LivingEntity entity) {
        if (outSnapshot == null || entity == null) return;

        List<AffixEntry> list;
        try {
            list = EXTRACTOR.extract(entity);
        } catch (Throwable t) {
            // Любая ошибка экстрактора не должна падать на ядро: просто игнорируем.
            list = Collections.emptyList();
        }

        if (list.isEmpty()) return;

        for (AffixEntry entry : list) {
            if (entry == null || entry.id() == null) continue;

            Affix affix = ModAffixes.get(entry.id());
            if (affix == null) continue; // неизвестный аффикс — пропускаем

            float mag = sanitize(entry.magnitude());
            if (mag == 0f) continue;

            try {
                affix.apply(outSnapshot, mag, entry.source());
            } catch (Throwable ignored) {
                // аффикс не должен ломать расчёт статов
            }
        }
    }

    private static float sanitize(float v) {
        if (Float.isNaN(v) || Float.isInfinite(v)) return 0f;
        // тут можно добавить клампы по типам аффиксов, но это ответственность конкретного Affix.apply
        return v;
    }

    // ===================== Контракт экстрактора и запись аффикса =====================

    /**
     * Экстрактор аффиксов для конкретной сущности.
     * ДОЛЖЕН собрать аффиксы со всей экипировки/бафов/Curios/компонентов и вернуть список.
     *
     * Пример реализации в grimfate:
     *  - обойти main/offhand + armor слоты,
     *  - если Curios загружен — обойти их слоты,
     *  - из каждого ItemStack прочитать AffixListComponent и собрать пары (id, magnitude, source).
     */
    @FunctionalInterface
    public interface Extractor {
        List<AffixEntry> extract(LivingEntity entity);
    }

    /**
     * Единичная запись аффикса.
     *
     * @param id        уникальный id аффикса (например, grimcore:damage_reduction_all)
     * @param magnitude числовая сила (уже нормализована, например 0.12f для +12%)
     * @param source    источник (оружие/броня/бижутерия/щит/прочее)
     */
    public record AffixEntry(ResourceLocation id, float magnitude, Affix.Source source) {
        public AffixEntry {
            if (source == null) source = Affix.Source.OTHER;
        }
    }

    // ===================== Утилита для сборки списков (необязательная) =====================

    /**
     * Удобный builder для внешнего экстрактора.
     * Позволяет накапливать аффиксы с валидацией, а затем получить immutable-список.
     */
    public static final class ListBuilder {
        private final List<AffixEntry> data = new ArrayList<>();

        public ListBuilder add(ResourceLocation id, float magnitude, Affix.Source source) {
            if (id != null && !Float.isNaN(magnitude) && !Float.isInfinite(magnitude)) {
                data.add(new AffixEntry(id, magnitude, source));
            }
            return this;
        }

        public List<AffixEntry> build() {
            return Collections.unmodifiableList(new ArrayList<>(data));
        }
    }
}