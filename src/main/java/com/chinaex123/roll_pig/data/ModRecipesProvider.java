package com.chinaex123.roll_pig.data;

import com.chinaex123.roll_pig.RollPig;
import com.chinaex123.roll_pig.init.RPItems;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.Tags;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class ModRecipesProvider extends RecipeProvider {
    private final HolderLookup.Provider provider;
    private final RecipeOutput output;

    public ModRecipesProvider(HolderLookup.Provider provider, RecipeOutput output) {
        super(provider, output);
        this.provider = provider;
        this.output = output;
    }

    public static class Runner extends RecipeProvider.Runner {
        public Runner(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
            super(packOutput, lookupProvider);
        }

        @Override
        protected @NotNull RecipeProvider createRecipeProvider(HolderLookup.Provider provider, RecipeOutput output) {
            return new ModRecipesProvider(provider, output);
        }

        @Override
        public @NotNull String getName() {
            return RollPig.MODID;
        }
    }

    @Override
    protected void buildRecipes() {
        HolderGetter<Item> itemRegistryLookup = this.registries.lookupOrThrow(Registries.ITEM);

        ShapedRecipeBuilder.shaped(itemRegistryLookup, RecipeCategory.MISC, RPItems.PIGPEN.get())
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
