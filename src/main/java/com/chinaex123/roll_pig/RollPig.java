package com.chinaex123.roll_pig;

import com.chinaex123.roll_pig.config.RPConfig;
import com.chinaex123.roll_pig.data.state.PigRegistry;
import com.chinaex123.roll_pig.data.state.RPGlyphs;
import com.chinaex123.roll_pig.init.RPAttachments;
import com.chinaex123.roll_pig.init.RPCreativeTabs;
import com.chinaex123.roll_pig.init.RPItems;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

@Mod(RollPig.MODID)
public class RollPig {
    public static final String MODID = "roll_pig";
    public static final Logger LOGGER = LogUtils.getLogger();

    public RollPig(IEventBus modEventBus, ModContainer modContainer) {
        RPItems.ITEMS.register(modEventBus);
        RPAttachments.ATTACHMENT_TYPES.register(modEventBus);
        RPCreativeTabs.register(modEventBus);
        modContainer.registerConfig(ModConfig.Type.COMMON, RPConfig.SPEC);
        PigRegistry.all();
        RPGlyphs.warmup();
    }
}
