package com.doomspire.grimcore.affix.pool;

import com.doomspire.grimcore.affix.Affix;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/*
//NOTE: Менеджер пулов аффиксов. Загружает data кэширует и даёт API для фильтрации и случайного выбора.
// Поддерживает hot-reload через AddReloadListenerEvent.
*/
public final class AffixPoolDataManager extends SimpleJsonResourceReloadListener {
    public static final String FOLDER = "affixes/pools";
    private static final Gson GSON = new GsonBuilder().setLenient().create();

    private final Map<ResourceLocation, AffixPoolDef> byId = new ConcurrentHashMap<>();
    public static final AffixPoolDataManager INSTANCE = new AffixPoolDataManager();

    private AffixPoolDataManager() { super(GSON, FOLDER); }

    // ---------- Public API ----------

    /** Получить пул по id. */
    public Optional<AffixPoolDef> get(ResourceLocation id) {
        return Optional.ofNullable(byId.get(id));
    }

    /** Все пулы (readonly). */
    public Map<ResourceLocation, AffixPoolDef> all() {
        return Collections.unmodifiableMap(byId);
    }

    /**
     * Вернёт список пулов, удовлетворяющих фильтрам:
     * источник (оружие, броня и т.д.), теги предмета и уровень.
     */
    public List<AffixPoolDef> filterApplicable(Item item, Set<TagKey<Item>> itemTags, Affix.Source src, int itemLevel) {
        final List<AffixPoolDef> result = new ArrayList<>();
        for (AffixPoolDef pool : byId.values()) {
            // фильтр источника
            if (!pool.allowSources().contains(src)) continue;
            // фильтр уровня
            if (!pool.matchesItemLevel(itemLevel)) continue;
            // фильтр тегов (все указанные должны присутствовать)
            if (!itemTags.containsAll(pool.requiredItemTags())) continue;
            result.add(pool);
        }
        return result;
    }

    /**
     * Извлекает взвешенный список всех записей из данного пула.
     * Позволяет затем выбрать случайный аффикс с учётом весов.
     */
    public WeightedRandomList<WeightedEntry.Wrapper<AffixPoolDef.Entry>> buildWeightedEntries(AffixPoolDef pool) {
        final List<WeightedEntry.Wrapper<AffixPoolDef.Entry>> list = new ArrayList<>();
        for (AffixPoolDef.Entry e : pool.entries()) {
            list.add(WeightedEntry.wrap(e, Math.max(1, e.weight())));
        }
        return WeightedRandomList.create(list);
    }

    /**
     * Выбрать случайный аффикс из пула, учитывая веса и фильтр уровня.
     * Возвращает Optional.empty(), если ничего не подошло.
     */
    public Optional<AffixPoolDef.Entry> sample(AffixPoolDef pool, int itemLevel, RandomSource random) {
        var candidates = pool.entries().stream()
                .filter(e -> e.levelOk(itemLevel))
                .toList();
        if (candidates.isEmpty()) return Optional.empty();

        var weighted = WeightedRandomList.create(
                candidates.stream()
                        .map(e -> WeightedEntry.wrap(e, Math.max(1, e.weight())))
                        .toList()
        );
        return weighted.getRandom(random).map(WeightedEntry.Wrapper::data);
    }

    // ---------- Reload pipeline ----------

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsons, ResourceManager manager, ProfilerFiller profiler) {
        profiler.push("grimcore_affix_pools_parse");

        final Map<ResourceLocation, AffixPoolDef> parsed = new HashMap<>();
        final Set<ResourceLocation> duplicateCheck = new HashSet<>();

        for (var e : jsons.entrySet()) {
            final ResourceLocation fileKey = e.getKey();
            var json = e.getValue();

            var result = AffixPoolDef.CODEC.parse(JsonOps.INSTANCE, json).result();
            if (result.isEmpty()) {
                throw new IllegalStateException("Failed to parse AffixPoolDef from " + fileKey);
            }
            var def = result.get();
            var id = def.id();

            if (!duplicateCheck.add(id))
                throw new IllegalStateException("Duplicate pool id: " + id);

            parsed.put(id, def);
        }

        byId.clear();
        byId.putAll(parsed);
        profiler.pop();
    }

    // ---------- Registration ----------

    /** Регистрируем менеджер на AddReloadListenerEvent (bus = MOD). */
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(INSTANCE);
    }
}
