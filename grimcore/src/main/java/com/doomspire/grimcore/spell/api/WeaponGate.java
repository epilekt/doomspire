package com.doomspire.grimcore.spell.api;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;

//NOTE: Утилита проверки оружия в руках игрока по {@link WeaponRequirement}.
/**
 * Основана на тегах предметов и (опционально) внешнем резолвере двуручности.
 * Спроектирована так, чтобы:
 *  - работать только на NeoForge / ванильных API 1.21.1;
 *  - не иметь жёстких зависимостей на grimfate/контент;
 *  - легко расшириться профилем оружия позже (WeaponProfileComponent).
 *
 * Как работает:
 *  1) Проверяем main hand: теги any/all/none.
 *  2) Если требование не выполнено — пробуем offhand (для кинжалов и т.п., где каста
 *     может быть "из левой руки"). Это поведение можно отключить, если нужно — на уровне вызова.
 *  3) Если requirement.twoHandedOnly() == true:
 *      - предмет обязан иметь тег TWO_HANDED (по умолчанию grimfate:two_handed)
 *        или вернёт true внешний резолвер двуручности;
 *      - если offhand НЕ пуст и предмет двуручный — отказ (блок оффхенда).
 *
 * Внешний резолвер двуручности:
 *  Можно (необязательно) установить через {@link #setTwoHandedResolver(BiFunction)},
 *  например, чтобы читать ваш WeaponProfileComponent без прямой зависимости.
 */

public final class WeaponGate {

    /** Глобальный тег «двуручное» — по умолчанию grimfate:two_handed. */
    public static final TagKey<Item> TWO_HANDED =
            TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("grimfate", "two_handed"));

    /** Необязательный внешний резолвер двуручности (например, из WeaponProfileComponent). */
    private static volatile BiFunction<ItemStack, Player, Boolean> TWO_HANDED_RESOLVER = null;

    private WeaponGate() {}

    /**
     * Основная проверка: смотрим main hand, затем offhand.
     * Возвращает объект-результат с причиной отказа (для UX) либо OK.
     */
    public static Result check(Player player, WeaponRequirement req) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(req, "requirement");

        // 1) Пытаемся удовлетворить требование предметом из main hand
        ItemStack main = player.getMainHandItem();
        Result mainRes = checkStackAgainstRequirement(main, player, req, InteractionHand.MAIN_HAND, true);
        if (mainRes.ok) return mainRes;

        // 2) Если не прошло — пробуем offhand (некоторые умения могут кастоваться из левой руки)
        ItemStack off = player.getOffhandItem();
        Result offRes = checkStackAgainstRequirement(off, player, req, InteractionHand.OFF_HAND, true);
        if (offRes.ok) return offRes;

        // 3) Оба варианта не подошли — вернём более «информативную» причину
        //    (предпочтительно с main hand, чтобы игрок сначала проверил основной слот)
        return preferInformative(mainRes, offRes);
    }

    /**
     * Точечная проверка конкретного стака и руки.
     * @param enforceOffhandBlock если true, при двуручности запретим занятый offhand.
     */
    public static Result checkStackAgainstRequirement(ItemStack stack,
                                                      Player player,
                                                      WeaponRequirement req,
                                                      InteractionHand hand,
                                                      boolean enforceOffhandBlock) {
        if (stack.isEmpty()) {
            return Result.fail("Требуется подходящее оружие в " + prettyHand(hand) + ".");
        }

        // Быстрые проверки тегов
        if (!matchesAny(stack, req.anyOfTags())) {
            return Result.fail("Оружие в " + prettyHand(hand) + " не подходит (нет нужной категории).");
        }
        if (!matchesAll(stack, req.allOfTags())) {
            return Result.fail("Оружие в " + prettyHand(hand) + " не подходит (не все требования выполнены).");
        }
        if (!matchesNone(stack, req.noneOfTags())) {
            return Result.fail("Оружие в " + prettyHand(hand) + " запрещено для этого умения.");
        }

        // Двуручность — отдельной проверкой (часто встречается и влияет на оффхенд/анимации)
        if (req.twoHandedOnly()) {
            boolean isTwoHanded = isTwoHanded(stack, player);
            if (!isTwoHanded) {
                return Result.fail("Нужно двуручное оружие.");
            }
            if (enforceOffhandBlock && !player.getOffhandItem().isEmpty()) {
                // Блокировка использования оффхенда с реальным двуручным оружием.
                return Result.fail("Освободите вторую руку для двуручного оружия.");
            }
        }

        return Result.ok(hand);
    }

    // ---------- Внешняя интеграция (настраиваемые хуки) ----------

    /**
     * Установить внешний резолвер двуручности (например, чтобы читать WeaponProfileComponent).
     * Возвращайте TRUE, если предмет двуручный по вашему профилю.
     */
    public static void setTwoHandedResolver(BiFunction<ItemStack, Player, Boolean> resolver) {
        TWO_HANDED_RESOLVER = resolver;
    }

    // ---------- Внутренняя логика ----------

    private static boolean isTwoHanded(ItemStack stack, Player player) {
        // 1) Наш общий тег two_handed (grimfate:two_handed)
        if (stack.is(TWO_HANDED)) return true;

        // 2) Внешний резолвер (например, WeaponProfileComponent из grimfate)
        BiFunction<ItemStack, Player, Boolean> resolver = TWO_HANDED_RESOLVER;
        if (resolver != null) {
            try {
                Boolean r = resolver.apply(stack, player);
                if (Boolean.TRUE.equals(r)) return true;
            } catch (Throwable ignored) {
                // Безопасность: внешняя интеграция не должна ломать гейт
            }
        }

        return false;
    }

    private static boolean matchesAny(ItemStack stack, Set<TagKey<Item>> tags) {
        if (tags == null || tags.isEmpty()) return true; // нет требований — проходим
        for (TagKey<Item> t : tags) if (stack.is(t)) return true;
        return false;
    }

    private static boolean matchesAll(ItemStack stack, Set<TagKey<Item>> tags) {
        if (tags == null || tags.isEmpty()) return true;
        for (TagKey<Item> t : tags) if (!stack.is(t)) return false;
        return true;
    }

    private static boolean matchesNone(ItemStack stack, Set<TagKey<Item>> tags) {
        if (tags == null || tags.isEmpty()) return true;
        for (TagKey<Item> t : tags) if (stack.is(t)) return false;
        return true;
    }

    private static String prettyHand(InteractionHand hand) {
        return hand == InteractionHand.MAIN_HAND ? "основной руке" : "второй руке";
    }

    private static Result preferInformative(Result a, Result b) {
        // Если одна из причин более конкретна — вернём её.
        // На практике причина с двуручностью/оффхендом полезнее, чем просто "нет тега".
        String ra = a.reason == null ? "" : a.reason;
        String rb = b.reason == null ? "" : b.reason;
        // Очень простая эвристика: более длинное сообщение считаем информативнее.
        return (ra.length() >= rb.length()) ? a : b;
    }

    // ---------- Результат проверки ----------

    public static final class Result {
        public final boolean ok;
        public final String reason;              // для UX: почему отказано (локализуем позже)
        public final InteractionHand usedHand;   // какой рукой можно кастовать (если ok)

        private Result(boolean ok, String reason, InteractionHand usedHand) {
            this.ok = ok;
            this.reason = reason;
            this.usedHand = usedHand;
        }

        public static Result ok(InteractionHand hand) {
            return new Result(true, null, hand);
        }

        public static Result fail(String reason) {
            return new Result(false, reason, null);
        }

        @Override
        public String toString() {
            return ok ? "OK(" + usedHand + ")" : "FAIL(" + reason + ")";
        }
    }
}
