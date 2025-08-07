/**
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * EnhancedInfusionRecipe
 * Copyright (C) 2023 Glease
 * <p>
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * <p>
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 * USA
 */
package net.glease.tc4tweak.api.infusionrecipe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.crafting.InfusionRecipe;

public class EnhancedInfusionRecipe extends InfusionRecipe {

    protected final RecipeIngredient central;
    protected final List<RecipeIngredient> components;

    protected EnhancedInfusionRecipe(String research, Object output, int inst, AspectList aspects2, RecipeIngredient input, List<RecipeIngredient> recipe) {
        super(research, output, inst, aspects2, input.getRepresentativeStack(), recipe.stream().map(RecipeIngredient::getRepresentativeStack).toArray(ItemStack[]::new));
        this.central = input;
        this.components = recipe;
    }

    public RecipeIngredient getCentral() {
        return central;
    }

    public List<RecipeIngredient> getComponentsExt() {
        return Collections.unmodifiableList(components);
    }

    @Override
    public boolean matches(ArrayList<ItemStack> input, ItemStack central, World world, EntityPlayer player) {
        if (this.getRecipeInput() == null) return false;
        if (!this.research.isEmpty() && !ThaumcraftApiHelper.isResearchComplete(player.getCommandSenderName(), this.research)) {
            return false;
        }
        if (!getCentral().matches(central)) return false;
        List<ItemStack> l = new ArrayList<>(input);

        outer:
        for (RecipeIngredient ingredient : getComponentsExt()) {
            for (Iterator<ItemStack> iterator = l.iterator(); iterator.hasNext(); ) {
                ItemStack stack = iterator.next();
                if (ingredient.matches(stack)) {
                    iterator.remove();
                    continue outer;
                }
            }
            return false;
        }
        return true;
    }
}
