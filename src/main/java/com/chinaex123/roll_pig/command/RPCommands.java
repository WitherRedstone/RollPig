package com.chinaex123.roll_pig.command;

import com.chinaex123.roll_pig.RollPig;
import com.chinaex123.roll_pig.api.RollResult;
import com.chinaex123.roll_pig.event.RollPigService;
import com.chinaex123.roll_pig.network.PigPenPayload;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.Collection;
import java.util.List;

@EventBusSubscriber(modid = RollPig.MODID)
public final class RPCommands {

    private static final String[] NAMES = {"rollpig", "今日猪猪"};

    private static final int FORCE_PERMISSION = 2;

    private RPCommands() { }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        for (String name : NAMES) {
            dispatcher.register(Commands.literal(name)
                    .executes(RPCommands::rollSelf)
                    .then(Commands.literal("force")
                            .requires(source -> source.hasPermission(FORCE_PERMISSION))
                            // /rollpig force —— 给自己来一次
                            .executes(context -> forceRoll(context,
                                    List.of(context.getSource().getPlayerOrException())))
                            // /rollpig force <targets> —— 给别人来一次
                            .then(Commands.argument("targets", EntityArgument.players())
                                    .executes(context -> forceRoll(context,
                                            EntityArgument.getPlayers(context, "targets"))))));
        }
    }

    private static int rollSelf(CommandContext<CommandSourceStack> context) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        if (player.containerMenu != player.inventoryMenu) {
            context.getSource().sendFailure(Component.translatable("message.roll_pig.close_container"));
            return 0;
        }
        return RollPigService.tryRoll(player, PigPenPayload.MODE_RESULT) ? 1 : 0;
    }

    private static int forceRoll(CommandContext<CommandSourceStack> context, Collection<ServerPlayer> targets) {
        int success = 0;
        for (ServerPlayer target : targets) {
            RollResult result = RollPigService.forceRoll(target, PigPenPayload.MODE_RESULT);
            if (!result.success()) {
                continue;
            }
            success++;
            context.getSource().sendSuccess(() -> Component.translatable("message.roll_pig.force.done",
                    target.getDisplayName(),
                    Component.literal(result.pig().name())).withStyle(ChatFormatting.GRAY), true);
        }
        if (success == 0) {
            context.getSource().sendFailure(Component.translatable("message.roll_pig.force.failed")
                    .withStyle(ChatFormatting.RED));
        }
        return success;
    }
}
