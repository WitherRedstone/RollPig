package com.chinaex123.roll_pig.data;

import com.chinaex123.roll_pig.init.RPItems;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.HolderGetter;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.neoforged.neoforge.common.Tags;

public class ModRecipesProvider extends RecipeProvider {
    public ModRecipesProvider(BootstrapContext<Recipe<?>> recipeOutput, BootstrapContext<Advancement> advancementOutput) {
        super(recipeOutput, advancementOutput);
    }

    @Override
    protected void buildRecipes() {
        HolderGetter<Item> items = this.items;

        ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC, RPItems.PIGPEN.get())
                .pattern("BBB")
                .pattern("BAB")
                .pattern("BBB")
                .define('A', Tags.Items.CROPS_CARROT)
                .define('B', Items.PORKCHOP)
                .unlockedBy(getHasName(Items.CARROT), has(Items.CARROT))
                .unlockedBy(getHasName(Items.PORKCHOP), has(Items.PORKCHOP))
                .save(output);

    }

}
