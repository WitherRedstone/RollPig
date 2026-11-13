package com.chinaex123.roll_pig.init;

import com.chinaex123.roll_pig.RollPig;
import com.chinaex123.roll_pig.item.PigPenItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public interface RPItems {
    DeferredRegister.Items ITEMS = DeferredRegister.createItems(RollPig.MODID);

    DeferredItem<Item> PIGPEN = ITEMS.registerItem("pigpen", PigPenItem::new, properties -> properties.stacksTo(1));

}
