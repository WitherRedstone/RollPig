package com.chinaex123.roll_pig;

import com.chinaex123.roll_pig.data.state.PigRegistry;
import com.chinaex123.roll_pig.init.RPAttachments;
import com.chinaex123.roll_pig.init.RPCreativeTabs;
import com.chinaex123.roll_pig.init.RPItems;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;

@Mod(RollPig.MODID)
public class RollPig {
    public static final String MODID = "roll_pig";
    public static final Logger LOGGER = LogUtils.getLogger();

    public RollPig(IEventBus modEventBus, ModContainer modContainer) {
        RPItems.ITEMS.register(modEventBus);
        RPAttachments.ATTACHMENT_TYPES.register(modEventBus);
        RPCreativeTabs.register(modEventBus);
        // 启动时就把猪猪资料读一遍，日志里能直接看到读到了多少只
        PigRegistry.all();
    }
}
