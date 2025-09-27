package com.doomspire.grimcore.spell.autobolt;

public record AutoBoltResult(boolean ok, String reason, int cooldownTicks, float projectileSpeed) {
    public static AutoBoltResult ok(int cd, float speed) { return new AutoBoltResult(true, null, cd, speed); }
    public static AutoBoltResult denied(String why)       { return new AutoBoltResult(false, why, 0, 0.0f); }
}

