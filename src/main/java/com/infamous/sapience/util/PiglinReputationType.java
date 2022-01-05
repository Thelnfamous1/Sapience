package com.infamous.sapience.util;

import net.minecraft.world.entity.ai.village.ReputationEventType;

public class PiglinReputationType implements ReputationEventType {
    //public static final IReputationType ZOMBIFIED_PIGLIN_CURED = IReputationType.register("zombified_piglin_cured");
    public static final ReputationEventType WITHER_KILLED = ReputationEventType.register("wither_killed");
    public static final ReputationEventType WITHER_SKELETON_KILLED = ReputationEventType.register("wither_skeleton_killed");
    public static final ReputationEventType FOOD_GIFT = ReputationEventType.register("food_gift");
    public static final ReputationEventType GOLD_GIFT = ReputationEventType.register("gold_gift");
    public static final ReputationEventType BARTER = ReputationEventType.register("barter");
    public static final ReputationEventType ADULT_PIGLIN_HURT = ReputationEventType.register("adult_piglin_hurt");
    public static final ReputationEventType BABY_PIGLIN_HURT = ReputationEventType.register("baby_piglin_hurt");
    public static final ReputationEventType GOLD_STOLEN = ReputationEventType.register("gold_stolen");
    public static final ReputationEventType ADULT_PIGLIN_KILLED = ReputationEventType.register("adult_piglin_killed");
    public static final ReputationEventType BABY_PIGLIN_KILLED = ReputationEventType.register("baby_piglin_killed");
    public static final ReputationEventType BRUTE_KILLED = ReputationEventType.register("brute_killed");
    public static final ReputationEventType ALLY_HURT = ReputationEventType.register("ally_hurt");
    public static final ReputationEventType ALLY_KILLED = ReputationEventType.register("ally_killed");

}
