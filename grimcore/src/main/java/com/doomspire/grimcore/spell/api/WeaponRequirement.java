package com.doomspire.grimcore.spell.api;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

//NOTE: DTO c требованиями к оружию для кастов/навыков.* Опираемся на теги предметов (data/*/tags/items/...) и флаг двуручности.
//Использование
// - Зафиксировать нужные теги (anyOf / allOf / noneOf) через фабрики или Builder
// - Флаг twoHandedOnly=true — требуем, чтобы оружие относилось к группе "двуручное" +
//   (проверяется в WeaponGate по нашему тегу items/two_handed или по профилю оружия)" +
//Важно: класс — чистый перенос данных (без логики проверки)" +
//Проверка выполняется в {@code WeaponGate}" +

        public final class WeaponRequirement {

            /** Должен совпасть ХОТЯ БЫ один тег из набора. */
            private final Set<TagKey<Item>> anyOfTags;

            /** Должны совпасть ВСЕ теги из набора. */
            private final Set<TagKey<Item>> allOfTags;

            /** НЕЛЬЗЯ, чтобы совпал ЛЮБОЙ из этих тегов. */
            private final Set<TagKey<Item>> noneOfTags;

            /** Требуется ли строго двуручное оружие (по тегу/профилю). */
            private final boolean twoHandedOnly;

            private WeaponRequirement(Set<TagKey<Item>> anyOfTags,
                                      Set<TagKey<Item>> allOfTags,
                                      Set<TagKey<Item>> noneOfTags,
                                      boolean twoHandedOnly) {
                this.anyOfTags = unmodifiableCopy(anyOfTags);
                this.allOfTags = unmodifiableCopy(allOfTags);
                this.noneOfTags = unmodifiableCopy(noneOfTags);
                this.twoHandedOnly = twoHandedOnly;
            }

            private static <T> Set<T> unmodifiableCopy(Set<T> in) {
                if (in == null || in.isEmpty()) return Collections.emptySet();
                return Collections.unmodifiableSet(new LinkedHashSet<>(in));
                // LinkedHashSet — предсказуемый порядок для дебага/тултипов
            }

            public Set<TagKey<Item>> anyOfTags()     { return anyOfTags; }
            public Set<TagKey<Item>> allOfTags()     { return allOfTags; }
            public Set<TagKey<Item>> noneOfTags()    { return noneOfTags; }
            public boolean twoHandedOnly()           { return twoHandedOnly; }

            // ---------- Фабрики для удобства ----------

            /** Требуется совпасть хотя бы с одним из тегов. */
            @SafeVarargs
            public static WeaponRequirement anyOf(TagKey<Item>... any) {
                return new Builder().anyOf(any).build();
            }

            /** Требуется совпасть по всем указанным тегам. */
            @SafeVarargs
            public static WeaponRequirement allOf(TagKey<Item>... all) {
                return new Builder().allOf(all).build();
            }

            /** Запрет по тегам (blacklist). */
            @SafeVarargs
            public static WeaponRequirement noneOf(TagKey<Item>... none) {
                return new Builder().noneOf(none).build();
            }

            /** Чистый билдер без предзаданных тегов. */
            public static Builder builder() {
                return new Builder();
            }

            // ---------- Builder ----------

            public static final class Builder {
                private final Set<TagKey<Item>> any = new LinkedHashSet<>();
                private final Set<TagKey<Item>> all = new LinkedHashSet<>();
                private final Set<TagKey<Item>> none = new LinkedHashSet<>();
                private boolean twoHandedOnly = false;

                @SafeVarargs
                public final Builder anyOf(TagKey<Item>... tags) {
                    if (tags != null) any.addAll(Arrays.asList(tags));
                    return this;
                }

                @SafeVarargs
                public final Builder allOf(TagKey<Item>... tags) {
                    if (tags != null) all.addAll(Arrays.asList(tags));
                    return this;
                }

                @SafeVarargs
                public final Builder noneOf(TagKey<Item>... tags) {
                    if (tags != null) none.addAll(Arrays.asList(tags));
                    return this;
                }

                /** Строго двуручное оружие (по тегу items/two_handed или WeaponProfile). */
                public Builder twoHandedOnly(boolean value) {
                    this.twoHandedOnly = value;
                    return this;
                }

                public WeaponRequirement build() {
                    return new WeaponRequirement(any, all, none, twoHandedOnly);
                }
            }

            // ---------- equals/hashCode/toString для удобства логов/сравнений ----------

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (!(o instanceof WeaponRequirement that)) return false;
                return twoHandedOnly == that.twoHandedOnly
                        && Objects.equals(anyOfTags, that.anyOfTags)
                        && Objects.equals(allOfTags, that.allOfTags)
                        && Objects.equals(noneOfTags, that.noneOfTags);
            }

            @Override
            public int hashCode() {
                return Objects.hash(anyOfTags, allOfTags, noneOfTags, twoHandedOnly);
            }

            @Override
            public String toString() {
                return "WeaponRequirement{" +
                        "any=" + anyOfTags +
                        ", all=" + allOfTags +
                        ", none=" + noneOfTags +
                        ", twoHandedOnly=" + twoHandedOnly +
                        '}';
            }
        }

