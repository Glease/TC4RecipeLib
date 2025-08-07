package net.glease.tc4tweak.api.infusionrecipe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.common.collect.ImmutableList;
import net.minecraft.item.ItemStack;

class RecipeIngredientOr implements RecipeIngredient {
    private final RecipeIngredient[] or;

    RecipeIngredientOr(RecipeIngredient... or) {
        List<RecipeIngredient> orList = new ArrayList<>();
        for (RecipeIngredient ingredient : or) {
            if (ingredient instanceof RecipeIngredientOr) {
                orList.addAll(Arrays.asList(((RecipeIngredientOr) ingredient).or));
            } else {
                orList.add(ingredient);
            }
        }
        this.or = orList.toArray(new RecipeIngredient[0]);
    }

    @Override
    public List<ItemStack> getRepresentativeStacks() {
        ImmutableList.Builder<ItemStack> builder = ImmutableList.builder();
        for (RecipeIngredient ingredient : or) {
            builder.addAll(ingredient.getRepresentativeStacks());
        }
        return builder.build();
    }

    @Override
    public boolean matches(ItemStack stack) {
        for (RecipeIngredient ingredient : or) {
            if (ingredient.matches(stack))
                return true;
        }
        return false;
    }
}
