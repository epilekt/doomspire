
# Doomspire: Grimfate — План и Архитектура (NeoForge 1.21.1) Combat Expansion

## ⚔️ Основная концепция. 

Мод не заменяет Minecraft, а дополняет его RPG-слоем:

* Игрок прокачивает персонажа, исследует мир, занимается ремёслами и строительством.
* Основные оси прогресса: **атрибуты**, **древа навыков**, **экипировка**, **заклинания**, **профессии**.
* Враги (мобы/боссы) тоже имеют кастомные статы.
* Ванильные эффекты и механики переопределяются или игнорируются.

---
## 🛠️ Дорожная карта

### Фаза 0 (ядро) ✅

* StatCalculator, DamageEngine, Attachments 🟩
* Data Components (stat\_bonus, block\_bonus, restrictions) 🟩
* Конфиги и JSON-баланс (атрибуты, XP, спеллы) 🟩
* `/grimfate exp give` 🟩

### Фаза A (контент-заготовки) ✅

* Базовые предметы (меч, щит, кольцо) 🟩
* Рецепты и лут-инъекции 🟩
* Папки заклинаний + JSON-шаблоны 🟩
* GUI Stats Hub (атрибуты) 🟩
* Keybinds 🟩

### Фаза B (механики)

* Первые реальные спеллы (Firebolt, Heal, ChainShock, IceLance, VenomDart, OreSense)
* Skill Tree Screen
* Первые узлы в древе прокачки
* Баланс №0 (json, XP, лут)
* Привязка игрока к классу в древе навыков
* Сеты брони и оружия (пока с ванильными рецептами)

### Фаза C (интеграции)

* Curios API (слоты, предметы, интеграция)
* GeckoLib (GeoProjectile, GeoMob, GeoArmor)
* Первые мобы с новыми аттрибутами

### Фаза D (контент)

* Расширенные деревья навыков
* Новый набор предметов
* Полубоссы/боссы с анимацией
* Новые профессии (алхимия, кузнец, кулинария)
* Генерация мира (руды, растения, структуры, рейды)

---

## 🏗️ Архитектура проекта

Проект состоит из двух модулей: `grimcore` (ядро) и `grimfate` (контент).

### Дерево проекта

```
src/main/java/com/doomspire/
  grimcore/                        # Ядро: серверная логика, сеть, данные
    attach/                        # Attachments игрока/мобов (статы, прогресс, лоадаут)
      MobStatCalculator.java
      MobStatsAttachment.java
      PlayerLoadoutAttachment.java
      PlayerProgressAttachment.java
      PlayerStatsAttachment.java
    combat/                        # Пайплайн урона и экология
      DamageContext.java
      DamageEngine.java
      EnvironmentalDamage.java
    commands/                      # Команды (выдача опыта и др.)
      GrimfateCommands.java
    config/
      CoreCommonConfig.java
    datapack/                      # Слушатели перезагрузки и кодеки баланса (заглушки готовы)
      BalanceReloadListener.java
      codec/
        AttributesBalance.java
        LevelsCurve.java
        SpellTuning.java
    events/                        # Игровые события
      CoreDamageEvents.java
      CorePlayerEvents.java
      MobSpawnInit.java
      MobTuning.java
      RegenTicker.java
      XpEvents.java
    net/                           # Синхронизация клиент↔сервер
      GrimcoreNetworking.java
      ProgressNetworking.java
      S2C_SyncStats.java
    runtime/                       # Невалидируемые рантайм-данные (сервер)
      PlayerRuntimeData.java
      PlayerRuntimeManager.java
    stat/                          # Атрибуты, урон, резисты, калькулятор статов
      Attributes.java
      DamageTypes.java
      MobStatsProvider.java
      ModAttachments.java
      PlayerProgress.java
      ResistTypes.java
      StatCalculator.java
      StatSnapshot.java
    xp/                            # Кривые уровней/награды
      LevelTable.java
      Rewards.java

  grimfate/                        # Контент и клиент
    client/
      ClientEvents.java
      CustomHudOverlay.java
      HudOverlay.java
      gui/
        StatsHubScreen.java
      input/
        Keybinds.java
      KeyBindings.java
    config/
      ClientConfig.java
      ModConfig.java
    core/
      Config.java
      Grimfate.java
      GrimfateClient.java
    network/
      ModNetworking.java
    spell/
      FireboltEntity.java          # Заглушка под контент спеллов (продолжим на фазе 1)

```

---

## 📊 Система статов

### Базовые характеристики

* Здоровье, мана + реген
* Урон: PHYS\_MELEE, PHYS\_RANGED, FIRE, FROST, LIGHTNING, POISON
* Резисты: PHYS, FIRE, FROST, LIGHTNING, POISON
* Крит. шанс и крит. урон
* Вампиризм, манастил
* Уклонение, блок (через щит)
* Скорости (движение/атака)

### Атрибуты

* Стойкость → здоровье, реген, блок
* Сила → ближний физ. урон
* Интеллект → стихийный урон
* Дух → мана, реген маны
* Ловкость → дальний физ. урон
* Уклонение → шанс избежать урона

### Порядок расчёта урона

1. Уклонение цели
2. Базовый урон × атрибуты
3. Критическая проверка
4. Резисты
5. Блок (щит)
6. Пост-эффекты (вампиризм, манастил, статусы)

---

## 🌳 Классы и древа умений

* **Воин** → Рыцарь (танк), Берсерк (урон)
* **Маг** → Священник (хил+молния), Чародей (огонь+лёд)
* **Вор** → Охотник (дальний бой), Убийца (яд/ближний бой)
* **Профессии** → Горняк, Рыбак, Фермер

Боевые ветки: \~50 узлов каждая, ремесленные: \~10 узлов.

---

## 🪄 Заклинания

* 15–25 умений на класс
* 5–6 активных в хотбаре
* JSON-параметры: стоимость, кд, шанс/длительность статуса, allowed\_classes
* Ремесленные заклинания в `miner/`, `fisherman/`, `farmer/`

---

## 🧩 Экипировка

* Оружие, броня, бижутерия (Curios API), реликвии
* Data Components для бонусов (`stat_bonus`, `block_bonus`, `class_restriction`)
* Рецепты через ванильный верстак
* Лут через GLM/loot tables
* Редкость через свойства предметов

---

## ⚒ Ремёсла

* Горное дело, фермерство, рыбалка, алхимия, кузнечество, готовка, зачарование
* Узлы и заклинания ремесленников

---

## 🎨 Визуал

* GeckoLib для мобов, снарядов спеллов, 3D-брони
* Шаблоны: GeoProjectileEntity, GeoMobEntity, GeoArmorItem

---

## 🔗 Интеграции

* **Curios API**: кольца, амулеты, пояса
* **FTB Quests**: выдача опыта через `/grimfate exp give`

---

## 🎮 Интерфейс

* Вкладка «RPG» в инвентаре
* Stats Hub (атрибуты, резисты, урон)
* Skill Tree (боевые/ремесленные ветки)
* Spell Loadout (хотбар заклинаний)
* Keybinds: открыть хаб (H), каст хотбар-слота (1–6)

---

## 🎯 Принципы

* Храним данные в attachments и data components → минимум NBT
* Баланс через JSON/datapack → без ребилдов
* DamageEngine и StatAggregator — единые точки входа
* Классы/школы/профессии разделены по папкам
* GeckoLib и Curios — тонкие интеграции, без вмешательства в ядро

Additional Resources: 
==========
Community Documentation: https://docs.neoforged.net/  
NeoForged Discord: https://discord.neoforged.net/
