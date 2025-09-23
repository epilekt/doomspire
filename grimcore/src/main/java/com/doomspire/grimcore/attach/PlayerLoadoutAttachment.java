package com.doomspire.grimcore.attach;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Лоадаут спеллов игрока: 6 слотов + пер-слот кулдауны.
 * Сериализация:
 *  - CODEC (в NBT/сейв): slots как список строк (пустая строка = null), cooldowns как список int.
 *  - STREAM_CODEC (в сеть): на слот пишем флаг наличия + ResourceLocation (если есть), затем cooldowns.
 */
public final class PlayerLoadoutAttachment {

    public static final int SLOTS = 6;

    private final ResourceLocation[] slots = new ResourceLocation[SLOTS];
    private final int[] cooldown = new int[SLOTS];

    public PlayerLoadoutAttachment() {}

    // -------- API --------
    public ResourceLocation get(int slot) { check(slot); return slots[slot]; }
    public void set(int slot, ResourceLocation id) { check(slot); slots[slot] = id; }

    public int getCooldown(int slot) { check(slot); return cooldown[slot]; }
    public void setCooldown(int slot, int ticks) { check(slot); cooldown[slot] = Math.max(0, ticks); }

    public void tickDown() {
        for (int i = 0; i < SLOTS; i++) if (cooldown[i] > 0) cooldown[i]--;
    }

    public void clearAll() {
        Arrays.fill(slots, null);
        Arrays.fill(cooldown, 0);
    }

    private static void check(int s) {
        if (s < 0 || s >= SLOTS) throw new IndexOutOfBoundsException("slot " + s);
    }

    // -------- CODEC (persist) --------
    public static final Codec<PlayerLoadoutAttachment> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            // список строк длиной SLOTS; "" означает пустой слот
            Codec.list(Codec.STRING).fieldOf("slots").forGetter(att -> {
                List<String> out = new ArrayList<>(SLOTS);
                for (int i = 0; i < SLOTS; i++) out.add(att.slots[i] != null ? att.slots[i].toString() : "");
                return out;
            }),
            Codec.list(Codec.INT).fieldOf("cooldowns").forGetter(att -> {
                List<Integer> out = new ArrayList<>(SLOTS);
                for (int i = 0; i < SLOTS; i++) out.add(att.cooldown[i]);
                return out;
            })
    ).apply(inst, (slotStrings, cds) -> {
        PlayerLoadoutAttachment att = new PlayerLoadoutAttachment();
        int n = Math.min(SLOTS, slotStrings.size());
        for (int i = 0; i < n; i++) {
            String s = slotStrings.get(i);
            if (s != null && !s.isEmpty()) {
                ResourceLocation rl = ResourceLocation.tryParse(s);
                if (rl != null) att.slots[i] = rl;
            }
        }
        int m = Math.min(SLOTS, cds.size());
        for (int i = 0; i < m; i++) att.cooldown[i] = Math.max(0, cds.get(i));
        return att;
    }));

    // -------- STREAM_CODEC (network) --------
    public static final StreamCodec<RegistryFriendlyByteBuf, PlayerLoadoutAttachment> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public PlayerLoadoutAttachment decode(RegistryFriendlyByteBuf buf) {
                    PlayerLoadoutAttachment att = new PlayerLoadoutAttachment();
                    for (int i = 0; i < SLOTS; i++) {
                        boolean has = buf.readBoolean();
                        if (has) att.slots[i] = ResourceLocation.STREAM_CODEC.decode(buf);
                    }
                    for (int i = 0; i < SLOTS; i++) {
                        att.cooldown[i] = buf.readVarInt();
                    }
                    return att;
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buf, PlayerLoadoutAttachment att) {
                    for (int i = 0; i < SLOTS; i++) {
                        ResourceLocation rl = att.slots[i];
                        buf.writeBoolean(rl != null);
                        if (rl != null) ResourceLocation.STREAM_CODEC.encode(buf, rl);
                    }
                    for (int i = 0; i < SLOTS; i++) {
                        buf.writeVarInt(att.cooldown[i]);
                    }
                }
            };
}
