package com.doomspire.grimfate.loot.modifier;

import com.google.common.base.Suppliers;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.LootModifier;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public final class AddItemModifier extends LootModifier {
    public static final Supplier<MapCodec<AddItemModifier>> CODEC = Suppliers.memoize(() ->
            RecordCodecBuilder.mapCodec(inst ->
                    codecStart(inst).and(
                            inst.group(
                                    BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(m -> m.addedItem),
                                    IntProvider.CODEC.optionalFieldOf("count", ConstantInt.of(1)).forGetter(m -> m.count)
                            )
                    ).apply(inst, AddItemModifier::new)
            )
    );

    private final Item addedItem;
    private final IntProvider count;

    public AddItemModifier(LootItemCondition[] conditionsIn, Item addedItem, IntProvider count) {
        super(conditionsIn);
        this.addedItem = addedItem;
        this.count = count;
    }

    @Nonnull
    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> loot, LootContext ctx) {
        int n = Math.max(0, count.sample(ctx.getRandom()));
        if (n <= 0) return loot;

        ItemStack stack = new ItemStack(addedItem, n);
        if (stack.getCount() <= stack.getMaxStackSize()) {
            loot.add(stack);
        } else {
            int left = stack.getCount();
            while (left > 0) {
                ItemStack sub = new ItemStack(addedItem, Math.min(left, stack.getMaxStackSize()));
                left -= sub.getCount();
                loot.add(sub);
            }
        }
        return loot;
    }

    @Override
    public MapCodec<? extends net.neoforged.neoforge.common.loot.IGlobalLootModifier> codec() {
        return CODEC.get();
    }
}
