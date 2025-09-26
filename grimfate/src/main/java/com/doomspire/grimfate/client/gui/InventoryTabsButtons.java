package com.doomspire.grimfate.client.gui;

import com.doomspire.grimfate.core.Grimfate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

@EventBusSubscriber(modid = Grimfate.MODID, value = Dist.CLIENT)
public final class InventoryTabsButtons {
    private InventoryTabsButtons() {}

    private static final ResourceLocation TAB_TEX =
            ResourceLocation.fromNamespaceAndPath(Grimfate.MODID, "textures/gui/stats/button_tab.png");
    private static final ResourceLocation ICON_STATS =
            ResourceLocation.fromNamespaceAndPath(Grimfate.MODID, "textures/gui/stats/icon_stats.png");
    private static final ResourceLocation ICON_SKILLS =
            ResourceLocation.fromNamespaceAndPath(Grimfate.MODID, "textures/gui/stats/icon_skills.png");

    // Держим ссылки для лайв-репозиционирования
    private static SpriteTabButton STATS_BTN;
    private static SpriteTabButton SKILLS_BTN;

    @SubscribeEvent
    public static void onInit(ScreenEvent.Init.Post e) {
        Screen screen = e.getScreen();
        if (!(screen instanceof InventoryScreen inv)) return;

        int left = inv.getGuiLeft();
        int top  = inv.getGuiTop();
        int w    = inv.getXSize();

        int xStats  = left + w + 2;
        int yStats  = top;
        int xSkills = xStats;
        int ySkills = yStats + 22;

        STATS_BTN = new SpriteTabButton(
                xStats, yStats,
                TAB_TEX, 20, 20, 60,
                ICON_STATS, 16, 48, 3, // 3 кадра у icon_stats
                () -> Minecraft.getInstance().setScreen(new InventoryWithStatsScreen(Minecraft.getInstance().player)));
        STATS_BTN.setTooltip(Tooltip.create(Component.translatable("grimfate.ui.stats.open")));

        SKILLS_BTN = new SpriteTabButton(
                xSkills, ySkills,
                TAB_TEX, 20, 20, 60,
                ICON_SKILLS, 16, 80, 5, // 5 кадров у icon_skills
                () -> {
                    var p = Minecraft.getInstance().player;
                    if (p != null) p.displayClientMessage(Component.literal("Древо не нарисовано, но открылось бы и все работает"), true);
                });

        SKILLS_BTN.setTooltip(Tooltip.create(Component.literal("Дерево навыков (WIP)")));

        e.addListener(STATS_BTN);
        e.addListener(SKILLS_BTN);
    }

    // Двигаем кнопки каждый кадр — они «клеятся» к инвентарю и при книге рецептов
    @SubscribeEvent
    public static void onRender(ScreenEvent.Render.Post e) {
        Screen screen = e.getScreen();
        if (!(screen instanceof InventoryScreen inv)) return;
        int left = inv.getGuiLeft();
        int top  = inv.getGuiTop();
        int w    = inv.getXSize();

        if (STATS_BTN != null) {
            STATS_BTN.setX(left + w + 2);
            STATS_BTN.setY(top);
        }
        if (SKILLS_BTN != null) {
            SKILLS_BTN.setX(left + w + 2);
            SKILLS_BTN.setY(top + 22);
        }
    }

    /** Кнопка-«таб»: фон 3 состояния, иконка — анимированный спрайт по времени. */
    static final class SpriteTabButton extends AbstractButton {
        private final ResourceLocation tabTex;
        private final int tabFrameW, tabFrameH, tabTexH;

        private final ResourceLocation iconTex;
        private final int iconFrame;   // размер кадра иконки (обычно 16)
        private final int iconTexH;    // высота текстуры иконки (для blit)
        private final int iconFrames;  // сколько кадров у иконки

        private boolean pressedVisual = false;
        private final Runnable onPress;

        SpriteTabButton(
                int x, int y,
                ResourceLocation tabTex, int tabFrameW, int tabFrameH, int tabTexH,
                ResourceLocation iconTex, int iconFrame, int iconTexH, int iconFrames,
                Runnable onPress
        ) {
            super(x, y, tabFrameW, tabFrameH, Component.empty());
            this.tabTex = tabTex;
            this.tabFrameW = tabFrameW;
            this.tabFrameH = tabFrameH;
            this.tabTexH = tabTexH;

            this.iconTex = iconTex;
            this.iconFrame = iconFrame;
            this.iconTexH = iconTexH;
            this.iconFrames = Math.max(1, iconFrames);

            this.onPress = onPress;
        }

        @Override
        protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
            // Фон — по состоянию
            int state = this.isHovered() ? 1 : 0;
            if (pressedVisual) state = 2; // 0/1/2 = normal/hover/pressed
            int vTab = state * tabFrameH;
            g.blit(tabTex, getX(), getY(), 0, vTab, tabFrameW, tabFrameH, tabFrameW, tabTexH);

            // Иконка — анимируем по времени, независимо от состояния
            long ms = System.currentTimeMillis();
            int anim = (int)((ms / 200L) % iconFrames); // ~5 FPS
            int vIcon = anim * iconFrame;

            int ix = getX() + (tabFrameW - 16) / 2;
            int iy = getY() + (tabFrameH - 16) / 2;
            g.blit(iconTex, ix, iy, 0, vIcon, 16, 16, 16, iconTexH);
        }

        @Override
        public void onPress() {
            if (onPress != null) onPress.run();
        }

        @Override
        public boolean mouseClicked(double mx, double my, int button) {
            if (this.isMouseOver(mx, my)) pressedVisual = true;
            return super.mouseClicked(mx, my, button);
        }

        @Override
        public boolean mouseReleased(double mx, double my, int button) {
            pressedVisual = false;
            return super.mouseReleased(mx, my, button);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narration) {
            this.defaultButtonNarrationText(narration);
        }
    }
}
