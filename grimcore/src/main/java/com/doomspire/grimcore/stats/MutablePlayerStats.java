package com.doomspire.grimcore.stats;

/**
 * Лёгкий мутабельный wrapper. Используется в runtime для изменений "на месте".
 * Конвертируется в immutable PlayerStats только при commit.
 */
public class MutablePlayerStats {
    public int health;
    public int mana;
    public int maxHealth;
    public int maxMana;
    public int healthRegen;
    public int manaRegen;

    private boolean dirty = false;

    public MutablePlayerStats(PlayerStats base) {
        this.health = base.health();
        this.mana = base.mana();
        this.maxHealth = base.maxHealth();
        this.maxMana = base.maxMana();
        this.healthRegen = base.healthRegen();
        this.manaRegen = base.manaRegen();
    }

    public synchronized void setHealth(int h) { if (this.health != h) { this.health = h; this.dirty = true; } }
    public synchronized void setMana(int m)   { if (this.mana != m)   { this.mana = m;   this.dirty = true; } }
    public synchronized void setMaxHealth(int v) { if (this.maxHealth != v) { this.maxHealth = v; this.dirty = true; } }
    public synchronized void setMaxMana(int v)   { if (this.maxMana != v)   { this.maxMana = v;   this.dirty = true; } }
    public synchronized void setHealthRegen(int v) { if (this.healthRegen != v) { this.healthRegen = v; this.dirty = true; } }
    public synchronized void setManaRegen(int v)   { if (this.manaRegen != v)   { this.manaRegen = v;   this.dirty = true; } }

    public synchronized boolean isDirty() { return dirty; }
    public synchronized void clearDirty() { this.dirty = false; }

    public synchronized PlayerStats toImmutable() {
        return new PlayerStats(health, mana, maxHealth, maxMana, healthRegen, manaRegen);
    }
}
