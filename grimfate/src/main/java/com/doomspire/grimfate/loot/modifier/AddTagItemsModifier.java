package com.doomspire.grimfate.loot.modifier;

import com.google.common.base.Suppliers;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.item.*;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.LootModifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.function.Supplier;

public final class AddTagItemsModifier extends LootModifier {
    private static final Logger LOG = LoggerFactory.getLogger("Grimfate/GLM-AddTagItems");

    // tag: id тега предметов (например, grimfate:loot/weapons)
    // count: IntProvider количества (по умолчанию 1)
    public static final Supplier<MapCodec<AddTagItemsModifier>> CODEC = Suppliers.memoize(() ->
            RecordCodecBuilder.mapCodec(inst ->
                    codecStart(inst).and(
                            inst.group(
                                    ResourceLocation.CODEC.fieldOf("tag").forGetter(m -> m.tagId),
                                    IntProvider.CODEC.optionalFieldOf("count", ConstantInt.of(1)).forGetter(m -> m.count)
                            )
                    ).apply(inst, AddTagItemsModifier::new)
            )
    );

    private final ResourceLocation tagId;
    private final IntProvider count;

    public AddTagItemsModifier(LootItemCondition[] conditions, ResourceLocation tagId, IntProvider count) {
        super(conditions);
        this.tagId = tagId;
        this.count = count;
    }

    @Nonnull
    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> loot, LootContext ctx) {
        try {
            var level = ctx.getLevel();
            if (level == null) return loot;

            RegistryAccess access = level.registryAccess();
            var itemsLookup = access.lookupOrThrow(Registries.ITEM);

            TagKey<Item> key = TagKey.create(Registries.ITEM, tagId);

            // Мягко читаем тег (не кидаем исключение, если его нет)
            Optional<HolderSet.Named<Item>> optSet = itemsLookup.get(key);
            if (optSet.isEmpty()) return loot;

            HolderSet<Item> set = optSet.get();

            RandomSource rnd = ctx.getRandom();
            Optional<Holder<Item>> picked = set.getRandomElement(rnd);
            if (picked.isEmpty()) return loot;

            int n = Math.max(0, count.sample(rnd));
            if (n <= 0) return loot;

            ItemStack stack = new ItemStack(picked.get().value(), n);
            loot.add(stack);

            // === ЛОГ №1: предмет добавлен из тега
            var rk = stack.getItem().builtInRegistryHolder().key();
            String idStr = (rk != null) ? rk.location().toString() : "?";
            LOG.info("[AddTagItems] +{} {} from tag {}", n, idStr, tagId);

            // === Имбую аффиксы сразу для только что добавленного предмета
            tryImbueAffixes(stack, ctx);

            return loot;
        } catch (Throwable t) {
            LOG.warn("[AddTagItems] tag={} failed: {}", tagId, t.toString());
            return loot; // Никогда не валим генерацию лута
        }
    }

    @Override
    public MapCodec<? extends net.neoforged.neoforge.common.loot.IGlobalLootModifier> codec() {
        return CODEC.get();
    }

    // ======== AFFIX IMBUE (локальный, согласован с RollAffixesLootModifier) ===================

    private static final String MODID = "grimfate";

    private static final TagKey<Item> TAG_WEAPONS =
            TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MODID, "loot/weapons"));
    private static final TagKey<Item> TAG_ARMORS  =
            TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MODID, "loot/armors"));
    private static final TagKey<Item> TAG_JEWELRY =
            TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MODID, "loot/jewelry"));

    private static boolean isOurs(ItemStack stack) {
        var key = stack.getItem().builtInRegistryHolder().key();
        return key != null && MODID.equals(key.location().getNamespace());
    }

    private static boolean isAffixable(ItemStack stack) {
        return stack.is(TAG_WEAPONS) || stack.is(TAG_ARMORS) || stack.is(TAG_JEWELRY);
    }

    private static com.doomspire.grimcore.affix.Affix.Source guessSource(Item item) {
        if (item instanceof ArmorItem)  return com.doomspire.grimcore.affix.Affix.Source.ARMOR;
        if (item instanceof ShieldItem) return com.doomspire.grimcore.affix.Affix.Source.SHIELD;
        if (item instanceof BowItem || item instanceof CrossbowItem || item instanceof TridentItem) {
            return com.doomspire.grimcore.affix.Affix.Source.WEAPON;
        }
        return com.doomspire.grimcore.affix.Affix.Source.WEAPON;
    }

    /**
     * Имбуем аффиксы сразу на добавленный стак. Те же предохранители:
     * только grimfate + только наши лут-теги + не перезаписываем существующие.
     * Пишем лог результата.
     */
    private static void tryImbueAffixes(ItemStack stack, LootContext ctx) {
        try {
            if (stack.isEmpty() || !isOurs(stack) || !isAffixable(stack)) return;
            if (com.doomspire.grimfate.item.comp.AffixListHelper.has(stack)) return; // уважаем уже «наполненные»

            var src = guessSource(stack.getItem());
            int itemLevel = 1; // см. default в глобальном роллере
            com.doomspire.grimfate.item.comp.AffixListHelper.rollAndApply(
                    stack, src, itemLevel, ctx.getRandom()
            );

            // === ЛОГ №2: результат имбью
            LOG.info("[AddTagItems] imbue: item={} has={}",
                    stack.getHoverName().getString(),
                    com.doomspire.grimfate.item.comp.AffixListHelper.has(stack));
        } catch (Throwable t) {
            LOG.debug("[AddTagItems] imbue skipped: {}", t.toString());
        }
    }
}
