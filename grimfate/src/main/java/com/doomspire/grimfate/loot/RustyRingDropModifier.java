package com.doomspire.grimfate.loot;

import com.doomspire.grimcore.data.ModDataComponents;
import com.doomspire.grimcore.data.component.StatBonusComponent;
import com.doomspire.grimcore.stat.Attributes;
import com.doomspire.grimfate.core.Grimfate;
import com.doomspire.grimfate.registry.ModItems;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

import java.util.Map;

/** Простой GLM: с заданным шансом добавляет grimfate:rusty_ring (+1 SPIRIT). */
public class RustyRingDropModifier extends LootModifier {

    public static final MapCodec<RustyRingDropModifier> CODEC = RecordCodecBuilder.mapCodec(inst ->
            LootModifier.codecStart(inst)
                    .and(Codec.DOUBLE.fieldOf("chance").forGetter(m -> m.chance))
                    .apply(inst, RustyRingDropModifier::new)
    );

    private final double chance;

    public RustyRingDropModifier(LootItemCondition[] conditions, double chance) {
        super(conditions);
        this.chance = chance;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() { return CODEC; }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext ctx) {
        // ✅ Ограничиваемся сундуками: имя лут-таблицы должно содержать "chests/"
        ResourceLocation tableId = ctx.getQueriedLootTableId();
        if (tableId == null || !tableId.getPath().contains("chests/")) return generatedLoot;

        if (ctx.getRandom().nextDouble() > this.chance) return generatedLoot;

        ItemStack ring = new ItemStack(ModItems.RUSTY_RING.get());

        // Компонент: +1 к SPIRIT
        ring.set(ModDataComponents.STAT_BONUS.get(),
                new StatBonusComponent(Map.of(Attributes.SPIRIT, 1)));

        generatedLoot.add(ring);
        Grimfate.LOGGER.debug("[GLM] rusty_ring: ADDED to {}", tableId);
        return generatedLoot;
    }
}
