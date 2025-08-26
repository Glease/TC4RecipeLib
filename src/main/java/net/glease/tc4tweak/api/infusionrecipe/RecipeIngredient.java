/**
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * EnhancedInfusionRecipe
 * Copyright (C) 2023 Glease
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 * USA
 */
package net.glease.tc4tweak.api.infusionrecipe;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.commons.lang3.ArrayUtils;
import thaumcraft.api.crafting.InfusionRecipe;

public interface RecipeIngredient {
    /**
     * The return value is intended to be passed into constructor of PositionedStack directly.
     * Only suitable for the purpose of displaying recipes in NEI and nothing else.
     * Might change across save reloads.
     *
     * All stacks returned here must pass the test of {@link #matches(ItemStack)}.
     * Not all stacks that pass the test of {@link #matches(ItemStack)} needs to be returned here.
     *
     * @return a list of item. no null allowed
     */
    List<ItemStack> getRepresentativeStacks();

    /**
     * The return value is intended to be passed into constructor of PositionedStack directly.
     * Only suitable for the purpose of compatibilities in {@link EnhancedInfusionRecipe#getRecipeInput()},
     * {@link EnhancedInfusionRecipe#getComponents()}  and nothing else.
     * Might change across save reloads.
     *
     * All stacks returned here must pass the test of {@link #matches(ItemStack)}.
     * Not all stacks that pass the test of {@link #matches(ItemStack)} needs to be returned here.
     *
     * @return usually just the first element of {@link #getRepresentativeStacks()}
     */
    default ItemStack getRepresentativeStack() {
        List<ItemStack> stacks = getRepresentativeStacks();
        return stacks.get(0);
    }

    /**
     * Test if given stack match the requirement of this ingredient
     * @param stack crafting input
     * @return result
     */
    boolean matches(ItemStack stack);

    /**
     * Helper function to create a union of two ingredients. Has short-circuiting behavior like java boolean operators.
     * Prefer this over given ingredient
     *
     * @param or the alternative
     * @return a new ingredient that matches either of these 2 ingredients, with a preference of this.
     */
    default RecipeIngredient or(RecipeIngredient or) {
        return new RecipeIngredientOr(this, or);
    }

    /**
     * Create a new recipe ingredient that must oredict to this oredict name.
     * Any ingredient with given oredict name will be allowed, even if it has a lot of other oredict names.
     *
     * Examples:
     * <table>
     *     <thead>
     *         <tr>
     *             <th>required oredict name</th>
     *             <th>given item's oredict names</th>
     *             <th>matches() result</th>
     *         </tr>
     *     </thead>
     *     <tbody>
     *         <tr><td>logWood</td><td>logWood</td><td>true</td></tr>
     *         <tr><td>logWood</td><td>logWood, craftingLogWood</td><td>true</td></tr>
     *         <tr><td>logWood</td><td>craftingLogWood</td><td>false</td></tr>
     *     </tbody>
     * </table>
     *
     * @param name oredict name
     * @return constructed ingredient
     */
    static RecipeIngredient oredict(String name) {
        return new RecipeIngredient() {

            private int oreID = -1;

            @Override
            public List<ItemStack> getRepresentativeStacks() {
                return OreDictionary.getOres(name, false);
            }

            @Override
            public boolean matches(ItemStack stack) {
                if (oreID == -1)
                    oreID = OreDictionary.getOreID(name);
                return ArrayUtils.contains(OreDictionary.getOreIDs(stack), oreID);
            }
        };
    }

    /**
     * Create a new recipe ingredient that must only oredict to this oredict name.
     * Any ingredient that has only one oredict name and matches the given string will be allowed.
     *
     * Examples:
     * <table>
     *     <thead>
     *         <tr>
     *             <th>required oredict name</th>
     *             <th>given item's oredict names</th>
     *             <th>matches() result</th>
     *         </tr>
     *     </thead>
     *     <tbody>
     *         <tr><td>logWood</td><td>logWood</td><td>true</td></tr>
     *         <tr><td>logWood</td><td>logWood, craftingLogWood</td><td>false</td></tr>
     *         <tr><td>logWood</td><td>craftingLogWood</td><td>false</td></tr>
     *     </tbody>
     * </table>
     *
     * @param name oredict name
     * @return constructed ingredient
     */
    static RecipeIngredient oredictStrict(String name) {
        return new RecipeIngredient() {

            private int oreID = -1;

            @Override
            public List<ItemStack> getRepresentativeStacks() {
                return OreDictionary.getOres(name, false);
            }

            @Override
            public boolean matches(ItemStack stack) {
                if (oreID == -1)
                    oreID = OreDictionary.getOreID(name);
                int[] oreIDs = OreDictionary.getOreIDs(stack);
                return oreIDs.length == 1 && oreIDs[0] == oreID;
            }
        };
    }

    /**
     * Create a new recipe ingredient that <b>DOES NOT</b> allow any oredict substitution at all.
     *
     * HOWEVER, if the given item has a metadata of {@link OreDictionary#WILDCARD_VALUE} (or {@code Short.MAX-1}),
     * then it will allow any itemstack with the give item, regardless of what metadata or NBT tag either has.
     * @param target match target
     * @param checkNBTTags false to ignore nbt tag mismatch, true otherwise
     * @return constructed ingredient
     */
    static RecipeIngredient item(boolean checkNBTTags, ItemStack target) {
        return new RecipeIngredient() {
            @Override
            public List<ItemStack> getRepresentativeStacks() {
                return Collections.singletonList(target);
            }

            @Override
            public boolean matches(ItemStack stack) {
                return Utility.itemMatches(target, stack, checkNBTTags);
            }
        };
    }

    /**
     * Create a new recipe ingredient that <b>DOES NOT</b> allow any oredict substitution at all.
     *
     * HOWEVER, if the given item has a metadata of {@link OreDictionary#WILDCARD_VALUE} (or {@code Short.MAX-1}),
     * then it will allow any itemstack with the give item, regardless of what metadata or NBT tag either has.
     * @param targets match targets
     * @param checkNBTTags false to ignore nbt tag mismatch, true otherwise
     * @return constructed ingredient
     */
    static RecipeIngredient items(boolean checkNBTTags, ItemStack... targets) {
        return new RecipeIngredient() {
            @Override
            public List<ItemStack> getRepresentativeStacks() {
                return Arrays.asList(targets);
            }

            @Override
            public boolean matches(ItemStack stack) {
                for (ItemStack target : targets) {
                    if (Utility.itemMatches(target, stack, checkNBTTags))
                        return true;
                }
                return false;
            }
        };
    }

    /**
     * Special placeholder recipe ingredient that will not accept any recipe ingredient. usually found only as a result of
     * {@link InfusionRecipeExt#convert(InfusionRecipe)}.
     * Its representative item is a unspecified item with a display name signaling something is very wrong with the recipe
     * It will not match any item.
     */
    RecipeIngredient ERROR = new RecipeIngredient() {
        @Override
        public List<ItemStack> getRepresentativeStacks() {
            ItemStack error = new ItemStack(Blocks.fire);
            error.setStackDisplayName(EnumChatFormatting.RED + "ERROR NULL INGREDIENT");
            return Collections.singletonList(error);
        }

        @Override
        public boolean matches(ItemStack stack) {
            return false;
        }
    };
}

