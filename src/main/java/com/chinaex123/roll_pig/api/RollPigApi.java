package com.chinaex123.roll_pig.api;

import com.chinaex123.roll_pig.util.Pig;
import com.chinaex123.roll_pig.data.state.PigRegistry;
import com.chinaex123.roll_pig.data.state.RollPigState;
import com.chinaex123.roll_pig.init.RPAttachments;
import com.chinaex123.roll_pig.event.RollPigService;
import com.chinaex123.roll_pig.network.PigPenPayload;
import com.chinaex123.roll_pig.network.RPNetwork;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;

public final class RollPigApi {

    private RollPigApi() { }

    public static List<Pig> pigs() {
        return PigRegistry.all();
    }

    public static Optional<Pig> pig(String id) {
        for (Pig pig : PigRegistry.all()) {
            if (pig.id().equals(id)) {
                return Optional.of(pig);
            }
        }
        return Optional.empty();
    }

    public static boolean hasPig(String id) {
        return pig(id).isPresent();
    }

    public static RollPigState state(Player player) {
        return RPAttachments.get(player);
    }

    public static int countOf(Player player, String pigId) {
        return RPAttachments.get(player).countOf(pigId);
    }

    public static int total(Player player) {
        return RPAttachments.get(player).total();
    }

    public static boolean canRoll(Player player) {
        return RPAttachments.get(player).canRoll(currentCycle(player.level()));
    }

    public static long currentCycle(Level level) {
        return RollPigState.cycleOf(level.getDayTime());
    }

    public static long ticksUntilReset(Level level) {
        long remainder = Math.floorMod(level.getDayTime(), 24000L);
        return remainder == 0L ? 24000L : 24000L - remainder;
    }

    public static RollResult roll(ServerPlayer player) {
        return RollPigService.attempt(player);
    }

    public static RollResult rollAndNotify(ServerPlayer player) {
        return RollPigService.attemptAndNotify(player, PigPenPayload.MODE_RESULT);
    }

    public static RollResult forceRoll(ServerPlayer player) {
        return RollPigService.forceRoll(player, PigPenPayload.MODE_RESULT);
    }

    public static void openScreen(ServerPlayer player) {
        RollPigState state = RPAttachments.get(player);
        RPNetwork.sendTo(player, new PigPenPayload(PigPenPayload.MODE_BROWSE,
                state.canRoll(currentCycle(player.level())), "", state.counts()));
    }

    public static void resetDaily(Player player) {
        RPAttachments.set(player, RPAttachments.get(player).withRolledToday(false));
    }

    public static void setCount(Player player, String pigId, int count) {
        RPAttachments.set(player, RPAttachments.get(player).withCount(pigId, count));
    }

    public static void clearCounts(Player player) {
        RPAttachments.set(player, RPAttachments.get(player).withCounts(java.util.Map.of()));
    }
}
