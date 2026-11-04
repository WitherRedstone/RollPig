package com.chinaex123.roll_pig.data.state;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public record RollPigState(Map<String, Integer> counts, long lastCycle, boolean rolledToday) {

    public static final RollPigState EMPTY = new RollPigState(Map.of(), Long.MIN_VALUE, false);

    public static final Codec<RollPigState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(Codec.STRING, Codec.INT)
                    .optionalFieldOf("counts", Map.of())
                    .forGetter(RollPigState::counts),
            Codec.LONG
                    .optionalFieldOf("last_cycle", Long.MIN_VALUE)
                    .forGetter(RollPigState::lastCycle),
            Codec.BOOL
                    .optionalFieldOf("rolled_today", false)
                    .forGetter(RollPigState::rolledToday)
    ).apply(instance, RollPigState::new));

    public static long cycleOf(long dayTime) {
        return Math.floorDiv(dayTime, 24000L);
    }

    public int total() {
        int sum = 0;
        for (int count : counts.values()) {
            sum += count;
        }
        return sum;
    }

    public int countOf(String pigId) {
        return counts.getOrDefault(pigId, 0);
    }

    public boolean canRoll(long cycle) {
        return !rolledToday || lastCycle != cycle;
    }

    public RollPigState withRoll(String pigId, long cycle) {
        Map<String, Integer> next = new HashMap<>(counts);
        next.merge(pigId, 1, Integer::sum);
        return new RollPigState(Map.copyOf(next), cycle, true);
    }

    public RollPigState withCount(String pigId, int count) {
        Map<String, Integer> next = new HashMap<>(counts);
        if (count <= 0) {
            next.remove(pigId);
        } else {
            next.put(pigId, count);
        }
        return new RollPigState(Map.copyOf(next), lastCycle, rolledToday);
    }

    public RollPigState withCounts(Map<String, Integer> newCounts) {
        return new RollPigState(Map.copyOf(newCounts), lastCycle, rolledToday);
    }

    public RollPigState withUnlocked(Collection<String> pigIds) {
        Map<String, Integer> next = new HashMap<>(counts);
        for (String pigId : pigIds) {
            next.merge(pigId, 1, Math::max);
        }
        return new RollPigState(Map.copyOf(next), lastCycle, rolledToday);
    }

    public RollPigState withRolledToday(boolean rolled) {
        return new RollPigState(counts, lastCycle, rolled);
    }
}
