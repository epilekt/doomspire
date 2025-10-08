package com.doomspire.grimfate.affix;

import com.doomspire.grimcore.affix.Affix;
import com.doomspire.grimfate.item.comp.AffixListHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.Locale;
import java.util.Objects;

/*
//NOTE: Сервис имбью (наполнение аффиксами крафтового снаряжения) — мягкий шаблон под будущего Идола.
// Работает сервер-сайд. Не занимается GUI/эффектами, только валидацией и записью компонента аффиксов.
 /**
  * Основная идея:
  *  • Крафтовые предметы рождаются пустыми (без AffixListComponent).
  *  • Ритуал (Идол) вызывает этот сервис — он валидирует цель и записывает сгенерированные аффиксы.
  *  • Вся генерация делегируется в RollService, а запись — в AffixListHelper.
  *
  * Хуки:
  *  • ItemLevelResolver — внешний провайдер уровня предмета (прогрессия мира/игрока).
  *  • Policy — политика имбью (разрешать ли перезапись уже «насыщенных» предметов и т.п.).
  *
  * Не трогает лут — за лут отвечает Global Loot Modifier.
  */
public final class AffixImbueService {
    private AffixImbueService() {}

    // ---------- Политика и параметры ----------

    /** Политика имбью: базовые правила, которые может настроить «Идол». */
    public record Policy(
            boolean allowOverwrite,  // можно ли перезаписывать уже «наполненные» предметы
            boolean requireEmpty,    // требовать отсутствия аффиксов (обычно true для крафтового пути)
            boolean strictSource     // вычислять источник строго (false — использовать эвристику guessSource)
    ) {
        public static final Policy DEFAULT = new Policy(false, true, true);
    }

    /** Контекст вызова имбью (для логов/ауры/ритуала). Здесь только то, что реально нужно сервису. */
    public record Context(
            ServerLevel level,
            RandomSource random,
            @Nullable Player actor
    ) {
        public Context {
            Objects.requireNonNull(level, "level");
            Objects.requireNonNull(random, "random");
        }
    }

    /** Внешний способ вычислить itemLevel для конкретного случая (зона, прогресс игрока и т.п.). */
    @FunctionalInterface
    public interface ItemLevelResolver {
        int resolve(Context ctx, ItemStack stack, int defaultLevel);
    }

    /** Глобальный резолвер уровня (мягкий хук; можно заменить из инициализации Идола). */
    private static volatile ItemLevelResolver ITEM_LEVEL_RESOLVER =
            (ctx, stack, def) -> def;

    public static void setItemLevelResolver(ItemLevelResolver resolver) {
        if (resolver != null) ITEM_LEVEL_RESOLVER = resolver;
    }

    // ---------- Публичное API ----------

    /**
     * Имбью одного предмета. Пишет AffixListComponent внутрь ItemStack при успехе.
     * @param stack   целевой предмет
     * @param ctx     серверный контекст
     * @param policy  политика (см. {@link Policy})
     * @param defaultItemLevel базовый уровень, если резолвер вернёт дефолт
     * @return результат операции
     */
    public static Result imbue(ItemStack stack, Context ctx, Policy policy, int defaultItemLevel) {
        if (stack == null || stack.isEmpty()) return Result.fail(Reason.EMPTY_STACK);

        // Проверка состояния предмета перед имбью
        boolean alreadyHas = AffixListHelper.has(stack);
        if (policy.requireEmpty && alreadyHas && !policy.allowOverwrite) {
            return Result.fail(Reason.ALREADY_IMBUED);
        }
        if (alreadyHas && !policy.allowOverwrite) {
            return Result.fail(Reason.ALREADY_IMBUED);
        }

        // Определяем источник аффиксов (оружие/броня/щит/бижу)
        Affix.Source src = policy.strictSource ? strictSourceOrFail(stack) : guessSource(stack);
        if (src == null) return Result.fail(Reason.UNSUPPORTED_ITEM);

        // Уровень предмета
        int itemLevel = Math.max(1, ITEM_LEVEL_RESOLVER.resolve(ctx, stack, Math.max(1, defaultItemLevel)));

        // Генерация и запись
        AffixListHelper.rollAndApply(stack, src, itemLevel, ctx.random());

        return Result.success(src, itemLevel);
    }

    /** Быстрый помощник с дефолтной политикой. */
    public static Result imbue(ItemStack stack, Context ctx, int defaultItemLevel) {
        return imbue(stack, ctx, Policy.DEFAULT, defaultItemLevel);
    }

    // ---------- Вспомогательные методы ----------

    /** Строгое сопоставление типа предмета к Source; null если не поддерживается. */
    private static @Nullable Affix.Source strictSourceOrFail(ItemStack stack) {
        var item = stack.getItem();
        if (item instanceof ArmorItem)  return Affix.Source.ARMOR;

        // Оружие: ванильные примеры; специальные типы добавим позже тегами grimfate
        if (item instanceof SwordItem)  return Affix.Source.WEAPON;
        if (item instanceof BowItem)    return Affix.Source.WEAPON;
        if (item instanceof CrossbowItem) return Affix.Source.WEAPON;
        if (item instanceof TridentItem)  return Affix.Source.WEAPON;

        // TODO: позже — StaffItem / DaggerItem / Jewelry через теги и интеграции
        return null;
    }

    /** Эвристика для «мягкого» режима. */
    private static Affix.Source guessSource(ItemStack stack) {
        var item = stack.getItem();
        if (item instanceof ArmorItem)  return Affix.Source.ARMOR;
        return Affix.Source.WEAPON;
    }

    // ---------- Результат ----------

    public enum Reason {
        OK,
        EMPTY_STACK,
        ALREADY_IMBUED,
        UNSUPPORTED_ITEM
    }

    public record Result(Reason reason, @Nullable Affix.Source source, int itemLevel) {
        public static Result success(Affix.Source src, int lvl) { return new Result(Reason.OK, src, Math.max(1, lvl)); }
        public static Result fail(Reason r) { return new Result(r, null, 0); }
        public boolean ok() { return reason == Reason.OK; }

        @Override
        public String toString() {
            if (ok()) return "Imbue OK{source=" + source + ", level=" + itemLevel + "}";
            return "Imbue FAIL{" + reason + "}";
        }
    }

    // ---------- Утилита: парс источника из строки (может пригодиться для будущих команд/датапаков ритуалов) ----------

    public static Affix.Source parseSource(String s, Affix.Source fallback) {
        if (s == null || s.isBlank()) return fallback;
        try {
            return Affix.Source.valueOf(s.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return fallback;
        }
    }

    // ---------- Набор поддерживаемых source для быстрых проверок (можно расширять позже) ----------

    public static EnumSet<Affix.Source> supportedSources() {
        return EnumSet.of(Affix.Source.WEAPON, Affix.Source.ARMOR, Affix.Source.SHIELD, Affix.Source.JEWELRY);
    }
}
