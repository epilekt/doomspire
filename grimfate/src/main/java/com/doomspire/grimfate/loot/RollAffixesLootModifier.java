package com.doomspire.grimfate.loot;

import com.doomspire.grimcore.affix.Affix;
import com.doomspire.grimfate.item.comp.AffixListHelper;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.*;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.LootModifier;

import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/*
//NOTE: Global Loot Modifier: навешивает аффиксы на дроп (оружие/броню/щит). Крафтовые предметы не трогаем — их будет «наполнять» Идол.
*/
public final class RollAffixesLootModifier extends LootModifier {

    public static final MapCodec<RollAffixesLootModifier> CODEC = RecordCodecBuilder.mapCodec(inst -> codecStart(inst).and(
            Codec.STRING.listOf().optionalFieldOf("allow_sources", List.of("WEAPON","ARMOR","SHIELD"))
                    .forGetter(m -> m.allowSources.stream().map(Enum::name).toList())
    ).and(
            Codec.BOOL.optionalFieldOf("replace_existing", false).forGetter(m -> m.replaceExisting)
    ).and(
            Codec.INT.optionalFieldOf("default_item_level", 1).forGetter(m -> m.defaultItemLevel)
    ).apply(inst, (conditions, sources, replace, lvl) -> new RollAffixesLootModifier(conditions, parseSources(sources), replace, lvl)));

    private static Set<Affix.Source> parseSources(List<String> tokens) {
        var set = EnumSet.noneOf(Affix.Source.class);
        for (String t : tokens) {
            var key = t.trim().toUpperCase(Locale.ROOT);
            if (key.equals("ALL")) { for (var s : Affix.Source.values()) set.add(s); break; }
            set.add(Affix.Source.valueOf(key));
        }
        if (set.isEmpty()) { set.add(Affix.Source.WEAPON); set.add(Affix.Source.ARMOR); set.add(Affix.Source.SHIELD); }
        return Set.copyOf(set);
    }

    private final Set<Affix.Source> allowSources;
    private final boolean replaceExisting;
    private final int defaultItemLevel;

    public RollAffixesLootModifier(LootItemCondition[] conditionsIn,
                                   Set<Affix.Source> allowSources,
                                   boolean replaceExisting,
                                   int defaultItemLevel) {
        super(conditionsIn);
        this.allowSources = allowSources;
        this.replaceExisting = replaceExisting;
        this.defaultItemLevel = Math.max(1, defaultItemLevel);
    }

    @FunctionalInterface
    public interface ItemLevelResolver { int resolve(LootContext ctx, ItemStack stack, int defaultLevel); }
    private static volatile ItemLevelResolver ITEM_LEVEL_RESOLVER = (ctx, st, def) -> def;
    public static void setItemLevelResolver(ItemLevelResolver resolver) { if (resolver != null) ITEM_LEVEL_RESOLVER = resolver; }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        if (generatedLoot == null || generatedLoot.isEmpty()) return generatedLoot;

        final RandomSource rnd = context.getRandom();

        for (ItemStack stack : generatedLoot) {
            if (stack == null || stack.isEmpty()) continue;
            if (!replaceExisting && AffixListHelper.has(stack)) continue;

            Affix.Source src = guessSource(stack);
            if (!allowSources.contains(src)) continue;

            int itemLevel = ITEM_LEVEL_RESOLVER.resolve(context, stack, defaultItemLevel);
            AffixListHelper.rollAndApply(stack, src, itemLevel, rnd);
        }
        return generatedLoot;
    }

    @Override
    public MapCodec<? extends LootModifier> codec() { return CODEC; }

    private static Affix.Source guessSource(ItemStack stack) {
        var item = stack.getItem();
        if (item instanceof ArmorItem)  return Affix.Source.ARMOR;
        if (item instanceof ShieldItem) return Affix.Source.SHIELD;
        if (item instanceof SwordItem)  return Affix.Source.WEAPON;
        if (item instanceof BowItem || item instanceof CrossbowItem || item instanceof TridentItem) return Affix.Source.WEAPON;
        return Affix.Source.WEAPON;
    }
}
