package com.doomspire.grimfate.loot.modifier;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.LootModifier;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public final class ReplaceItemModifier extends LootModifier {
    public static final Supplier<MapCodec<ReplaceItemModifier>> CODEC = Suppliers.memoize(() ->
            RecordCodecBuilder.mapCodec(inst ->
                    codecStart(inst).and(
                            inst.group(
                                    // Выбор цели: либо конкретный item, либо item-тег
                                    BuiltInRegistries.ITEM.byNameCodec().optionalFieldOf("match_item")
                                            .forGetter(m -> m.matchItem),
                                    ResourceLocation.CODEC.optionalFieldOf("match_tag")
                                            .forGetter(m -> m.matchTagId),
                                    // Чем заменить:
                                    BuiltInRegistries.ITEM.byNameCodec().fieldOf("replace_with")
                                            .forGetter(m -> m.replaceWith)
                            )
                    ).apply(inst, ReplaceItemModifier::new)
            )
    );

    private final java.util.Optional<Item> matchItem;
    private final java.util.Optional<ResourceLocation> matchTagId;
    private final Item replaceWith;

    public ReplaceItemModifier(LootItemCondition[] cond, java.util.Optional<Item> matchItem,
                               java.util.Optional<ResourceLocation> matchTagId, Item replaceWith) {
        super(cond);
        this.matchItem = matchItem;
        this.matchTagId = matchTagId;
        this.replaceWith = replaceWith;
    }

    @Nonnull
    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> loot, LootContext ctx) {
        boolean useTag = matchTagId.isPresent();
        HolderSet<Item> tag = null;

        if (useTag) {
            var access = ctx.getLevel().registryAccess().lookupOrThrow(Registries.ITEM);
            tag = access.getOrThrow(TagKey.create(Registries.ITEM, matchTagId.get()));
        }

        for (int i = 0; i < loot.size(); i++) {
            ItemStack s = loot.get(i);
            boolean match = matchItem.map(it -> s.is(it)).orElse(false)
                    || (useTag && tag != null && s.is(tag));
            if (match) {
                loot.set(i, new ItemStack(replaceWith, s.getCount()));
            }
        }
        return loot;
    }

    @Override
    public MapCodec<? extends net.neoforged.neoforge.common.loot.IGlobalLootModifier> codec() {
        return CODEC.get();
    }
}
