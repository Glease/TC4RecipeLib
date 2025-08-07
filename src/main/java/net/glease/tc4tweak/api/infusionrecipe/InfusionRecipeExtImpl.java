package net.glease.tc4tweak.api.infusionrecipe;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.StringUtils;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.crafting.InfusionRecipe;

class InfusionRecipeExtImpl implements InfusionRecipeExt {
    static final InfusionRecipeExtImpl INSTANCE = new InfusionRecipeExtImpl();
    private static final LoadingCache<InfusionRecipe, EnhancedInfusionRecipe> conversionCache = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .weakKeys()
            .build(new CacheLoader<InfusionRecipe, EnhancedInfusionRecipe>() {
                @Override
                public EnhancedInfusionRecipe load(InfusionRecipe key) {
                    // caller will ensure the instance is not one of ours
                    return new EnhancedInfusionRecipe(
                            key.getResearch(),
                            key.getRecipeOutput(),
                            key.getInstability(),
                            key.getAspects(),
                            Utility.convertUnderCurrentRule(key.getRecipeInput()),
                            Arrays.stream(key.getComponents()).map(Utility::convertUnderCurrentRule).collect(Collectors.toList())
                            );
                }
            });

    @SuppressWarnings("unused")
    @Deprecated
    @Override
    public EnhancedInfusionRecipe addInfusionCraftingRecipe(String research, ItemStack result, int instability, AspectList aspects, Object input, Object... recipe) {
        return addInfusionCraftingRecipe(research, result, instability, aspects, map(input), Arrays.stream(recipe).map(InfusionRecipeExtImpl::map).toArray(RecipeIngredient[]::new));
    }

    private static RecipeIngredient map(Object thing) {
        if (thing instanceof RecipeIngredient)
            return (RecipeIngredient) thing;
        if (thing instanceof ItemStack)
            return RecipeIngredient.item(false, (ItemStack) thing);
        if (thing instanceof ItemStack[])
            return RecipeIngredient.items(false, (ItemStack[]) thing);
        if (thing instanceof String)
            return RecipeIngredient.oredict((String) thing);
        if (thing instanceof Object[])
            return Arrays.stream((Object[]) thing)
                    .map(InfusionRecipeExtImpl::map)
                    .reduce(RecipeIngredient::or)
                    .orElseThrow(() -> new IllegalArgumentException("Infusion recipe given an empty array"));
        throw new IllegalArgumentException("Not a recipe ingredient: " + thing);
    }

    @SuppressWarnings("unused")
    @Deprecated
    @Override
    public EnhancedInfusionRecipe addInfusionCraftingRecipe(String research, ItemStack result, int instability, AspectList aspects, RecipeIngredient input, RecipeIngredient... recipe) {
        if (result == null || result.getItem() == null || result.stackSize <= 0)
            throw new IllegalArgumentException("result");
        return addInfusionCraftingRecipe(research, (Object) result, instability, aspects, input, recipe);
    }

    @SuppressWarnings("unused")
    @Deprecated
    @Override
    public EnhancedInfusionRecipe addInfusionCraftingRecipeAddTag(String research, String label, NBTBase tag, int instability, AspectList aspects, RecipeIngredient input, RecipeIngredient... recipe) {
        if (StringUtils.isNullOrEmpty(label))
            throw new IllegalArgumentException("label");
        if (tag == null)
            throw new IllegalArgumentException("tag");
        return addInfusionCraftingRecipe(research, new Object[]{label, tag}, instability, aspects, input, recipe);
    }

    @Override
    public EnhancedInfusionRecipe convert(InfusionRecipe recipe) {
        if (recipe instanceof EnhancedInfusionRecipe) return (EnhancedInfusionRecipe) recipe;
        return conversionCache.getUnchecked(recipe);
    }

    private static EnhancedInfusionRecipe addInfusionCraftingRecipe(String research, Object output, int instability, AspectList aspects, RecipeIngredient input, RecipeIngredient[] recipe) {
        if (aspects == null)
            throw new IllegalArgumentException("aspects");
        if (input == null)
            throw new IllegalArgumentException("input");
        if (recipe == null || recipe.length == 0 || Arrays.stream(recipe).anyMatch(Objects::isNull))
            throw new IllegalArgumentException("recipe");
        EnhancedInfusionRecipe r = new EnhancedInfusionRecipe(research, output, instability, aspects, input, Arrays.asList(recipe));
        getCraftingRecipes().add(r);
        return r;
    }

    // this is a huge mix of everything and we cannot really tell if it's workbench, infusion or crucible recipe
    private static List<Object> getCraftingRecipes() {
        @SuppressWarnings("unchecked")
        List<Object> recipes = ThaumcraftApi.getCraftingRecipes();
        return recipes;
    }
}
