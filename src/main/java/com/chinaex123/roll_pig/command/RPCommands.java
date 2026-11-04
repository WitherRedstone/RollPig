package com.chinaex123.roll_pig.command;

import com.chinaex123.roll_pig.RollPig;
import com.chinaex123.roll_pig.api.RollPigApi;
import com.chinaex123.roll_pig.api.RollResult;
import com.chinaex123.roll_pig.data.state.PigRegistry;
import com.chinaex123.roll_pig.event.RPChatQueue;
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
    private static final int UNLOCK_PERMISSION = 2;

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
                                            EntityArgument.getPlayers(context, "targets")))))
                    .then(Commands.literal("unlock")
                            .requires(source -> source.hasPermission(UNLOCK_PERMISSION))
                            // /rollpig unlock —— 解锁自己的全部猪猪
                            .executes(context -> unlock(context,
                                    List.of(context.getSource().getPlayerOrException())))
                            // /rollpig unlock <targets> —— 解锁别人的全部猪猪
                            .then(Commands.argument("targets", EntityArgument.players())
                                    .executes(context -> unlock(context,
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
            Component feedback = Component.translatable("message.roll_pig.force.done",
                    target.getDisplayName(),
                    Component.literal(result.pig().name())).withStyle(ChatFormatting.GRAY);
            // 等图腾动画和猪猪卡片播完再说结果，不然会抢在动画前面
            if (context.getSource().getEntity() instanceof ServerPlayer sender) {
                RPChatQueue.sendLater(sender, feedback, RPChatQueue.DELAY_TICKS);
            } else {
                context.getSource().sendSuccess(() -> feedback, false);
            }
        }
        if (success == 0) {
            context.getSource().sendFailure(Component.translatable("message.roll_pig.force.failed")
                    .withStyle(ChatFormatting.RED));
        }
        return success;
    }

    private static int unlock(CommandContext<CommandSourceStack> context, Collection<ServerPlayer> targets) {
        int total = PigRegistry.all().size();
        int handled = 0;
        for (ServerPlayer target : targets) {
            int unlocked = RollPigApi.unlockAll(target);
            handled++;
            Component feedback = Component.translatable("message.roll_pig.unlock.done",
                    target.getDisplayName(),
                    Component.literal(Integer.toString(unlocked)),
                    Component.literal(Integer.toString(RollPigApi.collected(target))),
                    Component.literal(Integer.toString(total))).withStyle(ChatFormatting.GRAY);
            // 自己执行就自己看；给别人解锁时两边都收到一条
            ServerPlayer sender = context.getSource().getEntity() instanceof ServerPlayer player ? player : null;
            if (sender == null) {
                context.getSource().sendSuccess(() -> feedback, false);
            } else {
                sender.displayClientMessage(feedback, false);
            }
            if (target != sender) {
                target.displayClientMessage(feedback, false);
            }
        }
        return handled;
    }
}
