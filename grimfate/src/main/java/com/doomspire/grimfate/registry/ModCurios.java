package com.doomspire.grimfate.registry;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;
import net.neoforged.fml.ModList;

//NOTE: Мост к Curios: проверка наличия, константы слотов, потенциальные хелперы
/**
 * Слоты в Curios 9.5.x регистрируются через data/curios/... (см. ресурсы нашего мода).
 */
public final class ModCurios {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final String SLOT_RING = "ring";
    public static final String SLOT_NECKLACE = "necklace";
    public static final String SLOT_BELT = "belt";

    private ModCurios() {}

    /** true, если Curios присутствует. */
    public static boolean isLoaded() {
        return ModList.get().isLoaded("curios");
    }

    /** Позвать из commonSetup для логов и ранней диагностики. */
    public static void init() {
        if (isLoaded()) {
            LOGGER.info("[Grimfate] Curios detected. Slot types are provided via data files (curios/slots).");
        } else {
            LOGGER.warn("[Grimfate] Curios NOT detected. Jewelry slots will be unavailable.");
        }
    }
}

