package net.glease.tc4tweak.api.infusionrecipe;

import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.crafting.InfusionRecipe;

/**
 * Main entry point into the library
 */
public interface InfusionRecipeExt {

    /**
     * A wrapper around {@link #addInfusionCraftingRecipe(String, ItemStack, int, AspectList, RecipeIngredient, RecipeIngredient...)} that tries to guess the ingredient type
     * <ul>
     * <li>ItemStack will map to {@link RecipeIngredient#item(boolean, ItemStack)} with check nbt disabled</li>
     * <li>ItemStack[] will map to {@link RecipeIngredient#items(boolean, ItemStack...)} with check nbt disabled</li>
     * <li>RecipeIngredient will be passed through unchanged</li>
     * <li>String will be considered oredict names and mapped to {@link RecipeIngredient#oredict(String)}</li>
     * <li>An array of above elements excluding {@code ItemStack[]} will be concatenated together using {@link RecipeIngredient#or(RecipeIngredient)}</li>
     * </ul>
     * Anything else will cause an IllegalArgumentException.
     * {@link List }s are not accepted, i.e. cause IllegalArgumentException as specified above.
     * @param aspects Required essentia
     * @param input center item
     * @param recipe items on the ring
     * @param instability infusion instability
     * @param research required research
     * @param result output item
     * @return recipe instance. it would have been added to the global recipe list
     */
    @SuppressWarnings("unused")
    EnhancedInfusionRecipe addInfusionCraftingRecipe(String research, ItemStack result, int instability, AspectList aspects, Object input, Object... recipe);

    /**
     * @deprecated still WIP. don't use yet
     * @param aspects Required essentia
     * @param input center item
     * @param recipe items on the ring
     * @param instability infusion instability
     * @param research required research
     * @param result output "thing"
     * @return recipe instance. it would have been added to the global recipe list
     */
    @SuppressWarnings("unused")
    EnhancedInfusionRecipe addInfusionCraftingRecipe(String research, ItemStack result, int instability, AspectList aspects, RecipeIngredient input, RecipeIngredient... recipe);

    /**
     * This variant overwrites the tag compound on the center item at given str to give tag
     * No merging is done whatsoever if a tag is already present with key {@code label}
     * @param aspects Required essentia
     * @param input center item
     * @param recipe items on the ring
     * @param instability infusion instability
     * @param research required research
     * @param label key in item nbt tag
     * @param tag tag to add
     * @return recipe instance. it would have been added to the global recipe list
     */
    @SuppressWarnings("unused")
    EnhancedInfusionRecipe addInfusionCraftingRecipeAddTag(String research, String label, NBTBase tag, int instability, AspectList aspects, RecipeIngredient input, RecipeIngredient... recipe);

    /**
     * Get an InfusionRecipe represented in EnhancedInfusionRecipe under current configuration. This should help prevent
     * writing the same recipe logic twice due to having two sets of API. The return value might change across config
     * reloads!
     * HOWEVER, do not use the result for actual recipe checking! Custom InfusionRecipe might override recipe logic,
     * and this compatibility layer will NOT replicate those custom logic. This compatibility layer is only for inspecting
     * recipe ingredient under a unified interface
     * This is a rather lightweight operation and the API does necessary caching for you. Caching key is the object identity
     * itself and make a minimal attempt at detecting modification inside recipe
     * No need to reinvent the caching layer!
     * @param recipe unknown recipe
     * @return recipe with its input unified to EnhancedInfusionRecipe
     */
    EnhancedInfusionRecipe convert(InfusionRecipe recipe);

    /**
     * Acquire an implementation of this interface.
     * @return implementation
     */
    static InfusionRecipeExt get() {
        return InfusionRecipeExtImpl.INSTANCE;
    }
}
