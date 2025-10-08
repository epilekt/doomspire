package com.doomspire.grimfate.debug;

import com.doomspire.grimfate.core.Grimfate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public final class DebugLootModsProbe implements PreparableReloadListener {
    public static void onAddReloadListeners(AddReloadListenerEvent e) {
        e.addListener(new DebugLootModsProbe());
    }

    @Override
    public CompletableFuture<Void> reload(PreparationBarrier barrier, ResourceManager rm,
                                          ProfilerFiller prep, ProfilerFiller apply,
                                          Executor bg, Executor game) {
        return CompletableFuture
                .supplyAsync(() -> {
                    // 1) Проверяем наличие global_loot_modifiers.json
                    ResourceLocation rl = ResourceLocation.fromNamespaceAndPath(
                            "neoforge", "loot_modifiers/global_loot_modifiers.json");
                    boolean exists = rm.getResource(rl).isPresent();
                    Grimfate.LOGGER.info("[Grimfate][GLM-PROBE] global_loot_modifiers present: {}", exists);

                    // 2) Листинг всего, что найдено под neoforge/loot_modifiers
                    rm.listResources("loot_modifiers", r -> r.getNamespace().equals("neoforge"))
                            .forEach((res, _resObj) ->
                                    Grimfate.LOGGER.info("[Grimfate][GLM-PROBE] found: {}", res));

                    return null;
                }, bg)
                .thenCompose(barrier::wait)
                .thenAcceptAsync(v -> {}, game);
    }
}
