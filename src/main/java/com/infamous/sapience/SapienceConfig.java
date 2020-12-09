package com.infamous.sapience;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class SapienceConfig {

    public static class Common {
        public final ForgeConfigSpec.ConfigValue<Boolean> REQUIRE_LIVING_FOR_BARTER;

        public final ForgeConfigSpec.ConfigValue<Integer> MIN_FOOD_VALUE_FOR_BREEDING;

        public final ForgeConfigSpec.ConfigValue<Boolean> PIGLINS_PREFER_CRAFTED_EQUIPMENT;

        public final ForgeConfigSpec.ConfigValue<Integer> WITHER_KILLED_GOSSIP_VALUE;
        public final ForgeConfigSpec.ConfigValue<Integer> WITHER_KILLED_BONUS_GOSSIP_VALUE;

        public final ForgeConfigSpec.ConfigValue<Integer> WITHER_SKELETON_KILLED_GOSSIP_VALUE;
        public final ForgeConfigSpec.ConfigValue<Integer> WITHER_SKELETON_KILLED_BONUS_GOSSIP_VALUE;

        public final ForgeConfigSpec.ConfigValue<Integer> GOLD_GIFT_GOSSIP_VALUE;
        public final ForgeConfigSpec.ConfigValue<Integer> FOOD_GIFT_GOSSIP_VALUE;
        public final ForgeConfigSpec.ConfigValue<Integer> BARTER_GOSSIP_VALUE;

        public final ForgeConfigSpec.ConfigValue<Integer> GOLD_STOLEN_GOSSIP_VALUE;
        public final ForgeConfigSpec.ConfigValue<Integer> ADULT_PIGLIN_HURT_GOSSIP_VALUE;
        public final ForgeConfigSpec.ConfigValue<Integer> BABY_PIGLIN_HURT_GOSSIP_VALUE;

        public final ForgeConfigSpec.ConfigValue<Integer> ADULT_PIGLIN_KILLED_GOSSIP_VALUE;
        public final ForgeConfigSpec.ConfigValue<Integer> ADULT_PIGLIN_KILLED_BONUS_GOSSIP_VALUE;

        public final ForgeConfigSpec.ConfigValue<Integer> BABY_PIGLIN_KILLED_GOSSIP_VALUE;
        public final ForgeConfigSpec.ConfigValue<Integer> BABY_PIGLIN_KILLED_BONUS_GOSSIP_VALUE;

        public final ForgeConfigSpec.ConfigValue<Integer> BRUTE_KILLED_GOSSIP_VALUE;
        public final ForgeConfigSpec.ConfigValue<Integer> BRUTE_KILLED_BONUS_GOSSIP_VALUE;

        public final ForgeConfigSpec.ConfigValue<Integer> ALLY_GOSSIP_REQUIREMENT;
        public final ForgeConfigSpec.ConfigValue<Integer> FRIENDLY_GOSSIP_REQUIREMENT;
        public final ForgeConfigSpec.ConfigValue<Integer> UNFRIENDLY_GOSSIP_REQUIREMENT;
        public final ForgeConfigSpec.ConfigValue<Integer> ENEMY_GOSSIP_REQUIREMENT;


        public Common(ForgeConfigSpec.Builder builder){

            builder.comment("General Configuration").push("general_configuration");

            REQUIRE_LIVING_FOR_BARTER = builder.comment("Determines whether or not Piglins can only give bartering loot if the received bartering item came from a living being [true/false, default:true]")
                    .define("requireLivingForBarter", true);

            builder.pop();

            builder.comment("Ageable Configuration").push("ageable_configuration");

            MIN_FOOD_VALUE_FOR_BREEDING = builder.comment("Minimum total food value for a piglin to breed [0-100, default: 12]")
                    .defineInRange("minFoodValueForBreeding", 12, 0, 100);

            builder.pop();

            builder.comment("Greed Configuration").push("greed_configuration");

            PIGLINS_PREFER_CRAFTED_EQUIPMENT = builder.comment("Determines whether or not a piglin will replace a non-gold piece of equipment with a gold one when crafting equipment [default: true]")
                    .define("piglinsPreferCraftedEquipment", true);

            builder.pop();

            builder.comment("Positive Reputation Configuration").push("positive_reputation_configuration");

            // EXTREMELY MAJOR POSITIVE
            WITHER_KILLED_GOSSIP_VALUE = builder.comment("The amount of major positive gossip received from Piglins that have seen you kill the Wither [0-100, default: 100]")
                    .defineInRange("witherKilledRepAmount", 100, 0, 100);

            WITHER_KILLED_BONUS_GOSSIP_VALUE = builder.comment("The amount of additional minor positive gossip received from Piglins that have seen you kill the Wither [0-200, default: 200]")
                    .defineInRange("witherKilledRepBonus", 200, 0, 200);

            // MAJOR POSITIVE
            WITHER_SKELETON_KILLED_GOSSIP_VALUE = builder.comment("The amount of major positive gossip received from Piglins that have seen you kill Wither Skeletons [0-100, default: 25]")
                    .defineInRange("witherSkeletonRepAmount", 25, 0, 100);

            WITHER_SKELETON_KILLED_BONUS_GOSSIP_VALUE = builder.comment("The amount of additional minor positive gossip received from Piglins that have seen you kill Wither Skeletons [0-200, default: 25]")
                    .defineInRange("witherSkeletonRepBonus", 25, 0, 200);

            // MINOR POSITIVE
            GOLD_GIFT_GOSSIP_VALUE = builder.comment("The amount of minor positive gossip received from giving a non-bartering gold item to a Piglin [0-200, default: 12]")
                    .defineInRange("goldGiftRepAmount", 12, 0, 200);

            FOOD_GIFT_GOSSIP_VALUE = builder.comment("The amount of minor positive gossip received from giving a food item to a Piglin [0-200, default: 6]")
                    .defineInRange("foodGiftRepAmount", 6, 0, 200);

            BARTER_GOSSIP_VALUE = builder.comment("The amount of minor positive gossip received from bartering with a Piglin [0-200, default: 3]")
                    .defineInRange("barterRepAmount", 3, 0, 200);

            // REP REQUIREMENTS
            ALLY_GOSSIP_REQUIREMENT = builder.comment("The minimum gossip requirement to be considered an ally of a Piglin. Allows you to open containers and mine gold around them [0-300, default: 100]")
                    .defineInRange("allyGossipRequirement", 100, 0, 300);

            FRIENDLY_GOSSIP_REQUIREMENT = builder.comment("The minimum gossip requirement to be considered friendly with a Piglin. Allows you to not have to wear gold around them [0-300, default: 50]")
                    .defineInRange("friendlyGossipRequirement", 50, 0, 300);

            builder.pop();

            builder.comment("Negative Reputation Configuration").push("negative_reputation_configuration");

            // MINOR NEGATIVE

            GOLD_STOLEN_GOSSIP_VALUE = builder.comment("The amount of minor negative gossip received from Piglins who catch you stealing their gold [0-200, default: 25]")
                    .defineInRange("goldStolenRepAmount", 12, 0, 200);
            
            ADULT_PIGLIN_HURT_GOSSIP_VALUE = builder.comment("The amount of minor negative gossip received from hurting an adult Piglin [0-200, default: 12]")
                    .defineInRange("adultPiglinHurtRepAmount", 12, 0, 200);

            BABY_PIGLIN_HURT_GOSSIP_VALUE = builder.comment("The amount of minor negative gossip received from hurting a baby Piglin [0-200, default: 25]")
                    .defineInRange("babyPiglinHurtRepAmount", 25, 0, 200);

            // MAJOR NEGATIVE
            ADULT_PIGLIN_KILLED_GOSSIP_VALUE = builder.comment("The amount of major negative gossip received from Piglins that have seen you kill an adult Piglin [0-100, default: 25]")
                    .defineInRange("adultPiglinKilledRepAmount", 25, 0, 100);

            ADULT_PIGLIN_KILLED_BONUS_GOSSIP_VALUE = builder.comment("The amount of additional minor negative gossip received from Piglins that have seen you kill an adult Piglin [0-200, default: 0]")
                    .defineInRange("adultPiglinKilledRepBonus", 0, 0, 200);

            BABY_PIGLIN_KILLED_GOSSIP_VALUE = builder.comment("The amount of major negative gossip received from Piglins that have seen you kill a baby Piglin [0-100, default: 25]")
                    .defineInRange("babyPiglinKilledRepAmount", 25, 0, 100);

            BABY_PIGLIN_KILLED_BONUS_GOSSIP_VALUE = builder.comment("The amount of additional minor negative gossip received from Piglins that have seen you kill a baby Piglin [0-200, default: 25]")
                    .defineInRange("babyPiglinKilledRepBonus", 25, 0, 200);

            // EXTREMELY MAJOR NEGATIVE
            BRUTE_KILLED_GOSSIP_VALUE = builder.comment("The amount of major negative gossip received from Piglins that have seen you kill a Piglin Brute [0-100, default: 50]")
                    .defineInRange("bruteKilledRepAmount", 50, 0, 100);

            BRUTE_KILLED_BONUS_GOSSIP_VALUE = builder.comment("The amount of additional minor negative gossip received from Piglins that have seen you kill a Piglin Brute [0-200, default: 50]")
                    .defineInRange("bruteKilledRepBonus", 50, 0, 200);

            // REP REQUIREMENTS
            UNFRIENDLY_GOSSIP_REQUIREMENT = builder.comment("The maximum gossip requirement to be considered unfriendly with a Piglin. Prevents you from receiving any bartering loot [-300-0, default: -50]")
                    .defineInRange("unfriendlyGossipRequirement", -50, -300, 0);

            ENEMY_GOSSIP_REQUIREMENT = builder.comment("The maximum gossip requirement to be considered an enemy of a Piglin. Prevents you from making them neutral by wearing gold armor [-300-0, default: -100]")
                    .defineInRange("enemyGossipRequirement", -100, -300, 0);

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