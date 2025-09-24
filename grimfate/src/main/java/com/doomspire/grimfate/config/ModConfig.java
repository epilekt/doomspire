package com.doomspire.grimfate.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * 🔧 ModConfig — централизованный конфиг мода
 * В будущем:
 *  - хранение базовых статов для игроков и мобов
 *  - множители регена/урона
 *  - настройка аффиксов
 *  - редактирование через JSON без пересборки
 */
public class ModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = new File("config/grimfate.json");

    // ===== Пример будущих параметров (пока закомментированы) =====

    // Базовые статы игрока
    // public int basePlayerHealth = 100;
    // public float basePlayerRegen = 0.5f; // 0.5 хп в сек.
    // public int basePlayerMana = 50;
    // public float manaRegenMultiplier = 1.0f;

    // Базовые статы мобов
    // public int baseMobHealth = 50;
    // public float baseMobRegen = 1.0f;

    // Множители урона/защиты
    // public float damageMultiplier = 1.0f;
    // public float defenseMultiplier = 1.0f;

    // Система аффиксов (позже)
    // public float affixDropChance = 0.1f;

    // =============================================================

    private static ModConfig INSTANCE = new ModConfig();

    public static ModConfig get() {
        return INSTANCE;
    }

    /** Загружаем конфиг из JSON */
    public static void load() {
        if (!CONFIG_FILE.exists()) {
            save(); // если файла нет, создаём дефолт
            return;
        }

        try (FileReader reader = new FileReader(CONFIG_FILE)) {
            INSTANCE = GSON.fromJson(reader, ModConfig.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Сохраняем конфиг в JSON */
    public static void save() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(INSTANCE, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

