package com.chinaex123.roll_pig.client;

import com.chinaex123.roll_pig.util.Pig;
import com.chinaex123.roll_pig.data.state.PigRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class ClientPigData {

    private static Map<String, Integer> counts = Map.of();
    private static int version;
    private static int cachedVersion = -1;
    private static List<Pig> unlocked = List.of();

    private ClientPigData() {
    }

    public static void update(Map<String, Integer> next) {
        counts = Map.copyOf(next);
        version++;
    }

    public static int countOf(String pigId) {
        return counts.getOrDefault(pigId, 0);
    }

    public static int unlockedCount() {
        int total = 0;
        for (int value : counts.values()) {
            if (value > 0) {
                total++;
            }
        }
        return total;
    }

    public static List<Pig> unlocked() {
        if (cachedVersion == version) {
            return unlocked;
        }
        List<Pig> list = new ArrayList<>();
        for (Pig pig : PigRegistry.all()) {
            if (countOf(pig.id()) > 0) {
                list.add(pig);
            }
        }
        unlocked = List.copyOf(list);
        cachedVersion = version;
        return unlocked;
    }
}
