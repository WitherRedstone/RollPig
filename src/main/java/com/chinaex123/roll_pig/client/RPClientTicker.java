package com.chinaex123.roll_pig.client;

import com.chinaex123.roll_pig.RollPig;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@EventBusSubscriber(modid = RollPig.MODID, value = Dist.CLIENT)
public final class RPClientTicker {

    private static final List<Delayed> TASKS = new ArrayList<>();

    private RPClientTicker() {
    }

    public static void schedule(int ticks, Runnable action) {
        TASKS.add(new Delayed(ticks, action));
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (TASKS.isEmpty()) {
            return;
        }
        for (Iterator<Delayed> iterator = TASKS.iterator(); iterator.hasNext(); ) {
            Delayed task = iterator.next();
            if (--task.ticksLeft <= 0) {
                iterator.remove();
                task.action.run();
            }
        }
    }

    private static final class Delayed {
        private int ticksLeft;
        private final Runnable action;

        private Delayed(int ticksLeft, Runnable action) {
            this.ticksLeft = ticksLeft;
            this.action = action;
        }
    }
}
