package com.doomspire.grimfate.loot.modifier;

import com.google.common.base.Suppliers;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.LootModifier;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public final class AddTagItemsModifier extends LootModifier {
    public static final Supplier<MapCodec<AddTagItemsModifier>> CODEC = Suppliers.memoize(() ->
            RecordCodecBuilder.mapCodec(inst ->
                    codecStart(inst).and(
                            inst.group(
                                    ResourceLocation.CODEC.fieldOf("tag").forGetter(m -> m.tagId),
                                    IntProvider.CODEC.optionalFieldOf("count", ConstantInt.of(1)).forGetter(m -> m.rolls),
                                    // если true — добавить "rolls" штук (с повторами),
                                    // если false — добавить только 1 предмет (один выбор)
                                    com.mojang.serialization.Codec.BOOL.optionalFieldOf("expand_all", false).forGetter(m -> m.expandAll)
                            )
                    ).apply(inst, AddTagItemsModifier::new)
            )
    );

    private final ResourceLocation tagId;
    private final IntProvider rolls;
    private final boolean expandAll;

    public AddTagItemsModifier(LootItemCondition[] cond, ResourceLocation tagId, IntProvider rolls, boolean expandAll) {
        super(cond);
        this.tagId = tagId;
        this.rolls = rolls;
        this.expandAll = expandAll;
    }

    @Nonnull
    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> loot, LootContext ctx) {
        var access = ctx.getLevel().registryAccess();
        var itemRegistry = access.lookupOrThrow(Registries.ITEM);
        TagKey<Item> key = TagKey.create(Registries.ITEM, tagId);
        HolderSet<Item> tag = itemRegistry.getOrThrow(key);
        if (tag.size() == 0) return loot;

        net.minecraft.util.RandomSource rnd = ctx.getRandom();
        var holder = tag.get(rnd.nextInt(tag.size())); // взять по индексу
        loot.add(new ItemStack(holder.value()));

        return loot;
    }


    @Override
    public MapCodec<? extends net.neoforged.neoforge.common.loot.IGlobalLootModifier> codec() {
        return CODEC.get();
    }
}

