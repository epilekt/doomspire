package com.doomspire.grimcore.nearby;

public final class NearbyItemSnapshot {

    public static final class ContainerRef {
        public final net.minecraft.core.BlockPos pos;
        public final net.neoforged.neoforge.capabilities.BlockCapabilityCache<net.neoforged.neoforge.items.IItemHandler, net.minecraft.core.Direction> cache;
        public ContainerRef(net.minecraft.core.BlockPos pos,
                            net.neoforged.neoforge.capabilities.BlockCapabilityCache<net.neoforged.neoforge.items.IItemHandler, net.minecraft.core.Direction> cache) {
            this.pos = pos;
            this.cache = cache;
        }
        public net.neoforged.neoforge.items.IItemHandler handler() { return cache.getCapability(); }
    }

    private final java.util.List<ContainerRef> containers;
    private NearbyItemSnapshot(java.util.List<ContainerRef> list) { this.containers = java.util.Collections.unmodifiableList(list); }
    public java.util.List<ContainerRef> containers() { return containers; }
    public boolean isEmpty() { return containers.isEmpty(); }

    public static NearbyItemSnapshot scan(net.minecraft.server.level.ServerLevel level,
                                          net.minecraft.core.BlockPos base,
                                          net.minecraft.core.Direction facing,
                                          boolean longFootprint,
                                          int radius,
                                          int maxContainers) {
        radius = java.lang.Math.max(0, java.lang.Math.min(4, radius));
        maxContainers = java.lang.Math.max(1, maxContainers);

        java.util.ArrayList<ContainerRef> found = new java.util.ArrayList<>();
        java.util.HashSet<net.minecraft.core.BlockPos> seen = new java.util.HashSet<>();

        double aLong = radius + 1.0;
        double aShort = radius + 0.5;
        boolean longX = (facing.getAxis() == net.minecraft.core.Direction.Axis.X);
        double aX = longFootprint ? (longX ? aLong : aShort) : (radius + 0.5);
        double aZ = longFootprint ? (longX ? aShort : aLong) : (radius + 0.5);
        int v = radius;

        net.minecraft.core.BlockPos top = base.above();
        net.minecraft.core.BlockPos.MutableBlockPos m = new net.minecraft.core.BlockPos.MutableBlockPos();
        int rx = (int) java.lang.Math.ceil(java.lang.Math.max(aX, 0));
        int rz = (int) java.lang.Math.ceil(java.lang.Math.max(aZ, 0));

        for (int dy = -v; dy <= v; dy++) {
            for (int dz = -rz; dz <= rz; dz++) {
                for (int dx = -rx; dx <= rx; dx++) {
                    if (dx == 0 && dz == 0 && (dy == 0 || top.getY() - base.getY() == dy)) continue;

                    double nx = dx / aX;
                    double nz = dz / aZ;
                    if ((nx * nx + nz * nz) > 1.0) continue;

                    m.set(base.getX() + dx, base.getY() + dy, base.getZ() + dz);
                    if (!level.isLoaded(m)) continue;

                    // 1) без стороны
                    var cache = net.neoforged.neoforge.capabilities.BlockCapabilityCache.create(
                            net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.BLOCK, level, m.immutable(), null);
                    var h = cache.getCapability();

                    // 2) пробуем по сторонам
                    if (h == null) {
                        for (net.minecraft.core.Direction d : net.minecraft.core.Direction.values()) {
                            var c2 = net.neoforged.neoforge.capabilities.BlockCapabilityCache.create(
                                    net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.BLOCK, level, m.immutable(), d);
                            var h2 = c2.getCapability();
                            if (h2 != null) { cache = c2; h = h2; break; }
                        }
                    }
                    if (h == null) continue;

                    net.minecraft.core.BlockPos key = m.immutable();
                    if (seen.add(key)) {
                        found.add(new ContainerRef(key, cache));
                        if (found.size() >= maxContainers) return new NearbyItemSnapshot(found);
                    }
                }
            }
        }
        return new NearbyItemSnapshot(found);
    }
}


