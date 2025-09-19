package com.doomspire.grimcore.stats;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * Логика работы с прогрессом игрока.
 * Здесь: выдача опыта, автолевелап, логирование.
 */
public class PlayerProgressHandler {

    public static void grantXp(ServerPlayer player, int xp) {
        PlayerProgress progress = player.getData(ModAttachments.PLAYER_PROGRESS);
        if (progress == null) return;

        int newXp = progress.exp() + xp;
        int newLevel = progress.level();
        int newCap = progress.expCap();

        boolean leveledUp = false;

        // Проверяем ап уровня
        while (newXp >= newCap) {
            newXp -= newCap;
            newLevel++;
            newCap = xpToNextLevel(newLevel);
            leveledUp = true;
        }

        // Обновляем прогресс
        PlayerProgress newProgress = new PlayerProgress(newLevel, newXp, newCap);
        player.setData(ModAttachments.PLAYER_PROGRESS, newProgress);

        // Если уровень апнулся → пересчитываем статы
        if (leveledUp) {
            PlayerStats oldStats = PlayerStatsProvider.get(player);
            if (oldStats == null) oldStats = PlayerStats.DEFAULT;

            PlayerStats scaled = LevelScalingSystem.scaledStatsForLevel(newLevel);

            // сохраняем долю текущих HP/MP при апе
            int newHealth = Math.min(
                    (int) ((oldStats.health() / (float) oldStats.maxHealth()) * scaled.maxHealth()),
                    scaled.maxHealth()
            );
            int newMana = Math.min(
                    (int) ((oldStats.mana() / (float) oldStats.maxMana()) * scaled.maxMana()),
                    scaled.maxMana()
            );

            PlayerStats adjusted = new PlayerStats(
                    newHealth,
                    newMana,
                    scaled.maxHealth(),
                    scaled.maxMana(),
                    scaled.healthRegen(),
                    scaled.manaRegen()
            );

            PlayerStatsProvider.set(player, adjusted);
            PlayerStatsProvider.clearMutableCache(player);
            PlayerStatsProvider.getMutable(player);
            PlayerStatsProvider.commitIfDirty(player);
        }
    }



    private static int xpToNextLevel(int currentLevel) {
        // твоя формула опыта на уровень
        return 100 + (currentLevel * 20);
    }

    /**
     * Заготовка под HUD отрисовку (вызывается в CustomHudOverlay).
     */
    public static String getHudText(Player player) {
        PlayerProgress progress = player.getData(ModAttachments.PLAYER_PROGRESS);
        return progress != null ? progress.hudString() : "Lvl ? (0/0)";
    }
}


