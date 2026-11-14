package com.chinaex123.roll_pig.data;

import com.chinaex123.roll_pig.RollPig;
import com.chinaex123.roll_pig.init.RPItems;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.data.PackOutput;

public class ModModelsProvider extends ModelProvider {
    public ModModelsProvider(PackOutput output) {
        super(output, RollPig.MODID);
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        itemModels.generateFlatItem(RPItems.PIGPEN.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
    }
}
