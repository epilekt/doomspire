package com.doomspire.grimcore.attach;

import com.doomspire.grimcore.stat.ModAttachments; // ← если пакет другой, поправь импорт
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

/**
 * Централизованный сервис угрозы без тик-затухания:
 *  - конвертация нанесённого урона в БАЗОВУЮ угрозу,
 *  - добавление постоянной (base) угрозы,
 *  - добавление временной (temp) угрозы с авто-сбросом по таймеру,
 *  - чтение total/top.
 *
 * Событий/подписок тут нет — только прямые вызовы из боёвки/спеллов.
 */
public final class ThreatService {
    private ThreatService() {}

    /** Множитель перевода нанесённого урона в угрозу (баланс). */
    public static final float THREAT_PER_DAMAGE = 1.0f;

    /** Доп. множитель из статов (когда будет нужный стат — подключим здесь). */
    public static float threatGenMultiplier(Player source) {
        return 1.0f;
    }

    /** Урон → БАЗОВАЯ угроза. */
    public static void addThreatFromDamage(LivingEntity victim, Player attacker, float damage) {
        if (!(victim.level() instanceof ServerLevel) || attacker == null || damage <= 0f) return;
        MobThreatAttachment att = victim.getData(ModAttachments.MOB_THREAT.get());
        if (att == null) return;

        float mult = THREAT_PER_DAMAGE * threatGenMultiplier(attacker);
        att.addBaseThreat(attacker.getUUID(), damage * mult);
    }

    /** Постоянная (base) прибавка угрозы. */
    public static void addThreatFlat(LivingEntity victim, UUID playerId, float amount) {
        if (!(victim.level() instanceof ServerLevel) || playerId == null || amount <= 0f) return;
        MobThreatAttachment att = victim.getData(ModAttachments.MOB_THREAT.get());
        if (att != null) {
            att.addBaseThreat(playerId, amount);
        }
    }

    /** Временная (temp) угроза: исчезает через durationMs. */
    public static void addTempThreat(LivingEntity victim, UUID playerId, float amount, long durationMs) {
        if (!(victim.level() instanceof ServerLevel) || playerId == null || amount <= 0f || durationMs <= 0L) return;
        MobThreatAttachment att = victim.getData(ModAttachments.MOB_THREAT.get());
        if (att != null) {
            att.addTempThreat(playerId, amount, durationMs);
        }
    }

    /** Текущее total-значение угрозы по игроку. */
    public static float getThreat(LivingEntity victim, UUID playerId) {
        MobThreatAttachment att = victim.getData(ModAttachments.MOB_THREAT.get());
        return (att != null) ? att.totalThreatFor(playerId, System.currentTimeMillis()) : 0f;
    }

    /** Игрок с наибольшей актуальной угрозой. */
    public static UUID topThreatPlayer(LivingEntity victim) {
        MobThreatAttachment att = victim.getData(ModAttachments.MOB_THREAT.get());
        return (att != null) ? att.topThreatPlayer(System.currentTimeMillis()) : null;
    }
}
