package com.doomspire.grimfate.item.armor;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;

public class GenericGeoArmorItem extends ArmorItem implements GeoItem {
    // <— это нужно ModItems.copperVisual()
    public record Visual(net.minecraft.resources.ResourceLocation geo,
                         net.minecraft.resources.ResourceLocation texture) {}

    private final Visual visual;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public GenericGeoArmorItem(Holder<ArmorMaterial> material, Type type, Properties props, Visual visual) {
        super(material, type, props);
        this.visual = visual;
    }

    public Visual visual() { return visual; }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return cache; }

    // Новый API GeckoLib 4.5+: регистрируем провайдера рендера брони
    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private GeoArmorRenderer<?> renderer;

            @Override
            public <T extends LivingEntity> net.minecraft.client.model.HumanoidModel<?> getGeoArmorRenderer(
                    @Nullable T living, ItemStack stack, @Nullable EquipmentSlot slot,
                    @Nullable net.minecraft.client.model.HumanoidModel<T> original) {
                if (renderer == null) {
                    renderer = new com.doomspire.grimfate.client.render.armor.BaseArmorRenderer();
                }
                return renderer;
            }
        });
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, 20, state -> {
            // базовая "idle" анимация предмета брони — если будет нужна
            state.setAnimation(DefaultAnimations.IDLE);

            // Правка: armorSlots есть только у LivingEntity
            var e = state.getData(software.bernie.geckolib.constant.DataTickets.ENTITY);
            if (!(e instanceof LivingEntity living)) return PlayState.STOP;

            // Можно включать всегда, либо доделать проверку полного сета позже
            for (ItemStack s : living.getArmorSlots()) {
                // no-op: проверка на пустоту при желании
            }
            return PlayState.CONTINUE;
        }));
    }
}
