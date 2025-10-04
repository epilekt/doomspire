
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

### Фаза A (контент-заготовки) ✅

### [Фаза B (механики)](docs/PHASE_PLAN.md) ↗️

* Первые реальные спеллы (Firebolt, Heal, ChainShock, IceLance, VenomDart, OreSense)
* Skill Tree Screen
* Первые узлы в древе прокачки
* Баланс №0 (json, XP, лут)
* Привязка игрока к классу в древе навыков
* Сеты брони и оружия (пока с ванильными рецептами)
* [Система аффиксов/тиров редкости](docs/AFFIX_SYSTEM.md)

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
  grimcore/                        # Ядро: баланс, статы, датапаки, сеть
    attach/                        # Персистентные и рантайм-данные сущностей/игрока
      MobStatCalculator.java       # Пересчёт статов мобов в снапшот (HP/резисты/уклон и т.п.). :contentReference[oaicite:0]{index=0}
      MobStatsAttachment.java      # Attachment моба: атрибуты, текущее HP, STREAM_CODEC/кодеки, снапшот. :contentReference[oaicite:1]{index=1}
      PlayerLoadoutAttachment.java # Слот-бар спеллов: RL слотов + их КД, CODEC и STREAM_CODEC. :contentReference[oaicite:2]{index=2}
      PlayerProgressAttachment.java# Прогресс игрока (уровень/опыт/кап), кодеки; старт: lvl=1. :contentReference[oaicite:3]{index=3}
      # (есть PlayerStatsAttachment/StatSnapshot/ModAttachments и др. в проекте ядра — используются сетью/ивентами) :contentReference[oaicite:4]{index=4}

    datapack/                      # Баланс и его загрузка из data-паков
      Balance.java                 # Глобальный доступ к текущему балансу (levels/attrs/spells). :contentReference[oaicite:5]{index=5}
      BalanceData.java             # Применение новых значений после reload + лог summary. :contentReference[oaicite:6]{index=6}
      BalanceReloadListener.java   # (упоминается в использовании) перезагрузка баланса из JSON. :contentReference[oaicite:7]{index=7}
      codec/
        AttributesBalance.java     # Кодек баланса атрибутов (присутствует в сводке Balance). :contentReference[oaicite:8]{index=8}
        LevelsCurve.java           # Кодек кривых уровней/опыта (присутствует в сводке Balance). :contentReference[oaicite:9]{index=9}
        SpellTuning.java           # Кодек тюнинга спеллов: base_cost/base_cooldown/scaling/огр.оружия. :contentReference[oaicite:10]{index=10}

    events/
      CoreDamageEvents.java        # Обработка урона/событий, синк статов, подписки на шину. :contentReference[oaicite:11]{index=11}

    net/
      GrimcoreNetworking.java      # Синхронизация аттачментов ядра клиент↔сервер. :contentReference[oaicite:12]{index=12}
      ProgressNetworking.java      # Синхронизация прогресса/слотов (используется в Attachments). :contentReference[oaicite:13]{index=13}
      S2C_SyncStats.java           # Пакет синхронизации статов (используется в событиях ядра). :contentReference[oaicite:14]{index=14}

    stat/
      Attributes.java              # Перечень базовых атрибутов (STR/INT/…); используются везде. :contentReference[oaicite:15]{index=15}
      DamageTypes.java             # Типы урона ядра (phys/fire/frost/…); применяются в бою. :contentReference[oaicite:16]{index=16}
      ModAttachments.java          # Регистрация/ключи attachments (PLAYER_STATS и др.). :contentReference[oaicite:17]{index=17}
      PlayerProgress.java          # Лёгкий снапшот прогресса (уровень/exp) для клиента. :contentReference[oaicite:18]{index=18}
      ResistTypes.java             # Набор резистов (POISON/FIRE/…); используется в калькуляторах. :contentReference[oaicite:19]{index=19}
      StatCalculator.java          # Калькулятор статов игрока (ссылки в аттачах/ивентах). :contentReference[oaicite:20]{index=20}
      StatSnapshot.java            # Снимок статов игрока/моба для быстрого доступа. :contentReference[oaicite:21]{index=21}

    xp/
      LevelTable.java              # Таблица опыта по уровням (исп. в PlayerProgressAttachment). :contentReference[oaicite:22]{index=22}
      Rewards.java                 # Награды за уровень (упоминается в прогрессе/ивентах). :contentReference[oaicite:23]{index=23}

  grimfate/                        # Контент и клиент (Neoforge 1.21.1)
    core/
      Grimfate.java                # Главный класс мода; регистрация ивентов/ресурсов. :contentReference[oaicite:24]{index=24}
      GrimfateClient.java          # Клиентская часть: рендеры, keybind’ы, клиентские слушатели. :contentReference[oaicite:25]{index=25}

    client/
      Hotkeys.java                 # Горячие клавиши + client tick listener (подписка из Client). :contentReference[oaicite:26]{index=26}
      # (HudOverlay/CustomHudOverlay/StatsHubScreen встречались в ранних дампах; в свежем — ключевой Hotkeys)

    entity/
      BoltProjectileEntity.java    # Снаряд «болт»: AIR как default item, no-gravity, частицы/урон. :contentReference[oaicite:27]{index=27}

    events/
      StaffAttackEvents.java       # Клиентские/игровые хендлеры для посохов (ЛКМ/ПКМ логика). :contentReference[oaicite:28]{index=28}

    item/
      StaffItem.java               # Посох: удержание ПКМ → отправка C2S запроса авто-болта. :contentReference[oaicite:29]{index=29}
      comp/
        AffixListComponent.java    # Компонент предмета: список аффиксов (для будущей системы). :contentReference[oaicite:30]{index=30}
        WeaponProfileComponent.java# Компонент оружия: тип/двуручность/модификаторы/качество. :contentReference[oaicite:31]{index=31}

    loot/
      ModLootModifiers.java        # Регистрация глобальных лут-модификаторов. :contentReference[oaicite:32]{index=32}
      RustyRingDropModifier.java   # Пример дропа кольца из сундуков с компонентом +1 SPIRIT. :contentReference[oaicite:33]{index=33}

    network/
      AutoBoltServer.java          # (устар. заглушка) ранняя серверная логика авто-болта; заменена новой схемой. :contentReference[oaicite:34]{index=34}
      ModNetworking.java           # Регистрация payload’ов (в т.ч. C2SCastAutoBoltPayload) и их хендлеров. :contentReference[oaicite:35]{index=35}
      SpellCastClient.java         # Клиентская отправка C2S пакетов для кастов (из хоткеев/итемов). :contentReference[oaicite:36]{index=36}
      payload/
        C2SAllocatePointPayload.java   # C2S: выделение очка навыка/атрибута. :contentReference[oaicite:37]{index=37}
        S2CAllocateResultPayload.java  # S2C: подтверждение/результат аллокации. :contentReference[oaicite:38]{index=38}
        C2SCastSpellSlotPayload.java   # C2S: каст по слоту лоадаута. :contentReference[oaicite:39]{index=39}
        C2SCastAutoBoltPayload.java    # C2S: запрос авто-болта посохом c указанием руки. :contentReference[oaicite:40]{index=40}
        C2SCastStaffBoltPayload.java   # (наследие) ранний вариант C2S для болта, не используется. :contentReference[oaicite:41]{index=41}

    registry/
      ModDataComponents.java       # Регистрация data components предметов (WEAPON_PROFILE и др.). :contentReference[oaicite:42]{index=42}
      ModEntityTypes.java          # Регистрация сущностей (Bolt и т.п.) + клиентский рендер. :contentReference[oaicite:43]{index=43}
      ModItems.java                # Регистрация предметов (мечи/посохи/луки/кольцо) с компонентами. :contentReference[oaicite:44]{index=44}
      ModItemTags.java             # Теги предметов (STAFF/two_handed и пр.) для предикатов оружия. :contentReference[oaicite:45]{index=45}

    # ресурсы (важные для понимания структуры)
    resources/assets/grimfate/
      models/item/bolt.json        # Модель временного «болта» (привязана к ThrownItemRenderer). :contentReference[oaicite:46]{index=46}
      models/item/rusty_ring.json  # Модель кольца. :contentReference[oaicite:47]{index=47}
      models/item/staff.json       # Модель посоха. :contentReference[oaicite:48]{index=48}
      lang/{en_us,ru_ru}.json      # Локализации. :contentReference[oaicite:49]{index=49}

    resources/data/grimfate/balance/
      attributes.json              # Баланс атрибутов (ядро читает через BalanceReloadListener). :contentReference[oaicite:50]{index=50}
      levels.json                  # Кривые уровней. :contentReference[oaicite:51]{index=51}
      spells.json                  # Тюнинг спеллов; auto_bolt_staff с base_cost/base_cooldown/scaling. :contentReference[oaicite:52]{index=52}

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
* Ловкость → дальний физ. урон, скорость движения
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
* Keybinds: каст хотбар-слота (1–6)

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
