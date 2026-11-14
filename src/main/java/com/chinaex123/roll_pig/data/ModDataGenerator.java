package com.chinaex123.roll_pig.data;

import com.chinaex123.roll_pig.RollPig;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = RollPig.MODID)
public class ModDataGenerator {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent.Client event) {
        event.createProvider(ModRecipesProvider.Runner::new);
        event.createProvider(ModModelsProvider::new);
    }
}
