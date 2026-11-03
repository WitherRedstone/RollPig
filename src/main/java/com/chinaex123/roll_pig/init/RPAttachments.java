package com.chinaex123.roll_pig.init;

import com.chinaex123.roll_pig.RollPig;
import com.chinaex123.roll_pig.data.state.RollPigState;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public final class RPAttachments {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, RollPig.MODID);

    public static final Supplier<AttachmentType<RollPigState>> ROLL_STATE =
            ATTACHMENT_TYPES.register("roll_state", () -> AttachmentType.builder(() -> RollPigState.EMPTY)
                    .serialize(RollPigState.CODEC)
                    .copyOnDeath()
                    .build());

    private RPAttachments() { }

    public static RollPigState get(Player player) {
        return player.getData(ROLL_STATE.get());
    }

    public static void set(Player player, RollPigState state) {
        player.setData(ROLL_STATE.get(), state);
    }
}
