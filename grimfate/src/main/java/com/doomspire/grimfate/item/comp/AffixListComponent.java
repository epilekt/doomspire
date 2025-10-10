package com.doomspire.grimfate.item.comp;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.ArrayList;
import java.util.List;

/** Хранилище выбранных аффиксов (id + роллы) + редкость предмета. */
public record AffixListComponent(String rarityId, List<Entry> entries) {

    public record Entry(String id, List<Float> rolls) {
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.STRING.fieldOf("id").forGetter(Entry::id),
                Codec.FLOAT.listOf().fieldOf("rolls").forGetter(Entry::rolls)
        ).apply(i, Entry::new));
    }

    public static final Codec<AffixListComponent> CODEC =
            RecordCodecBuilder.create(i -> i.group(
                    Codec.STRING.optionalFieldOf("rarity", "").forGetter(AffixListComponent::rarityId),
                    Entry.CODEC.listOf().fieldOf("entries").forGetter(AffixListComponent::entries)
            ).apply(i, AffixListComponent::new));

    /** Для сетевой синхронизации data-component. */
    public static final StreamCodec<RegistryFriendlyByteBuf, AffixListComponent> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public AffixListComponent decode(RegistryFriendlyByteBuf buf) {
                    String rarity = buf.readUtf();
                    int n = buf.readVarInt();
                    List<Entry> list = new ArrayList<>(n);
                    for (int i = 0; i < n; i++) {
                        String id = buf.readUtf();
                        int m = buf.readVarInt();
                        List<Float> rolls = new ArrayList<>(m);
                        for (int j = 0; j < m; j++) rolls.add(buf.readFloat());
                        list.add(new Entry(id, rolls));
                    }
                    return new AffixListComponent(rarity, list);
                }
                @Override
                public void encode(RegistryFriendlyByteBuf buf, AffixListComponent v) {
                    buf.writeUtf(v.rarityId() == null ? "" : v.rarityId());
                    List<Entry> list = v.entries();
                    buf.writeVarInt(list.size());
                    for (Entry e : list) {
                        buf.writeUtf(e.id());
                        List<Float> rolls = e.rolls();
                        buf.writeVarInt(rolls.size());
                        for (Float r : rolls) buf.writeFloat(r);
                    }
                }
            };
}
