// src/main/java/com/doomspire/grimfate/loot/modifier/AddTagItemsModifier.java
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
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

            // ВАЖНО: используем get(key), не getOrThrow — чтобы не падать, если тег пуст/не найден
            Optional<HolderSet.Named<Item>> optSet = itemsLookup.get(key);
            if (optSet.isEmpty()) return loot;

            HolderSet<Item> set = optSet.get();
            if (set.size() <= 0) return loot;

            RandomSource rnd = ctx.getRandom();
            Optional<Holder<Item>> picked = set.getRandomElement(rnd);
            if (picked.isEmpty()) return loot;

            int n = Math.max(0, count.sample(rnd));
            if (n <= 0) return loot;

            loot.add(new ItemStack(picked.get().value(), n));
            return loot;
        } catch (Throwable t) {
            LOG.warn("[AddTagItems] tag={} failed: {}", tagId, t.toString());
            return loot; // Никогда не валим генерацию сундука
        }
    }

    @Override
    public MapCodec<? extends net.neoforged.neoforge.common.loot.IGlobalLootModifier> codec() {
        return CODEC.get();
    }
}
