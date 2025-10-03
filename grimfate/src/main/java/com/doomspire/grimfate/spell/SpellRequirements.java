package com.doomspire.grimfate.spell;

import com.doomspire.grimcore.spell.api.WeaponRequirement;
import net.minecraft.resources.ResourceLocation;

/**
 * Централизованная таблица: какой спелл требует какое оружие.
 * На старте держим тут только то, что уже есть; позже расширим при добавлении новых спеллов.
 */
public final class SpellRequirements {
    private SpellRequirements() {}

    /**
     * Вернёт требования к оружию для указанного спелла, либо null если требований нет.
     */
    public static WeaponRequirement require(ResourceLocation spellId) {
        if (spellId == null) return null;

        // Примеры (раскомментировать/добавлять по мере готовности спеллов):
        // if (spellId.equals(ResourceLocation.fromNamespaceAndPath("grimfate", "hunter_mark"))) {
        //     return WeaponRequirement.builder().allowTag("grimfate:bows").build();
        // }
        // if (spellId.equals(ResourceLocation.fromNamespaceAndPath("grimfate", "mage_bolt"))) {
        //     return WeaponRequirement.builder().allowTag("grimfate:staves").build();
        // }

        return null; // по умолчанию — без требований
    }
}

