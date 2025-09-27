package com.doomspire.grimfate.item.comp;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.ArrayList;
import java.util.List;

/** Хранилище выбранных аффиксов (id + роллы). Эффекты подключим позже. */
public record AffixListComponent(List<Entry> entries) {

    public record Entry(String id, List<Float> rolls) {
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.STRING.fieldOf("id").forGetter(Entry::id),
                Codec.FLOAT.listOf().fieldOf("rolls").forGetter(Entry::rolls)
        ).apply(i, Entry::new));
    }

    public static final Codec<AffixListComponent> CODEC =
            Entry.CODEC.listOf().xmap(AffixListComponent::new, AffixListComponent::entries);

    /** Для сетевой синхронизации data-component. */
    public static final StreamCodec<RegistryFriendlyByteBuf, AffixListComponent> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public AffixListComponent decode(RegistryFriendlyByteBuf buf) {
                    int n = buf.readVarInt();
                    List<Entry> list = new ArrayList<>(n);
                    for (int i = 0; i < n; i++) {
                        String id = buf.readUtf();
                        int m = buf.readVarInt();
                        List<Float> rolls = new ArrayList<>(m);
                        for (int j = 0; j < m; j++) rolls.add(buf.readFloat());
                        list.add(new Entry(id, rolls));
                    }
                    return new AffixListComponent(list);
                }
                @Override
                public void encode(RegistryFriendlyByteBuf buf, AffixListComponent v) {
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
