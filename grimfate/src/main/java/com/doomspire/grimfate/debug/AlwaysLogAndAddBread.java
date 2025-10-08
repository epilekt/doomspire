package com.doomspire.grimfate.debug;

import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AlwaysLogAndAddBread implements IGlobalLootModifier {
    private static final Logger LOG = LoggerFactory.getLogger("Grimfate/GLM-DEBUG");
    public static final MapCodec<AlwaysLogAndAddBread> CODEC = MapCodec.unit(new AlwaysLogAndAddBread());

    @Override public MapCodec<? extends IGlobalLootModifier> codec() { return CODEC; }

    @Override
    public ObjectArrayList<ItemStack> apply(ObjectArrayList<ItemStack> loot, LootContext ctx) {
        boolean hasBlock  = ctx.hasParam(LootContextParams.BLOCK_STATE);
        boolean hasEntity = ctx.hasParam(LootContextParams.THIS_ENTITY);
        boolean hasTool   = ctx.hasParam(LootContextParams.TOOL);
        var dim = ctx.getLevel().dimension().location();
        LOG.info("[GLM] apply() called: hasBlock={}, hasEntity={}, hasTool={}, dim={}", hasBlock, hasEntity, hasTool, dim);

        loot.add(new ItemStack(Items.BREAD));
        return loot;
    }
}
