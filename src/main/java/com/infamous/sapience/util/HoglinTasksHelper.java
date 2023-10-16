package com.infamous.sapience.util;

import com.google.common.collect.ImmutableList;
import com.infamous.sapience.Sapience;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;

public class HoglinTasksHelper {
    public static final TagKey<Item> HOGLIN_FOOD_ITEMS = ItemTags.create(new ResourceLocation(Sapience.MODID, "hoglin_food_items"));
    public static final int BREEDING_ID = 18;

    private static final UniformInt RETREAT_DURATION = TimeUtil.rangeOfSeconds(5, 20);

    public static boolean canPickUpItemStack(Animal animalEntity, ItemStack itemStack) {
        Item item = itemStack.getItem();
        if (item instanceof BlockItem blockItem && blockItem.getBlock().builtInRegistryHolder().containsTag(BlockTags.HOGLIN_REPELLENTS)) {
            return false;
        } else if (animalEntity.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET)) {
            return false;
        } else {
            if (animalEntity.isFood(itemStack)) {
                return !hasAteRecently(animalEntity);
            }
            else{
                return false;
            }
        }
    }

    public static void pickUpHoglinItem(Animal animalEntity, ItemEntity itemEntity) {
        animalEntity.take(itemEntity, 1);
        ItemStack temptationStack = itemEntity.getItem();
        ItemStack pickupStack = temptationStack.split(1);
        if(temptationStack.isEmpty()){
            itemEntity.discard();
        } else {
            itemEntity.setItem(temptationStack);
        }
        if(!hasAteRecently(animalEntity)){
            setAteRecently(animalEntity);
        }
        int growingAge = animalEntity.getAge();
        Level animalWorld = animalEntity.level();
        if(!animalWorld.isClientSide && growingAge == 0 && !animalEntity.isInLove()){
            animalEntity.setInLove(null);
        }
        else if(!animalWorld.isClientSide && animalEntity.isBaby()){
            animalEntity.ageUp((int)((float)(-growingAge / 20) * 0.1F), true);
        }
    }

    private static boolean hasAteRecently(Animal animalEntity) {
        return animalEntity.getBrain().hasMemoryValue(MemoryModuleType.ATE_RECENTLY);
    }

    public static boolean isHoglinFoodItem(ItemStack item){
        return item.is(HoglinTasksHelper.HOGLIN_FOOD_ITEMS);
    }

    public static void setAteRecently(Animal animalEntity) {
        animalEntity.getBrain().setMemoryWithExpiry(MemoryModuleType.ATE_RECENTLY, true, 200L);
    }

    public static void additionalSensorLogic(Hoglin entity) {
        Brain<?> brain = entity.getBrain();
        Optional<AbstractPiglin> optionalNearestPiglin = Optional.empty();
        int visiblePiglinCount = 0;
        NearestVisibleLivingEntities nvle = brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).orElse(NearestVisibleLivingEntities.empty());
        for(LivingEntity livingEntity : nvle.findAll((le) -> !le.isBaby())) {
            if (livingEntity instanceof AbstractPiglin piglin) {
                ++visiblePiglinCount;
                if (optionalNearestPiglin.isEmpty()) {
                    optionalNearestPiglin = Optional.of(piglin);
                }
            }
        }
        brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLIN, optionalNearestPiglin);
        brain.setMemory(MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT, visiblePiglinCount);

    }

    public static void handleHoglinInteractPost(Hoglin hoglin, Player playerEntity, InteractionHand hand, InteractionResult interactionResult) {
        ItemStack stack = playerEntity.getItemInHand(hand);
        if(interactionResult.consumesAction()){
            if(hoglin.isFood(stack) && !hoglin.level().isClientSide){
                setAteRecently(hoglin);
            }
            else if(hoglin.level().isClientSide){
                hoglin.playSound(SoundEvents.HOGLIN_AMBIENT, 1.0F, hoglin.getVoicePitch());
            }
        }
        else{
            if(hoglin.level().isClientSide){
                hoglin.playSound(SoundEvents.HOGLIN_ANGRY, 1.0F, hoglin.getVoicePitch());
            }
        }
    }

    public static void maybeRetaliate(Hoglin hoglin, LivingEntity attacker) {
        if (!hoglin.getBrain().isActive(Activity.AVOID) || !(attacker instanceof Piglin)) {
            if (!(attacker instanceof Hoglin)) {
                if (!BehaviorUtils.isOtherTargetMuchFurtherAwayThanCurrentAttackTarget(hoglin, attacker, 4.0D)) {
                    if (Sensor.isEntityAttackable(hoglin, attacker)) {
                        setAttackTarget(hoglin, attacker);
                        broadcastAttackTarget(hoglin, attacker);
                    }
                }
            }
        }
    }

    private static void setAttackTarget(Hoglin hoglin, LivingEntity target) {
        Brain<Hoglin> brain = hoglin.getBrain();
        brain.eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
        brain.eraseMemory(MemoryModuleType.BREED_TARGET);
        brain.setMemoryWithExpiry(MemoryModuleType.ATTACK_TARGET, target, 200L);
    }

    private static void broadcastAttackTarget(Hoglin hoglin, LivingEntity target) {
        getVisibleAdultHoglins(hoglin).forEach((h) -> {
            setAttackTargetIfCloserThanCurrent(h, target);
        });
    }

    private static void setAttackTargetIfCloserThanCurrent(Hoglin hoglin, LivingEntity target) {
        if (!isPacified(hoglin)) {
            Optional<LivingEntity> currentTarget = hoglin.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET);
            LivingEntity livingentity = BehaviorUtils.getNearestTarget(hoglin, currentTarget, target);
            setAttackTarget(hoglin, livingentity);
        }
    }

    private static List<Hoglin> getVisibleAdultHoglins(Hoglin hoglin) {
        return hoglin.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_ADULT_HOGLINS).orElse(ImmutableList.of());
    }

    protected static boolean isPacified(Hoglin hoglin) {
        return hoglin.getBrain().hasMemoryValue(MemoryModuleType.PACIFIED);
    }

    public static void onHitTarget(Hoglin hoglin, LivingEntity target) {
        if (!hoglin.isBaby()) {
            if (target instanceof Piglin && piglinsOutnumberHoglins(hoglin)) {
                setAvoidTarget(hoglin, target);
                broadcastRetreat(hoglin, target);
            } else {
                broadcastAttackTarget(hoglin, target);
            }
        }
    }

    private static void broadcastRetreat(Hoglin hoglin, LivingEntity target) {
        getVisibleAdultHoglins(hoglin).forEach((p_34590_) -> {
            retreatFromNearestTarget(p_34590_, target);
        });
    }

    private static void retreatFromNearestTarget(Hoglin hoglin, LivingEntity target) {
        Brain<Hoglin> brain = hoglin.getBrain();
        LivingEntity $$2 = BehaviorUtils.getNearestTarget(hoglin, brain.getMemory(MemoryModuleType.AVOID_TARGET), target);
        $$2 = BehaviorUtils.getNearestTarget(hoglin, brain.getMemory(MemoryModuleType.ATTACK_TARGET), $$2);
        setAvoidTarget(hoglin, $$2);
    }

    private static void setAvoidTarget(Hoglin hoglin, LivingEntity target) {
        hoglin.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
        hoglin.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        hoglin.getBrain().setMemoryWithExpiry(MemoryModuleType.AVOID_TARGET, target, (long)RETREAT_DURATION.sample(hoglin.level().random));
    }

    private static boolean piglinsOutnumberHoglins(Hoglin hoglin) {
        if (hoglin.isBaby()) {
            return false;
        } else {
            int i = hoglin.getBrain().getMemory(MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT).orElse(0);
            int j = hoglin.getBrain().getMemory(MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT).orElse(0) + 1;
            return i > j;
        }
    }

    public static boolean wantsToPickUp(Hoglin hoglin, ItemStack itemStack) {
        return hoglin.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)
                && hoglin.canPickUpLoot()
                && canPickUpItemStack(hoglin, itemStack);
    }
}
