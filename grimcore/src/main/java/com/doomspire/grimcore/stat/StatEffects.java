package com.doomspire.grimcore.stat;

import com.doomspire.grimcore.attach.PlayerStatsAttachment;
import com.doomspire.grimcore.stat.ModAttachments;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * Применяет эффекты из статов к ванильным атрибутам игрока.
 * ЕДИНАЯ точка – тут же и снимаем/обновляем модификаторы.
 */
public final class StatEffects {
    private StatEffects() {}

    // Идентификаторы наших модификаторов (ResourceLocation в 1.21.1)
    public static final ResourceLocation MOVE_SPEED_ID =
            ResourceLocation.fromNamespaceAndPath("grimcore", "dex_move_speed");

    /**
     * Применяет все эффекты заново на основе текущего снапшота статов.
     * Вызывать после любого изменения статов/экипировки и при логине/респауне.
     */
    public static void applyAll(ServerPlayer sp) {
        PlayerStatsAttachment att = sp.getData(ModAttachments.PLAYER_STATS.get());
        if (att == null) return;

        var snap = att.getSnapshot();

        // 1) Ловкость → скорость передвижения: +0.25% за 1 DEX
        applyMoveSpeed(sp, snap.moveSpeedPct);
        // 2) здесь же позже: сила → урон ближнего, интеллект → сила магии и т.п.
    }

    private static void applyMoveSpeed(ServerPlayer sp, double percent) {
        AttributeInstance inst = sp.getAttribute(Attributes.MOVEMENT_SPEED);
        if (inst == null) return;

        // снимаем старый наш модификатор
        inst.removeModifier(MOVE_SPEED_ID);

        // +X% к ИТОГОВОЙ скорости → ADD_MULTIPLIED_TOTAL с долей (0.075 для +7.5%)
        double addTotal = percent / 100.0;
        if (addTotal == 0.0) return;

        AttributeModifier mod = new AttributeModifier(
                MOVE_SPEED_ID,
                addTotal,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        );
        inst.addPermanentModifier(mod);
    }
}
