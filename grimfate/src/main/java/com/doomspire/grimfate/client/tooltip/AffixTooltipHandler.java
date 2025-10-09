package com.doomspire.grimfate.client.tooltip;

import com.doomspire.grimfate.item.comp.AffixListComponent;
import com.doomspire.grimfate.registry.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.List;

public final class AffixTooltipHandler {

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent e) {
        ItemStack stack = e.getItemStack();
        var compType = ModDataComponents.AFFIX_LIST.get();
        AffixListComponent comp = stack.get(compType);
        if (comp == null) return;

        List<AffixListComponent.Entry> entries = comp.entries();
        if (entries == null || entries.isEmpty()) return;

        // Заголовок
        e.getToolTip().add(Component.literal("Affixes")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));

        // Список аффиксов
        for (AffixListComponent.Entry en : entries) {
            String nice  = prettifyId(en.id());      // человекочитаемое имя
            String rolls = formatRolls(en.rolls());  // "0.12, 0.34"

            net.minecraft.network.chat.MutableComponent line =
                    rolls.isEmpty()
                            ? net.minecraft.network.chat.Component.literal("• " + nice)
                            : net.minecraft.network.chat.Component.literal("• " + nice + " (" + rolls + ")");

            // применяем стиль и добавляем
            line.withStyle(net.minecraft.ChatFormatting.BLUE);
            e.getToolTip().add(line);
        }

    }

    private static String prettifyId(String id) {
        if (id == null || id.isEmpty()) return "?";
        // ожидаем вида "namespace:path/like_this" или "namespace:id"
        int colon = id.indexOf(':');
        String tail = colon >= 0 ? id.substring(colon + 1) : id;
        // берем последний сегмент пути, заменяем '_' на ' ', капитализуем слова
        int slash = tail.lastIndexOf('/');
        String base = (slash >= 0 ? tail.substring(slash + 1) : tail).replace('_', ' ');
        String[] parts = base.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (p.isEmpty()) continue;
            sb.append(Character.toUpperCase(p.charAt(0)));
            if (p.length() > 1) sb.append(p.substring(1));
            sb.append(' ');
        }
        return sb.toString().trim();
    }

    private static String formatRolls(List<Float> rolls) {
        if (rolls == null || rolls.isEmpty()) return "";
        // пока не знаем шкалу — выводим «как есть», до двух знаков
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < rolls.size(); i++) {
            float v = rolls.get(i);
            sb.append(String.format("%.2f", v));
            if (i + 1 < rolls.size()) sb.append(", ");
        }
        return sb.toString();
    }
}
