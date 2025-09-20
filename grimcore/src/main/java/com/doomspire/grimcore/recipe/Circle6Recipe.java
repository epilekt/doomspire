package com.doomspire.grimcore.recipe;

public class Circle6Recipe implements net.minecraft.world.item.crafting.Recipe<net.minecraft.world.item.crafting.RecipeInput> {

    private final net.minecraft.resources.ResourceLocation id;
    private final net.minecraft.core.NonNullList<net.minecraft.world.item.crafting.Ingredient> ingredients; // size=6, shapeless
    private final net.minecraft.world.item.ItemStack result;

    public Circle6Recipe(net.minecraft.resources.ResourceLocation id,
                         java.util.List<net.minecraft.world.item.crafting.Ingredient> inputs,
                         net.minecraft.world.item.ItemStack result) {
        if (inputs.size() != 6) throw new IllegalArgumentException("circle6 requires exactly 6 ingredients");
        this.id = id;
        this.ingredients = net.minecraft.core.NonNullList.create();
        this.ingredients.addAll(inputs);
        this.result = result.copy();
    }

    // shapeless матчинг по мультимножеству: используем первые 6 позиций из RecipeInput
    @Override
    public boolean matches(net.minecraft.world.item.crafting.RecipeInput inv, net.minecraft.world.level.Level level) {
        if (inv.size() < 6) return false;

        java.util.ArrayList<net.minecraft.world.item.ItemStack> present = new java.util.ArrayList<>(6);
        for (int i = 0; i < 6; i++) {
            net.minecraft.world.item.ItemStack s = inv.getItem(i);
            if (s.isEmpty()) return false;
            present.add(s);
        }
        boolean[] used = new boolean[present.size()];
        for (net.minecraft.world.item.crafting.Ingredient ing : ingredients) {
            boolean ok = false;
            for (int i = 0; i < present.size(); i++) {
                if (!used[i] && ing.test(present.get(i))) { used[i] = true; ok = true; break; }
            }
            if (!ok) return false;
        }
        return true;
    }

    @Override
    public net.minecraft.world.item.ItemStack assemble(net.minecraft.world.item.crafting.RecipeInput inv,
                                                       net.minecraft.core.HolderLookup.Provider registries) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int w, int h) { return w * h >= 6; }

    @Override
    public net.minecraft.world.item.ItemStack getResultItem(net.minecraft.core.HolderLookup.Provider registries) {
        return result;
    }

    public net.minecraft.resources.ResourceLocation id(){ return id; }
    public net.minecraft.resources.ResourceLocation getId(){ return id; }

    @Override
    public net.minecraft.world.item.crafting.RecipeSerializer<?> getSerializer() {
        return com.doomspire.grimcore.registry.CoreRecipes.CIRCLE6_SERIALIZER.get();
    }

    @Override
    public net.minecraft.world.item.crafting.RecipeType<?> getType() {
        return com.doomspire.grimcore.registry.CoreRecipes.CIRCLE6_TYPE.get();
    }

    public java.util.List<net.minecraft.world.item.crafting.Ingredient> getIngredientsList() {
        return java.util.Collections.unmodifiableList(ingredients);
    }
}



