# Пайплайн расчёта урона (с уклоном в начале)

**Порядок слоёв:**  
1) **Evade/Dodge** → 2) Сбор урона атакера *(Base + Flat → Add% → Mult% → Attribute Scale)* → 3) **Крит** → 4) **Броня/Блок (только PHYS)** → 5) **Резисты по типам** → 6) Сумма типов → 7) Кламп/округление → 8) Пост-эффекты *(leech/on-hit)*.

---

## 0) Уклон (ранний выход)

Бросок на сервере, один раз на хит:

$$
I_{\text{evade}}=
\begin{cases}
0,& \text{если } \mathrm{rand}() < p_{\text{evade}}\\
1,& \text{иначе}
\end{cases}
$$

Если $I_{\text{evade}}=0$ → хит «уклонён» (остальные слои не считаем; исключения/стоимости опишешь отдельно).

---

## 1) Сбор урона атакера (per-type)

Для каждого типа
$$
 t \in \{\text{PHYS\_MELEE}, \text{PHYS\_RANGED}, \text{FIRE}, \text{FROST}, \text{LIGHTNING}, \text{POISON}\}:
$$

$$
\mathrm{Damage}^{(t)}_{\text{raw}}
=\Big((B^{(t)} + F^{(t)}) \cdot (1 + A^{(t)}) \cdot M^{(t)}\Big)\cdot (1+S^{(t)})
$$


- $B^{(t)}$ — базовый урон источника (оружие/заклинание), ед./хит.  
- $F^{(t)}$ — суммарные **плоские** прибавки, ед./хит.  
- $A^{(t)}$ — суммарный **аддитивный %** (доли; 0.15 = +15%), **складывается**.  
- $M^{(t)}$ = \prod_i (1+m^{(t)}_i)\) — **мультипликативные %** (перемножаются).  
- $S^{(t)}$ — скейл от атрибутов/архетипа (доли; напр. $STR_\%, DEX_\%, INT_\%$).

---

## 2) Крит

(Крит до защит.)

$$
\mathrm{Damage}^{(t)}_{\text{crit}}=
\begin{cases}
\mathrm{Damage}^{(t)}_{\text{raw}}\cdot c_{\text{crit}}, & \text{если } \mathrm{rand}() < p_{\text{crit}}\\[2pt]
\mathrm{Damage}^{(t)}_{\text{raw}}, & \text{иначе}
\end{cases}
$$

- $p_{\text{crit}}$ — шанс крита (0..1).  
- $c_{\text{crit}}$ — множитель крита (напр. 1.5 для +50%).

---

## 3) Броня и Блок *(только для физических типов)*

Выбери **одну** модель и зафиксируй её в документации.

### Модель A (выбранная) — «Блок добавляется к броне»

$$
\mathrm{Armor}_{\text{eff}}=\max(0,\; \mathrm{Armor}+\mathrm{Block})
$$
$$
\mathrm{DR}_{\text{armor}}=\frac{\mathrm{Armor}_{\text{eff}}}{\mathrm{Armor}_{\text{eff}}+K}
$$
$$
\mathrm{Damage}^{(\text{PHYS})}_{\text{postArmor}}
=\mathrm{Damage}^{(\text{PHYS})}_{\text{crit}}\cdot (1-\mathrm{DR}_{\text{armor}})
$$

### Модель B (запасная) — «Блок = плоская редукция после брони»

$$
\mathrm{DR}_{\text{armor}}=\frac{\mathrm{Armor}}{\mathrm{Armor}+K}
$$
$$
\mathrm{Damage}^{(\text{PHYS})}_{\text{postArmor}}
=\max\Big(0,\; \mathrm{Damage}^{(\text{PHYS})}_{\text{crit}}\cdot (1-\mathrm{DR}_{\text{armor}})-\mathrm{Block}\Big)
$$

- $K>0$ — броневой параметр баланса (подбирается под целевой TTK, обычно 100–400).  
- Для элементарных типов: $\mathrm{Damage}^{(t)}_{\text{postArmor}}=\mathrm{Damage}^{(t)}_{\text{crit}}$.

---

## 4) Резисты (per-type, с кэпом)

$$
R^{(t)}=\min\big(\max(R^{(t)}_{\text{raw}},\,0),\,\mathrm{Cap}_{\text{res}}\big)
$$

$$
\mathrm{Damage}^{(t)}_{\text{res}}
=\mathrm{Damage}^{(t)}_{\text{postArmor}}\cdot \big(1-R^{(t)}\big)
$$

- $R^{(t)}_{\text{raw}}$ — итоговый резист цели для типа \(t\) (доли).  
- $\mathrm{Cap}_{\text{res}}\in(0,1)$ — твёрдый кэп резистов (напр. 0.90).

---

## 5) Применение уклона

$$
\mathrm{Damage}^{(t)}_{\text{afterEvade}}
=\mathrm{Damage}^{(t)}_{\text{res}}\cdot I_{\text{evade}}
$$

---

## 6) Суммирование, клампы, округление

$$
\mathrm{Final}=\mathrm{round}\!\left(\max\!\left(0,\; \sum_{t}\mathrm{Damage}^{(t)}_{\text{afterEvade}}\right)\right)
$$

- Округляем до целого (HP).  
- Лиич/манастил считаются от $\mathrm{Final}$.

---

## 7) Пост-эффекты

- **Leech/Manasteal:**
  $$
  \mathrm{heal}=\left\lfloor \mathrm{Final}\cdot LS \right\rfloor,\quad
  \mathrm{mana}=\left\lfloor \mathrm{Final}\cdot MS \right\rfloor
  $$
  где $LS,MS$ — доли (кэп по доку).

- **Threat:** рассчитывается от $\mathrm{Final}$ (с учётом множителей угрозы).

---

## Переменные и единицы

- $p_{\text{evade}}\in[0,1]$ — шанс уклона; $I_{\text{evade}}\in\{0,1\}$ — индикатор уклонения.  
- $B^{(t)}$ — базовый урон источника (ед./хит).  
- $F^{(t)}$ — flat-бафы урона (ед./хит).  
- $A^{(t)}$ — суммарный **аддитивный %** (доли).  
- $M^{(t)}\ge 1$ — совокупный **мультипликативный** множитель.  
- $S^{(t)}$ — скейл от атрибутов (доли, напр. $STR_\%, DEX_\%, INT_\%$).  
- $p_{\text{crit}}\in[0,1]$, $c_{\text{crit}}\ge 1$ — параметры крита.  
- $\mathrm{Armor}$, $\mathrm{Block}$ — броня и блок (ед.); $K>0$ — параметр формулы брони.  
- $R^{(t)}\in[0,\mathrm{Cap}_{\text{res}}]$ — резист по типу (доли), $\mathrm{Cap}_{\text{res}}\in(0,1)$.  
- $LS,MS$ — доли лиича/воровства маны (с кэпами).  
- $\mathrm{Final}$ — финальный урон по HP (целое).

**Замечания по реализации:**  
- Все проценты храним как **доли** *(0.15 = 15%)*.  
- NaN/Inf → 0; далее клампы.  
- Для AoE/DoT/ground-эффектов обычно **без уклона/крита** — оговори отдельно.  
- Глобальные редукции \(DR_{\text{all}}\) (ауры/бафы) можно вставить после резистов: \(\mathrm{Final}\cdot (1-DR_{\text{all}})\) с кэпом.
