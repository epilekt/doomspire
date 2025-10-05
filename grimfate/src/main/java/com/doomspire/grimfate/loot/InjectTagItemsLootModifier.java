package com.doomspire.grimfate.loot;

import com.mojang.serialization.Codec;
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
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.LootModifier;

import java.util.List;

/*
//NOTE: GLM-инжектор — добавляет предметы из item-тега в сгенерированный лут.
// Важно: если тега нет — молча выходим (никаких крэшей).
*/
public final class InjectTagItemsLootModifier extends LootModifier {

    public static final MapCodec<InjectTagItemsLootModifier> CODEC = RecordCodecBuilder.mapCodec(i -> codecStart(i)
            .and(ResourceLocation.CODEC.fieldOf("tag").forGetter(m -> m.tag.location()))
            .and(Codec.FLOAT.optionalFieldOf("chance", 1.0f).forGetter(m -> m.chance))
            .and(IntProvider.CODEC.optionalFieldOf("count", UniformInt.of(1, 1)).forGetter(m -> m.count))
            .and(Codec.BOOL.optionalFieldOf("expand_all", false).forGetter(m -> m.expandAll))
            .apply(i, (conditions, tagRl, chance, count, expand) ->
                    new InjectTagItemsLootModifier(conditions, TagKey.create(Registries.ITEM, tagRl), chance, count, expand)));

    private final TagKey<Item> tag;
    private final float chance;
    private final IntProvider count;
    private final boolean expandAll;

    public InjectTagItemsLootModifier(LootItemCondition[] conditionsIn,
                                      TagKey<Item> tag,
                                      float chance,
                                      IntProvider count,
                                      boolean expandAll) {
        super(conditionsIn);
        this.tag = tag;
        this.chance = Math.max(0f, Math.min(1f, chance));
        this.count = count;
        this.expandAll = expandAll;
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        final RandomSource rng = context.getRandom();
        if (rng.nextFloat() > chance) return generatedLoot;

        int rolls = Math.max(0, count.sample(rng));
        if (rolls == 0) return generatedLoot;

        // Безопасно получаем набор предметов по тегу (если тега нет — выходим)
        RegistryAccess access = context.getLevel().registryAccess();
        var itemsLookup = access.lookupOrThrow(Registries.ITEM); // сам реестр есть всегда
        var setOpt = itemsLookup.get(tag);                       // а вот конкретный тег может отсутствовать
        if (setOpt.isEmpty()) return generatedLoot;

        HolderSet<Item> set = setOpt.get();
        if (set.size() == 0) return generatedLoot;

        for (int r = 0; r < rolls; r++) {
            if (expandAll) {
                for (Holder<Item> holder : set) {
                    generatedLoot.add(new ItemStack(holder.value()));
                }
            } else {
                var opt = set.getRandomElement(rng);
                opt.ifPresent(h -> generatedLoot.add(new ItemStack(h.value())));
            }
        }
        return generatedLoot;
    }

    @Override
    public MapCodec<? extends LootModifier> codec() {
        return CODEC;
    }
}
