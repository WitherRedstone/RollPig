package com.chinaex123.roll_pig.init;

import com.chinaex123.roll_pig.RollPig;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class RPCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, RollPig.MODID);

    public static final Supplier<CreativeModeTab> ROLL_PIG_TAB =
            CREATIVE_MODE_TAB.register("roll_pig_tab", () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(RPItems.PIGPEN.get()))
                    .title(Component.translatable("itemGroup.roll_pig_tab"))
                    .displayItems((parameters, output) -> {

                        output.accept(RPItems.PIGPEN.get());

                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TAB.register(eventBus);
    }
}
