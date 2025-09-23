package com.doomspire.grimcore.datapack;

import com.doomspire.grimcore.datapack.codec.AttributesBalance;
import com.doomspire.grimcore.datapack.codec.LevelsCurve;
import com.doomspire.grimcore.datapack.codec.SpellTuning;
import com.doomspire.grimcore.stat.Attributes;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.util.EnumMap;
import java.util.Map;

/**
 * Глобальный кэш загруженных из datapack'ов балансовых таблиц.
 * Потокобезопасность: доступ только с сервера-треда при reload/старте.
 */
public final class BalanceData {
    private static final Logger LOG = LogUtils.getLogger();

    // Текущие активные значения
    private static volatile LevelsCurve levelsCurve = LevelsCurve.defaults();
    private static volatile AttributesBalance attributes = AttributesBalance.defaults();
    private static volatile SpellTuning spellTuning = SpellTuning.defaults();

    private BalanceData() {}

    public static LevelsCurve levels() { return levelsCurve; }
    public static AttributesBalance attrs() { return attributes; }
    public static SpellTuning spells() { return spellTuning; }

    /** Вызывается из BalanceReloadListener после успешного парсинга JSON. */
    static void apply(LevelsCurve lv, AttributesBalance ab, SpellTuning st) {
        if (lv != null) levelsCurve = lv;
        if (ab != null) attributes = ab;
        if (st != null) spellTuning = st;
        LOG.info("[Grim] Balance data applied: levels={}, attrs={}, spells={}",
                levelsCurve.summary(), attributes.summary(), spellTuning.summary());
    }
}

