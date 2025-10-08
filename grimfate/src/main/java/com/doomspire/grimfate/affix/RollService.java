package com.doomspire.grimfate.affix;

import com.doomspire.grimcore.affix.Affix;
import com.doomspire.grimcore.affix.def.AffixDataManager;
import com.doomspire.grimcore.affix.def.AffixDef;
import com.doomspire.grimcore.affix.pool.AffixPoolDataManager;
import com.doomspire.grimcore.affix.pool.AffixPoolDef;
import com.doomspire.grimcore.affix.rarity.RarityDataManager;
import com.doomspire.grimcore.affix.rarity.RarityDef;
import com.doomspire.grimfate.item.comp.AffixListComponent;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;

import java.util.*;

/*
//NOTE: Сервис роллинга предметов. Генерирует редкость и аффиксы на основе пулов, уровня и источника. Пишет в AffixListComponent.
*/
public final class RollService {
    private RollService() {}

    private static final String MODID = "grimfate";

    // Жёсткие теги аффиксируемых категорий
    private static final TagKey<Item> TAG_WEAPONS = TagKey.create(Registries.ITEM, rl("loot/weapons"));
    private static final TagKey<Item> TAG_ARMORS  = TagKey.create(Registries.ITEM, rl("loot/armors"));
    private static final TagKey<Item> TAG_JEWELRY = TagKey.create(Registries.ITEM, rl("loot/jewelry"));

    private static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    /** Разрешены только предметы нашего мода и только из целевых тегов. */
    private static boolean isOurGear(Item item, Set<TagKey<Item>> itemTags) {
        ResourceKey<Item> key = item.builtInRegistryHolder().key(); // 1.21.1: это НЕ Optional
        if (key == null || !MODID.equals(key.location().getNamespace())) return false;
        // хотя бы один из наших тегов
        return itemTags.contains(TAG_WEAPONS) || itemTags.contains(TAG_ARMORS) || itemTags.contains(TAG_JEWELRY);
    }

    public static AffixListComponent roll(Item item, Set<TagKey<Item>> itemTags, Affix.Source source, int itemLevel, RandomSource random) {
        // --- Жёсткий предохранитель: только наш гир (namespace + целевые теги) ---
        if (!isOurGear(item, itemTags)) {
            return new AffixListComponent(List.of());
        }

        // 1) Редкость
        Optional<RarityDef> rarityOpt = RarityDataManager.INSTANCE.sample(random);
        if (rarityOpt.isEmpty()) {
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

            var picked = affixOpt.get();
            var affixId = picked.affixId();

            // Повторы по id — только если пул это разрешает
            if (used.contains(affixId) && pool.uniqueTypes()) {
                continue;
            }

            Optional<AffixDef> defOpt = AffixDataManager.INSTANCE.get(affixId);
            if (defOpt.isEmpty()) continue;
            AffixDef def = defOpt.get();

            // Источник должен совпадать с разрешёнными у аффикса
            if (!def.allowSources().contains(source)) continue;

            int rolls = picked.rollsPerAffixOverride().sample(random);
            if (rolls <= 0) rolls = rarity.sampleRollsPerAffix(random);

            float valueSum = 0f;
            for (int r = 0; r < rolls; r++) {
                valueSum += def.sampleBase(random);
            }
            float avg     = valueSum / Math.max(1, rolls);
            float scaled  = rarity.scaleMagnitude(avg);
            float clamped = def.clampTotal(scaled);

            entries.add(new AffixListComponent.Entry(affixId.toString(), List.of(clamped)));
            used.add(affixId);
        }

        return new AffixListComponent(entries);
    }
}
