package com.doomspire.grimcore.recipe;

public final class Circle6RecipeSerializer implements net.minecraft.world.item.crafting.RecipeSerializer<Circle6Recipe> {

    // === JSON (data pack) ===
    private static final com.mojang.serialization.Codec<java.util.List<net.minecraft.world.item.crafting.Ingredient>> ING_LIST_CODEC =
            net.minecraft.world.item.crafting.Ingredient.CODEC_NONEMPTY
                    .listOf() // Codec<List<Ingredient>>
                    .flatXmap(list -> list.size() == 6
                                    ? com.mojang.serialization.DataResult.success(list)
                                    : com.mojang.serialization.DataResult.error(() -> "circle6 requires exactly 6 ingredients"),
                            com.mojang.serialization.DataResult::success);

    private static final com.mojang.serialization.MapCodec<Circle6Recipe> MAP_CODEC =
            com.mojang.serialization.codecs.RecordCodecBuilder.mapCodec(instance -> instance.group(
                    ING_LIST_CODEC.fieldOf("ingredients").forGetter(Circle6Recipe::getIngredientsList),
                    net.minecraft.world.item.ItemStack.CODEC.fieldOf("result")
                            .forGetter(r -> r.getResultItem(null))
            ).apply(instance,
                    (ings, out) -> new Circle6Recipe(null, ings, out)));

    @Override
    public com.mojang.serialization.MapCodec<Circle6Recipe> codec() {
        return MAP_CODEC;
    }

    // === Сеть (sync к клиенту) ===
    private static final net.minecraft.network.codec.StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, Circle6Recipe> STREAM_CODEC =
            new net.minecraft.network.codec.StreamCodec<>() {
                @Override
                public Circle6Recipe decode(net.minecraft.network.RegistryFriendlyByteBuf buf) {
                    java.util.ArrayList<net.minecraft.world.item.crafting.Ingredient> ings = new java.util.ArrayList<>(6);
                    for (int i = 0; i < 6; i++) {
                        ings.add(net.minecraft.world.item.crafting.Ingredient.CONTENTS_STREAM_CODEC.decode(buf));
                    }
                    net.minecraft.world.item.ItemStack result = net.minecraft.world.item.ItemStack.STREAM_CODEC.decode(buf);
                    // id будет подставлен RecipeManager’ом; здесь используем null
                    return new Circle6Recipe(null, ings, result);
                }

                @Override
                public void encode(net.minecraft.network.RegistryFriendlyByteBuf buf, Circle6Recipe recipe) {
                    for (net.minecraft.world.item.crafting.Ingredient ing : recipe.getIngredientsList()) {
                        net.minecraft.world.item.crafting.Ingredient.CONTENTS_STREAM_CODEC.encode(buf, ing);
                    }
                    net.minecraft.world.item.ItemStack.STREAM_CODEC.encode(buf, recipe.getResultItem(null));
                }
            };

    @Override
    public net.minecraft.network.codec.StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, Circle6Recipe> streamCodec() {
        return STREAM_CODEC;
    }
}



