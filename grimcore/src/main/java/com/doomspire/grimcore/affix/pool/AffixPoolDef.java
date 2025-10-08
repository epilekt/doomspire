package com.doomspire.grimcore.affix.pool;

import com.doomspire.grimcore.affix.Affix;
import com.doomspire.grimcore.affix.codec.AffixSourceSetCodec;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.item.Item;

import java.util.List;
import java.util.Set;

/*
//NOTE: Data-driven пул аффиксов для роллинга лута/имбуинга.
// Содержит записи (affix_id + вес), фильтры по источнику, тегам предмета и уровню itemLevel.
*/
public record AffixPoolDef(
        ResourceLocation id,
        List<Entry> entries,
        Set<Affix.Source> allowSources,
        List<TagKey<Item>> requiredItemTags,
        int minItemLevel,
        int maxItemLevel,
        boolean uniqueTypes,
        IntProvider maxAffixesOverride
) {
    public static final Codec<TagKey<Item>> ITEM_TAG_CODEC =
            ResourceLocation.CODEC.xmap(rl -> TagKey.create(Registries.ITEM, rl), TagKey::location);

    public static final Codec<AffixPoolDef> CODEC = RecordCodecBuilder.create(i -> i.group(
            ResourceLocation.CODEC.fieldOf("id").forGetter(AffixPoolDef::id),
            Entry.CODEC.listOf().fieldOf("entries").forGetter(AffixPoolDef::entries),
            AffixSourceSetCodec.INSTANCE.optionalFieldOf("allow_sources", Set.of(Affix.Source.WEAPON, Affix.Source.ARMOR, Affix.Source.JEWELRY, Affix.Source.OTHER)).forGetter(AffixPoolDef::allowSources),
            ITEM_TAG_CODEC.listOf().optionalFieldOf("required_item_tags", List.of()).forGetter(AffixPoolDef::requiredItemTags),
            Codec.INT.optionalFieldOf("min_item_level", 1).forGetter(AffixPoolDef::minItemLevel),
            Codec.INT.optionalFieldOf("max_item_level", 0).forGetter(AffixPoolDef::maxItemLevel),
            Codec.BOOL.optionalFieldOf("unique_types", true).forGetter(AffixPoolDef::uniqueTypes),
            IntProvider.CODEC.optionalFieldOf("max_affixes_override", ConstantInt.of(0)).forGetter(AffixPoolDef::maxAffixesOverride)
    ).apply(i, AffixPoolDef::new));

    public boolean matchesItemLevel(int itemLevel) {
        if (itemLevel < Math.max(1, minItemLevel)) return false;
        if (maxItemLevel > 0 && itemLevel > maxItemLevel) return false;
        return true;
    }

    public record Entry(
            ResourceLocation affixId,
            int weight,
            int perItemMinLevel,
            int perItemMaxLevel,
            IntProvider rollsPerAffixOverride
    ) {
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(i -> i.group(
                ResourceLocation.CODEC.fieldOf("affix_id").forGetter(Entry::affixId),
                Codec.intRange(1, Integer.MAX_VALUE).fieldOf("weight").forGetter(Entry::weight),
                Codec.INT.optionalFieldOf("min_item_level", 1).forGetter(Entry::perItemMinLevel),
                Codec.INT.optionalFieldOf("max_item_level", 0).forGetter(Entry::perItemMaxLevel),
                IntProvider.CODEC.optionalFieldOf("rolls_override", ConstantInt.of(0)).forGetter(Entry::rollsPerAffixOverride)
        ).apply(i, Entry::new));

        public boolean levelOk(int itemLevel) {
            if (itemLevel < Math.max(1, perItemMinLevel)) return false;
            if (perItemMaxLevel > 0 && itemLevel > perItemMaxLevel) return false;
            return true;
        }
    }
}
