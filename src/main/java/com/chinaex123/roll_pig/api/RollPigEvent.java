package com.chinaex123.roll_pig.api;

import com.chinaex123.roll_pig.data.state.Pig;
import com.chinaex123.roll_pig.data.state.RollPigState;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

public class RollPigEvent extends Event {

    private final ServerPlayer player;

    protected RollPigEvent(ServerPlayer player) {
        this.player = player;
    }

    public ServerPlayer getPlayer() {
        return this.player;
    }

    public static final class Pre extends RollPigEvent implements ICancellableEvent {

        public Pre(ServerPlayer player) {
            super(player);
        }
    }

    public static final class Post extends RollPigEvent {

        private final Pig pig;
        private final RollPigState state;

        public Post(ServerPlayer player, Pig pig, RollPigState state) {
            super(player);
            this.pig = pig;
            this.state = state;
        }

        public Pig getPig() {
            return this.pig;
        }

        public RollPigState getState() {
            return this.state;
        }
    }
}
