package com.infamous.sapience;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class SapienceConfig {

    public static class Common {
        public final ForgeConfigSpec.ConfigValue<Integer> MIN_FOOD_VALUE_FOR_BREEDING;
        public final ForgeConfigSpec.ConfigValue<Boolean> PIGLINS_PREFER_CRAFTED_EQUIPMENT;

        public Common(ForgeConfigSpec.Builder builder){

            builder.comment("Ageable Configuration").push("ageable_configuration");

            MIN_FOOD_VALUE_FOR_BREEDING = builder.comment("Minimum total food value for a piglin to breed [0-100, default: 12]")
                    .defineInRange("minFoodValueForBreeding", 12, 0, 100);
            PIGLINS_PREFER_CRAFTED_EQUIPMENT = builder.comment("Determines whether or not a piglin will replace a non-gold piece of equipment with a gold one when crafting equipment [default: true]")
                    .define("piglinsPreferCraftedEquipment", true);

            builder.pop();
        }

    }

    public static final ForgeConfigSpec COMMON_SPEC;
    public static final Common COMMON;

    static {
        final Pair<Common, ForgeConfigSpec> commonSpecPair = new ForgeConfigSpec.Builder().configure(Common::new);
        COMMON_SPEC = commonSpecPair.getRight();
        COMMON = commonSpecPair.getLeft();
    }
}