package com.doomspire.grimfate.client.tooltip;

import com.doomspire.grimcore.affix.rarity.RarityDataManager;
import com.doomspire.grimcore.affix.rarity.RarityDef;
import com.doomspire.grimfate.item.comp.AffixListComponent;
import com.doomspire.grimfate.registry.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
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

        // ===== Заголовок по редкости =====
        String rarityId = comp.rarityId(); // требуется обновлённый AffixListComponent
        if (rarityId != null && !rarityId.isEmpty()) {
            var defOpt = RarityDataManager.INSTANCE.get(ResourceLocation.tryParse(rarityId));
            if (defOpt.isPresent()) {
                RarityDef def = defOpt.get(); // содержит display_key и text_color

                String titleKey = def.displayKey().isEmpty()
                        ? ("rarity." + def.id().getNamespace() + "." + def.id().getPath())
                        : def.displayKey();

                MutableComponent title = Component.translatable(titleKey);
                int rgb = def.textColor();
                title.withStyle(s -> s.withColor(rgb).withBold(true));
                e.getToolTip().add(title);
            }
        } else {
            // Фолбэк (если по какой-то причине редкость не была сохранена)
            e.getToolTip().add(Component.literal("Affixes")
                    .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        }

        // ===== Список аффиксов =====
        for (AffixListComponent.Entry en : entries) {
            String nice  = prettifyId(en.id());      // человекочитаемое имя
            String rolls = formatRolls(en.rolls());  // "0.12, 0.34"

            MutableComponent line = rolls.isEmpty()
                    ? Component.literal("• " + nice)
                    : Component.literal("• " + nice + " (" + rolls + ")");

            line.withStyle(ChatFormatting.BLUE);
            e.getToolTip().add(line);
        }
    }

    private static String prettifyId(String id) {
        if (id == null || id.isEmpty()) return "?";
        int colon = id.indexOf(':');
        String tail = colon >= 0 ? id.substring(colon + 1) : id;
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
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < rolls.size(); i++) {
            float v = rolls.get(i);
            sb.append(String.format("%.2f", v));
            if (i + 1 < rolls.size()) sb.append(", ");
        }
        return sb.toString();
    }
}
