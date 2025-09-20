package com.doomspire.grimcore.nearby;

public final class PullPlanner {

    public static final class OriginRef {
        public final net.minecraft.core.BlockPos containerPos;
        public final int containerSlot;
        public final int targetSlot;
        public final net.minecraft.world.item.ItemStack moved;
        public OriginRef(net.minecraft.core.BlockPos pos, int cslot, int tslot, net.minecraft.world.item.ItemStack moved) {
            this.containerPos = pos; this.containerSlot = cslot; this.targetSlot = tslot; this.moved = moved.copy();
        }
    }

    public static final class PullReport {
        private final java.util.List<OriginRef> origins = new java.util.ArrayList<>();
        public void add(OriginRef r){ origins.add(r); }
        public java.util.List<OriginRef> origins(){ return java.util.Collections.unmodifiableList(origins); }
        public boolean isEmpty(){ return origins.isEmpty(); }
    }

    /**
     * Пытается заполнить целевой инвентарь под данную "потребность" (список ингредиентов) из снапшота.
     * Не сканирует мир, работает только с данными снапшота. Без тиков. Порядок ингредиентов не важен.
     *
     * @param snapshot       результат NearbyItemSnapshot.scan(...)
     * @param needs          список ингредиентов (каждый — 1 шт.) размером N (для нашей станции N=6, shapeless)
     * @param target         IItemHandler целевой (6 входных слотов станции)
     * @param targetSlots    список индексов целевых слотов, куда можно класть (например [0..5])
     */
    public static PullReport planAndPullToHandler(NearbyItemSnapshot snapshot,
                                                  java.util.List<net.minecraft.world.item.crafting.Ingredient> needs,
                                                  net.neoforged.neoforge.items.IItemHandler target,
                                                  int[] targetSlots) {
        PullReport report = new PullReport();
        if (snapshot == null || snapshot.isEmpty() || needs == null || needs.isEmpty()) return report;

        // локальная отметка занятых слотов цели (заполняем свободные)
        boolean[] targetUsed = new boolean[target.getSlots()];
        for (int s : targetSlots) {
            if (!target.getStackInSlot(s).isEmpty()) targetUsed[s] = true;
        }

        // для каждого ингредиента попробуем найти совпадающий предмет в ближайших контейнерах
        outer:
        for (net.minecraft.world.item.crafting.Ingredient ing : needs) {
            // сначала определим свободный целевой слот (любой из targetSlots)
            int tSlot = -1;
            for (int s : targetSlots) {
                if (!targetUsed[s]) { tSlot = s; break; }
            }
            if (tSlot == -1) break; // нет свободных слотов

            // пробежка по контейнерам → по слотам
            for (NearbyItemSnapshot.ContainerRef cref : snapshot.containers()) {
                var ih = cref.handler();
                if (ih == null) continue;

                for (int cslot = 0; cslot < ih.getSlots(); cslot++) {
                    net.minecraft.world.item.ItemStack got = ih.getStackInSlot(cslot);
                    if (got.isEmpty()) continue;
                    if (!ing.test(got)) continue;

                    // симуляция: можем ли положить 1 шт. в целевой слот?
                    net.minecraft.world.item.ItemStack one = got.copy(); one.setCount(1);
                    net.minecraft.world.item.ItemStack simulated = target.insertItem(tSlot, one, true);
                    if (!simulated.isEmpty()) {
                        // не смогли вставить (есть остаток) — пробуем другой целевой слот
                        continue;
                    }

                    // пробуем извлечь 1 шт. и реально вставить
                    net.minecraft.world.item.ItemStack extracted = ih.extractItem(cslot, 1, false);
                    if (extracted.isEmpty()) continue; // кто-то опередил

                    net.minecraft.world.item.ItemStack left = target.insertItem(tSlot, extracted, false);
                    if (!left.isEmpty()) {
                        // крайне маловероятно, но откатим назад
                        // пытаемся вернуть остаток в тот же слот
                        net.minecraft.world.item.ItemStack rem = ih.insertItem(cslot, left, false);
                        if (!rem.isEmpty()) {
                            // если совсем некуда — дропнем рядом (на совести вызывающей стороны обработать)
                        }
                        continue; // ищем дальше
                    }

                    // успех
                    targetUsed[tSlot] = true;
                    report.add(new OriginRef(cref.pos, cslot, tSlot, one));
                    continue outer;
                }
            }
            // если сюда дошли — не нашли для этого ингредиента (нормально, частичное заполнение)
        }

        return report;
    }
}

