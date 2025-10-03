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

/**
 * Проверяет предмет в руках игрока против {@link WeaponRequirement}.
 * Учитывает теги, (опционально) двуручность и занятость offhand.
 */
public final class WeaponGate {

    /** Глобальный тег «двуручное» — grimfate:two_handed. */
    public static final TagKey<Item> TWO_HANDED =
            TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("grimfate", "two_handed"));

    /** Внешний резолвер двуручности (например, читает WeaponProfileComponent в контент-модуле). */
    private static volatile BiFunction<ItemStack, Player, Boolean> TWO_HANDED_RESOLVER = null;

    private WeaponGate() {}

    /** Основная проверка: сначала main hand, затем offhand. */
    public static Result check(Player player, WeaponRequirement req) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(req, "requirement");

        ItemStack main = player.getMainHandItem();
        Result mainRes = checkStackAgainstRequirement(main, player, req, InteractionHand.MAIN_HAND, true);
        if (mainRes.ok) return mainRes;

        ItemStack off = player.getOffhandItem();
        Result offRes = checkStackAgainstRequirement(off, player, req, InteractionHand.OFF_HAND, true);
        if (offRes.ok) return offRes;

        return preferInformative(mainRes, offRes);
    }

    /**
     * Точечная проверка стака в конкретной руке.
     * @param enforceOffhandBlock если true — при двуручности запрещаем занятый offhand.
     */
    public static Result checkStackAgainstRequirement(ItemStack stack,
                                                      Player player,
                                                      WeaponRequirement req,
                                                      InteractionHand hand,
                                                      boolean enforceOffhandBlock) {
        if (stack.isEmpty()) {
            return Result.failKey("grimfate.msg.weapon_required",
                    "Требуется подходящее оружие в " + prettyHand(hand) + ".");
        }

        // Теги
        if (!matchesAny(stack, req.anyOfTags())) {
            return Result.failKey("grimfate.msg.weapon_required",
                    "Оружие в " + prettyHand(hand) + " не подходит (нет нужной категории).");
        }
        if (!matchesAll(stack, req.allOfTags())) {
            return Result.failKey("grimfate.msg.weapon_required",
                    "Оружие в " + prettyHand(hand) + " не подходит (не все требования выполнены).");
        }
        if (!matchesNone(stack, req.noneOfTags())) {
            return Result.failKey("grimfate.msg.weapon_forbidden",
                    "Оружие в " + prettyHand(hand) + " запрещено для этого умения.");
        }

        // Двуручность
        if (req.twoHandedOnly()) {
            boolean isTwoHanded = isTwoHanded(stack, player);
            if (!isTwoHanded) {
                return Result.failKey("grimfate.msg.two_handed_needed", "Нужно двуручное оружие.");
            }
            if (enforceOffhandBlock && !player.getOffhandItem().isEmpty()) {
                return Result.failKey("grimfate.msg.free_offhand", "Освободите вторую руку для двуручного оружия.");
            }
        }

        return Result.ok(hand);
    }

    // ---------- внешняя интеграция ----------
    /** Установить внешний резолвер двуручности. Возвращайте TRUE, если предмет двуручный. */
    public static void setTwoHandedResolver(BiFunction<ItemStack, Player, Boolean> resolver) {
        TWO_HANDED_RESOLVER = resolver;
    }

    // ---------- внутренняя логика ----------
    private static boolean isTwoHanded(ItemStack stack, Player player) {
        if (stack.is(TWO_HANDED)) return true;
        BiFunction<ItemStack, Player, Boolean> resolver = TWO_HANDED_RESOLVER;
        if (resolver != null) {
            try {
                Boolean r = resolver.apply(stack, player);
                if (Boolean.TRUE.equals(r)) return true;
            } catch (Throwable ignored) {}
        }
        return false;
    }

    private static boolean matchesAny(ItemStack stack, Set<TagKey<Item>> tags) {
        if (tags == null || tags.isEmpty()) return true;
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
        String ra = a.reason == null ? "" : a.reason;
        String rb = b.reason == null ? "" : b.reason;
        return (ra.length() >= rb.length()) ? a : b;
    }

    // ---------- результат ----------
    public static final class Result {
        public final boolean ok;
        /** Локализуемый ключ (если не null) — для UX сообщений на клиенте. */
        public final String reasonKey;
        /** Человекочитаемое пояснение (fallback, может быть на любом языке). */
        public final String reason;
        public final InteractionHand usedHand;

        private Result(boolean ok, String reasonKey, String reason, InteractionHand usedHand) {
            this.ok = ok;
            this.reasonKey = reasonKey;
            this.reason = reason;
            this.usedHand = usedHand;
        }

        public static Result ok(InteractionHand hand) {
            return new Result(true, null, null, hand);
        }

        public static Result failKey(String key, String fallbackText) {
            return new Result(false, key, fallbackText, null);
        }

        public static Result fail(String fallbackText) {
            return new Result(false, null, fallbackText, null);
        }

        @Override
        public String toString() {
            return ok ? "OK(" + usedHand + ")" : "FAIL(key=" + reasonKey + ", text=" + reason + ")";
        }
    }
}
