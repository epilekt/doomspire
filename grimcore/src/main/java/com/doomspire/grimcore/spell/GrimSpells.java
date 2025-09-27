package com.doomspire.grimcore.spell;

import com.doomspire.grimcore.spell.api.Spell;
import net.minecraft.resources.ResourceLocation;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Простой реестр спеллов в рантайме.
 * Регистрация выполняется на общих инициализациях мода или при загрузке datapack-тюнинга (в т.ч. референс-спеллы).
 */
public final class GrimSpells {
    private GrimSpells() {}

    private static final Map<ResourceLocation, Spell> REGISTRY = new LinkedHashMap<>();

    public static void register(Spell spell) {
        REGISTRY.put(spell.id(), spell);
    }

    public static Spell get(ResourceLocation id) {
        return REGISTRY.get(id);
    }

    public static Map<ResourceLocation, Spell> all() {
        return Collections.unmodifiableMap(REGISTRY);
    }

    public static void clear() {
        REGISTRY.clear();
    }
}

