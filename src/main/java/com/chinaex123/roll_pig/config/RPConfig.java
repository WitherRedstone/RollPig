package com.chinaex123.roll_pig.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class RPConfig {
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.BooleanValue ANNOUNCE_NEW_DAY;
    public static final ModConfigSpec.BooleanValue BROADCAST_ROLL;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.comment("通用配置").push("General settings");
        ANNOUNCE_NEW_DAY = builder
                .comment("天亮时在聊天栏提示玩家可以抽猪猪")
                .comment("Announce in chat when a new day starts and rolls are available again")
                .define("announce_new_day", true);
        BROADCAST_ROLL = builder
                .comment("有玩家抽到猪猪时向全服广播")
                .comment("Broadcast every successful roll to the whole server")
                .define("broadcast_roll", true);
        builder.pop();

        SPEC = builder.build();
    }
}
