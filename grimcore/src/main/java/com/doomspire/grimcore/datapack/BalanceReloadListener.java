package com.doomspire.grimcore.datapack;

import com.doomspire.grimcore.datapack.codec.AttributesBalance;
import com.doomspire.grimcore.datapack.codec.LevelsCurve;
import com.doomspire.grimcore.datapack.codec.SpellTuning;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Читает JSON из:
 *  - data/grimfate/balance/levels.json
 *  - data/grimfate/balance/attributes.json
 *  - data/grimfate/balance/spells.json
 * и применяет в BalanceData.
 *
 * 1.21.1 / NeoForge: регистрируемся через AddReloadListenerEvent.
 */
public final class BalanceReloadListener extends SimplePreparableReloadListener<BalanceReloadListener.Data> {
    private static final Logger LOG = LogUtils.getLogger();

    /** Снэпшот подготовленных данных. */
    public record Data(LevelsCurve levels, AttributesBalance attrs, SpellTuning spells) {}

    /** Регистрация серверного reload-listener’а. */
    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent e) {
        e.addListener(new BalanceReloadListener());
    }

    @Override
    protected Data prepare(ResourceManager rm, ProfilerFiller profiler) {
        var levels = readJson(rm, "grimfate", "balance/levels.json", LevelsCurve.CODEC);
        var attrs  = readJson(rm, "grimfate", "balance/attributes.json", AttributesBalance.CODEC);
        var spells = readJson(rm, "grimfate", "balance/spells.json", SpellTuning.CODEC);
        com.doomspire.grimcore.datapack.Balance.set(levels, attrs, spells);
        return new Data(levels, attrs, spells);
    }

    @Override
    protected void apply(Data data, ResourceManager rm, ProfilerFiller profiler) {
        BalanceData.apply(
                data.levels() != null ? data.levels() : LevelsCurve.defaults(),
                data.attrs()  != null ? data.attrs()  : AttributesBalance.defaults(),
                data.spells() != null ? data.spells() : SpellTuning.defaults()
        );
        LOG.info("[Grim] BalanceReloadListener applied.");
    }

    // ---------- helpers ----------

    private static <T> T readJson(ResourceManager rm, String namespace, String path, Codec<T> codec) {
        try {
            ResourceLocation rl = ResourceLocation.fromNamespaceAndPath(namespace, path);
            var opt = rm.getResource(rl);
            if (opt.isEmpty()) return null;

            try (var in = opt.get().open();
                 var br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                var json = com.google.gson.JsonParser.parseReader(br);
                var parsed = codec.parse(JsonOps.INSTANCE, json);
                return parsed.result().orElse(null);
            }
        } catch (Exception ex) {
            LOG.error("[Grim] Failed to read json {}/{}: {}", namespace, path, ex.toString());
            return null;
        }
    }
}
