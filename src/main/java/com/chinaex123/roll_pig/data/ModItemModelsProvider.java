package com.chinaex123.roll_pig.data;

import com.chinaex123.roll_pig.RollPig;
import com.chinaex123.roll_pig.init.RPItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModItemModelsProvider extends ItemModelProvider {
    public ModItemModelsProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, RollPig.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        basicItem(RPItems.PIGPEN.get());
    }
}
