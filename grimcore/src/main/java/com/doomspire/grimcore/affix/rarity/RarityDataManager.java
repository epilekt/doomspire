package com.doomspire.grimcore.affix.rarity;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.random.WeightedRandomList;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 //NOTE: Менеджер редкостей (Rarity) — грузит RarityDef из datapack, валидирует и даёт API выбора по весам.
 *
 * Data-driven загрузчик RarityDef с поддержкой hot-reload.
 * Путь в датапаке: data/<namespace>/affixes/rarities/*.json
 *
 * Требуется регистрация на событие {@link AddReloadListenerEvent} (см. метод onAddReloadListeners).
 */
public final class RarityDataManager extends SimpleJsonResourceReloadListener {
    public static final String FOLDER = "affixes/rarities";
    private static final Gson GSON = new GsonBuilder().setLenient().create();

    // По id редкости (из поля "id" в JSON)
    private final Map<ResourceLocation, RarityDef> byId = new ConcurrentHashMap<>();

    // Предсобранный взвешенный список для быстрого выбора
    private volatile WeightedRandomList<WeightedEntry.Wrapper<ResourceLocation>> weighted = WeightedRandomList.create();

    // Синглтон-экземпляр менеджера (ядро grimcore, без знания о grimfate)
    public static final RarityDataManager INSTANCE = new RarityDataManager();

    private RarityDataManager() {
        super(GSON, FOLDER);
    }

    // ---------- Public API ----------

    /** Вернёт определение редкости по её логическому id. */
    public Optional<RarityDef> get(ResourceLocation id) {
        return Optional.ofNullable(byId.get(id));
    }

    /** Все редкости (иммутабельная копия). */
    public Map<ResourceLocation, RarityDef> all() {
        return Collections.unmodifiableMap(byId);
    }

    /**
     * Выбор случайной редкости по весам. Если список пуст — Optional.empty().
     * NB: возвращает именно id, чтобы потребитель мог повторно достать актуальное определение.
     */
    public Optional<ResourceLocation> sampleId(RandomSource random) {
        return weighted.getRandom(random).map(WeightedEntry.Wrapper::data);
    }

    /** Удобство: сразу вернуть сам RarityDef, если найден. */
    public Optional<RarityDef> sample(RandomSource random) {
        return sampleId(random).flatMap(this::get);
    }

    // ---------- Reload pipeline ----------

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsons,
                         ResourceManager resourceManager,
                         ProfilerFiller profiler) {
        profiler.push("grimcore_rarities_parse");

        final Map<ResourceLocation, RarityDef> parsed = new HashMap<>();
        final Set<ResourceLocation> duplicateCheck = new HashSet<>();
        final List<Pair<ResourceLocation, Integer>> weights = new ArrayList<>();

        for (Map.Entry<ResourceLocation, JsonElement> e : jsons.entrySet()) {
            final ResourceLocation fileKey = e.getKey(); // data/<ns>/<FOLDER>/<path>.json
            final var json = e.getValue();

            final var result = RarityDef.CODEC.parse(JsonOps.INSTANCE, json).result();
            if (result.isEmpty()) {
                // Жёсткая диагностика: указываем файл, чтобы быстро найти ошибку
                throw new IllegalStateException("Failed to parse RarityDef from " + fileKey + " (see logs for details)");
            }

            final RarityDef def = result.get();
            final ResourceLocation id = def.id();

            // Валидация уникальности id между файлами
            if (!duplicateCheck.add(id)) {
                throw new IllegalStateException("Duplicate rarity id detected: " + id + " (from " + fileKey + ")");
            }

            parsed.put(id, def);
            weights.add(Pair.of(id, Math.max(0, def.weight())));
        }

        // Сборка взвешенного списка
        final List<WeightedEntry.Wrapper<ResourceLocation>> wrappers = new ArrayList<>(weights.size());
        for (Pair<ResourceLocation, Integer> p : weights) {
            final int w = p.getSecond();
            if (w > 0) {
                wrappers.add(WeightedEntry.wrap(p.getFirst(), w));
            }
        }

        // Публикуем атомарно
        byId.clear();
        byId.putAll(parsed);
        weighted = wrappers.isEmpty() ? WeightedRandomList.create() : WeightedRandomList.create(wrappers);

        profiler.pop();
    }

    // ---------- Registration ----------

    /**
     * Зарегистрировать менеджер как ресурс-лоадер. Вызвать один раз на старте (common-setup),
     * например из grimcore Main или из grimfate, если у тебя там центральная точка инициализации ядра.
     */
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(INSTANCE);
    }
}
