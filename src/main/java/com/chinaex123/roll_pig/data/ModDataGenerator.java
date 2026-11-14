package com.chinaex123.roll_pig.data;

import com.chinaex123.roll_pig.RollPig;
import net.minecraft.data.recipes.RecipeProvider;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.data.event.GatherDataRegistryEntriesEvent;

@EventBusSubscriber(modid = RollPig.MODID)
public class ModDataGenerator {

    @SubscribeEvent
    public static void gatherRegistryEntries(GatherDataRegistryEntriesEvent event) {
        event.add(RecipeProvider.asBootstrap(ModRecipesProvider::new));
    }

    @SubscribeEvent
    public static void gatherData(GatherDataEvent.Client event) {
        event.createProvider(ModModelsProvider::new);
    }
}
