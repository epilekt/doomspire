package com.doomspire.grimcore.affix.def;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/*
//NOTE: Менеджер дефиниций аффиксов. Грузит data, валидирует, даёт быстрый доступ по id. Поддерживает hot-reload.
 *
 * Data-driven загрузчик AffixDef. Папка: data/<ns>/affixes/defs/*.json
 * Регистрируется на MOD bus через AddReloadListenerEvent (см. onAddReloadListeners).
 */
public final class AffixDataManager extends SimpleJsonResourceReloadListener {
    public static final String FOLDER = "affixes/defs";
    private static final Gson GSON = new GsonBuilder().setLenient().create();

    private final Map<ResourceLocation, AffixDef> byId = new ConcurrentHashMap<>();
    public static final AffixDataManager INSTANCE = new AffixDataManager();

    private AffixDataManager() {
        super(GSON, FOLDER);
    }

    // ---------- Public API ----------

    /** Получить определение аффикса по id. */
    public Optional<AffixDef> get(ResourceLocation id) {
        return Optional.ofNullable(byId.get(id));
    }

    /** Признак наличия аффикса. */
    public boolean contains(ResourceLocation id) {
        return byId.containsKey(id);
    }

    /** Все аффиксы (неизменяемая копия). */
    public Map<ResourceLocation, AffixDef> all() {
        return Collections.unmodifiableMap(byId);
    }

    // ---------- Reload pipeline ----------

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsons,
                         ResourceManager resourceManager,
                         ProfilerFiller profiler) {
        profiler.push("grimcore_affix_defs_parse");

        final Map<ResourceLocation, AffixDef> parsed = new HashMap<>();
        final Set<ResourceLocation> duplicateCheck = new HashSet<>();

        for (Map.Entry<ResourceLocation, JsonElement> e : jsons.entrySet()) {
            final ResourceLocation fileKey = e.getKey();
            final var json = e.getValue();

            final var result = AffixDef.CODEC.parse(JsonOps.INSTANCE, json).result();
            if (result.isEmpty()) {
                throw new IllegalStateException("Failed to parse AffixDef from " + fileKey);
            }

            final AffixDef def = result.get();
            final ResourceLocation id = def.id();

            if (!duplicateCheck.add(id)) {
                throw new IllegalStateException("Duplicate affix id detected: " + id + " (from " + fileKey + ")");
            }

            // Доп. валидации связей (минимальные, остальное — на этапе роллинга/агрегации):
            if (def.min() == def.max() && def.unit() == AffixDef.ValueUnit.PERCENT && def.min() == 0f) {
                // «пустой» процентный аффикс — скорее всего ошибка данных
                throw new IllegalStateException("Suspicious zero-percent affix: " + id + " in " + fileKey);
            }

            parsed.put(id, def);
        }

        byId.clear();
        byId.putAll(parsed);

        profiler.pop();
    }

    // ---------- Registration ----------

    /** Регистрируем менеджер как ресурс-лоадер (Bus.MOD). Вызывать один раз при старте. */
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(INSTANCE);
    }
}
