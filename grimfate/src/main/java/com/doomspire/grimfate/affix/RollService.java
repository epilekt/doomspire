package com.doomspire.grimfate.affix;

import com.doomspire.grimcore.affix.Affix;
import com.doomspire.grimcore.affix.def.AffixDataManager;
import com.doomspire.grimcore.affix.def.AffixDef;
import com.doomspire.grimcore.affix.pool.AffixPoolDataManager;
import com.doomspire.grimcore.affix.pool.AffixPoolDef;
import com.doomspire.grimcore.affix.rarity.RarityDataManager;
import com.doomspire.grimcore.affix.rarity.RarityDef;
import com.doomspire.grimfate.item.comp.AffixListComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.*;

/*
//NOTE: Сервис роллинга предметов. Генерирует редкость и аффиксы на основе пулов, уровня и источника. Пишет в AffixListComponent.
*/
public final class RollService {
    private RollService() {}

    public static AffixListComponent roll(Item item, Set<TagKey<Item>> itemTags, Affix.Source source, int itemLevel, RandomSource random) {
        // 1) Редкость
        Optional<RarityDef> rarityOpt = RarityDataManager.INSTANCE.sample(random);
        if (rarityOpt.isEmpty()) {
            // предмет без аффиксов
            return new AffixListComponent(List.of());
        }
        RarityDef rarity = rarityOpt.get();

        int maxAffixes = rarity.sampleMaxAffixes(random);
        if (maxAffixes <= 0) {
            return new AffixListComponent(List.of());
        }

        // 2) Пулы
        List<AffixPoolDef> pools = AffixPoolDataManager.INSTANCE.filterApplicable(item, itemTags, source, itemLevel);
        if (pools.isEmpty()) {
            return new AffixListComponent(List.of());
        }

        // 3) Генерация
        List<AffixListComponent.Entry> entries = new ArrayList<>(maxAffixes);
        Set<ResourceLocation> used = new HashSet<>();

        for (int i = 0; i < maxAffixes; i++) {
            AffixPoolDef pool = pools.get(random.nextInt(pools.size()));
            var affixOpt = AffixPoolDataManager.INSTANCE.sample(pool, itemLevel, random);
            if (affixOpt.isEmpty()) continue;

            var entry = affixOpt.get();
            var affixId = entry.affixId();

            if (!pool.uniqueTypes() && used.contains(affixId)) {
                // повторы разрешены — ничего
            } else if (used.contains(affixId)) {
                continue;
            }

            Optional<AffixDef> defOpt = AffixDataManager.INSTANCE.get(affixId);
            if (defOpt.isEmpty()) continue;
            AffixDef def = defOpt.get();

            if (!def.allowSources().contains(source)) continue;

            int rolls = entry.rollsPerAffixOverride().sample(random);
            if (rolls <= 0) rolls = rarity.sampleRollsPerAffix(random);

            float valueSum = 0f;
            for (int r = 0; r < rolls; r++) {
                valueSum += def.sampleBase(random);
            }
            float avg = valueSum / rolls;
            float scaled = rarity.scaleMagnitude(avg);
            float clamped = def.clampTotal(scaled);

            entries.add(new AffixListComponent.Entry(affixId.toString(), List.of(clamped)));
            used.add(affixId);
        }

        return new AffixListComponent(entries);
    }
}
