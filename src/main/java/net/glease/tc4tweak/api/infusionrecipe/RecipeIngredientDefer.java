package net.glease.tc4tweak.api.infusionrecipe;

import java.lang.invoke.MethodHandle;
import java.util.List;

import net.minecraft.item.ItemStack;

class RecipeIngredientDefer implements RecipeIngredient {
    private final MethodHandle supplier;

    RecipeIngredientDefer(MethodHandle supplier) {
        this.supplier = supplier;
    }

    private RecipeIngredient get() {
        try {
            return (RecipeIngredient) supplier.invokeExact();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public RecipeIngredient or(RecipeIngredient or) {
        return get().or(or);
    }

    @Override
    public boolean matches(ItemStack stack) {
        return get().matches(stack);
    }

    @Override
    public ItemStack getRepresentativeStack() {
        return get().getRepresentativeStack();
    }

    @Override
    public List<ItemStack> getRepresentativeStacks() {
        return get().getRepresentativeStacks();
    }
}
