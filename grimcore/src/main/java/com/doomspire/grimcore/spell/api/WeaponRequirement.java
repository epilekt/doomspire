package com.doomspire.grimcore.spell.api;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

//NOTE: DTO c требованиями к оружию для кастов/навыков.* Опираемся на теги предметов (data/*/tags/items/...) и флаг двуручности.
/**
 * Семантика:
 *  - anyOfTags — хотя бы один из тегов должен совпасть;
 *  - allOfTags — каждый из тегов должен совпасть;
 *  - noneOfTags — ни один из тегов не должен совпасть (запрещённые категории);
 *  - twoHandedOnly — предмет должен быть двуручным (см. WeaponGate.TWO_HANDED и внешний резолвер).
 */

public final class WeaponRequirement {

    private final Set<TagKey<Item>> anyOfTags;
    private final Set<TagKey<Item>> allOfTags;
    private final Set<TagKey<Item>> noneOfTags;
    private final boolean twoHandedOnly;

    private WeaponRequirement(Set<TagKey<Item>> anyOf,
                              Set<TagKey<Item>> allOf,
                              Set<TagKey<Item>> noneOf,
                              boolean twoHandedOnly) {
        this.anyOfTags = anyOf == null ? Collections.emptySet() : Set.copyOf(anyOf);
        this.allOfTags = allOf == null ? Collections.emptySet() : Set.copyOf(allOf);
        this.noneOfTags = noneOf == null ? Collections.emptySet() : Set.copyOf(noneOf);
        this.twoHandedOnly = twoHandedOnly;
    }

    // ==== getters, которые ожидает WeaponGate ====
    public Set<TagKey<Item>> anyOfTags()   { return anyOfTags; }
    public Set<TagKey<Item>> allOfTags()   { return allOfTags; }
    public Set<TagKey<Item>> noneOfTags()  { return noneOfTags; }
    public boolean twoHandedOnly()         { return twoHandedOnly; }

    // ==== Builder API ====
    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private final LinkedHashSet<TagKey<Item>> any = new LinkedHashSet<>();
        private final LinkedHashSet<TagKey<Item>> all = new LinkedHashSet<>();
        private final LinkedHashSet<TagKey<Item>> none = new LinkedHashSet<>();
        private boolean twoHanded;

        /** Требуется совпасть ХОТЯ БЫ с одним из этих тегов. */
        public Builder anyOf(TagKey<Item> tag) { if (tag != null) any.add(tag); return this; }
        public Builder anyOf(ResourceLocation tagId) { return anyOf(tagKey(tagId)); }

        /** Требуется совпасть со ВСЕМИ этими тегами. */
        public Builder allOf(TagKey<Item> tag) { if (tag != null) all.add(tag); return this; }
        public Builder allOf(ResourceLocation tagId) { return allOf(tagKey(tagId)); }

        /** Предмет НЕ должен иметь ни один из этих тегов. */
        public Builder noneOf(TagKey<Item> tag) { if (tag != null) none.add(tag); return this; }
        public Builder noneOf(ResourceLocation tagId) { return noneOf(tagKey(tagId)); }

        /** Требовать двуручность. */
        public Builder requireTwoHanded(boolean v) { this.twoHanded = v; return this; }

        public WeaponRequirement build() {
            return new WeaponRequirement(any, all, none, twoHanded);
        }

        private static TagKey<Item> tagKey(ResourceLocation id) {
            Objects.requireNonNull(id, "tag id");
            return TagKey.create(Registries.ITEM, id);
        }
    }

    // ==== Удобные фабрики под наши теги ====
    public static WeaponRequirement stavesOnly() {
        return builder()
                .anyOf(ResourceLocation.fromNamespaceAndPath("grimfate", "staves"))
                .build();
    }

    public static WeaponRequirement bowsOnly() {
        return builder()
                .anyOf(ResourceLocation.fromNamespaceAndPath("grimfate", "bows"))
                .build();
    }

    public static WeaponRequirement twoHandedFrom(ResourceLocation... families) {
        Builder b = builder().requireTwoHanded(true);
        if (families != null) for (ResourceLocation id : families) b.anyOf(id);
        return b.build();
    }

    @Override
    public String toString() {
        return "WeaponRequirement{any=" + anyOfTags + ", all=" + allOfTags +
                ", none=" + noneOfTags + ", twoHanded=" + twoHandedOnly + "}";
    }
}