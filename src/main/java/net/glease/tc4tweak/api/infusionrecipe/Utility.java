package net.glease.tc4tweak.api.infusionrecipe;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

class Utility {
    private static final MethodHandle rule;

    static {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle get, getCurrent;
        try {
            Class<?> infusionOreDictMode = Class.forName("net.glease.tc4tweak.modules.infusionRecipe.InfusionOreDictMode");
            get = lookup.unreflect(infusionOreDictMode.getMethod("get", ItemStack.class));
            try {
                // try to get the synced infusion recipe mode
                Class<?> networkedConfig = Class.forName("net.glease.tc4tweak.network.NetworkedConfiguration");
                getCurrent = lookup.findStatic(networkedConfig, "getInfusionOreDictMode", MethodType.methodType(infusionOreDictMode));
                get = MethodHandles.foldArguments(get, getCurrent);
            } catch (ReflectiveOperationException e) {
                // in case using an old version of TC4Tweaks without sync
                Class<?> localConfig = Class.forName("net.glease.tc4tweak.ConfigurationHandler");
                getCurrent = lookup.findStatic(localConfig, "getInfusionOreDictMode", MethodType.methodType(infusionOreDictMode, localConfig)).bindTo(localConfig.getEnumConstants()[0]);
                get = MethodHandles.foldArguments(get, getCurrent);
            }
        } catch (ReflectiveOperationException ex) {
            // probably no TC4Tweaks, or it's too old
            try {
                get = lookup.findStatic(Utility.class, "defaultRule", MethodType.methodType(RecipeIngredient.class, ItemStack.class));
            } catch (ReflectiveOperationException e) {
                throw new AssertionError(e);
            }
        }
        rule = get;
    }

    static boolean itemMatches(ItemStack target, ItemStack given, boolean checkNBTTags) {
        if (Items.feather.getDamage(target) == OreDictionary.WILDCARD_VALUE) {
            if (target.getItem() != given.getItem())
                return false;
        } else {
            if (!target.isItemEqual(given))
                return false;
        }
        return !checkNBTTags || ItemStack.areItemStackTagsEqual(target, given);
    }

    static RecipeIngredient convertUnderCurrentRule(ItemStack recipeSpec) {
        if (recipeSpec == null) return RecipeIngredient.ERROR;
        return new RecipeIngredientDefer(rule.bindTo(recipeSpec));
    }

    static RecipeIngredient defaultRule(ItemStack recipeSpec) {
        @SuppressWarnings("deprecation") // let's not worry about deprecation in a no longer updated project
        int oreID = OreDictionary.getOreID(recipeSpec);
        if (oreID == -1) return RecipeIngredient.item(false, recipeSpec);
        return RecipeIngredient.oredict(OreDictionary.getOreName(oreID));
    }
}
