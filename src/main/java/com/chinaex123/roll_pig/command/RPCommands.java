package com.chinaex123.roll_pig.command;

import com.chinaex123.roll_pig.RollPig;
import com.chinaex123.roll_pig.event.logic.RollPigService;
import com.chinaex123.roll_pig.network.PigPenPayload;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = RollPig.MODID)
public final class RPCommands {

    private static final String[] NAMES = {"rollpig", "今日猪猪"};

    private RPCommands() { }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        for (String name : NAMES) {
            event.getDispatcher().register(Commands.literal(name).executes(context -> {
                ServerPlayer player = context.getSource().getPlayerOrException();
                if (player.containerMenu != player.inventoryMenu) {
                    context.getSource().sendFailure(Component.translatable("message.roll_pig.close_container"));
                    return 0;
                }
                return RollPigService.tryRoll(player, PigPenPayload.MODE_RESULT) ? 1 : 0;
            }));
        }
    }
}
