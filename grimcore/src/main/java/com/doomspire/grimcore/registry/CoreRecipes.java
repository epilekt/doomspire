package com.doomspire.grimcore.registry;

public final class CoreRecipes {

    public static final net.neoforged.neoforge.registries.DeferredRegister<net.minecraft.world.item.crafting.RecipeSerializer<?>>
            SERIALIZERS = net.neoforged.neoforge.registries.DeferredRegister.create(
            net.minecraft.core.registries.Registries.RECIPE_SERIALIZER,
            com.doomspire.grimcore.Grimcore.MODID
    );

    public static final net.neoforged.neoforge.registries.DeferredRegister<net.minecraft.world.item.crafting.RecipeType<?>>
            TYPES = net.neoforged.neoforge.registries.DeferredRegister.create(
            net.minecraft.core.registries.Registries.RECIPE_TYPE,
            com.doomspire.grimcore.Grimcore.MODID
    );

    public static final net.neoforged.neoforge.registries.DeferredHolder<net.minecraft.world.item.crafting.RecipeType<?>, net.minecraft.world.item.crafting.RecipeType<com.doomspire.grimcore.recipe.Circle6Recipe>>
            CIRCLE6_TYPE = TYPES.register("circle6", () -> new net.minecraft.world.item.crafting.RecipeType<>() {
        public String toString() { return com.doomspire.grimcore.Grimcore.MODID + ":circle6"; }
    });

    public static final net.neoforged.neoforge.registries.DeferredHolder<net.minecraft.world.item.crafting.RecipeSerializer<?>, net.minecraft.world.item.crafting.RecipeSerializer<com.doomspire.grimcore.recipe.Circle6Recipe>>
            CIRCLE6_SERIALIZER = SERIALIZERS.register("circle6", com.doomspire.grimcore.recipe.Circle6RecipeSerializer::new);

    public static void init(net.neoforged.bus.api.IEventBus modBus) {
        TYPES.register(modBus);
        SERIALIZERS.register(modBus);
    }
}

