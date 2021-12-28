package com.infamous.sapience.util;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.AnimalMakeLove;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.StartAttacking;
import net.minecraft.world.entity.ai.behavior.StartCelebratingIfTargetDead;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Zoglin;
import net.minecraft.world.entity.monster.piglin.*;
import net.minecraft.world.level.GameRules;

import java.util.Optional;

public class BehaviorHelper {

    public static boolean handleSapienceBehaviorCESC(Behavior<?> behavior, LivingEntity entity, final boolean canStartVanilla) {
        if(behavior instanceof StartAdmiringItemIfSeen<?>){
            Optional<ItemEntity> itemMemory = entity.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM);
            return itemMemory.map(ie -> canStartVanilla || PiglinTasksHelper.isBarterItem(ie.getItem()) || PiglinTasksHelper.isPiglinFoodItem(ie.getItem()))
                    .orElse(canStartVanilla);
        } else if(behavior instanceof StopHoldingItemIfNoLongerAdmiring<?> && entity instanceof AbstractPiglin piglin){
            return canStartVanilla && !PiglinTasksHelper.hasConsumableOffhandItem(piglin);
        } else if(behavior instanceof StartAttacking<?> && entity instanceof Zoglin zoglin){
            return ZoglinTasksHelper.findNearestValidAttackTarget(zoglin).filter(zoglin::canAttack).isPresent();
        } else{
            return canStartVanilla;
        }
    }

    public static void handleSapienceBehaviorPostTick(Behavior<?> behavior, ServerLevel serverLevel, LivingEntity entity, long gameTime) {
        if(behavior instanceof AnimalMakeLove animalMakeLove && entity instanceof Animal animal){
            GeneralHelper.getBreedTarget(animal).ifPresent(
                    a -> {
                        if (entity.closerThan(a, 3.0D)) {
                            if (gameTime < ReflectionHelper.getSpawnChildAtTime(animalMakeLove) && entity.getRandom().nextInt(35) == 0) {
                                serverLevel.broadcastEntityEvent(entity, (byte) HoglinTasksHelper.BREEDING_ID);
                                serverLevel.broadcastEntityEvent(a, (byte) HoglinTasksHelper.BREEDING_ID);
                            }
                        }
                    }
            );
        }
    }

    public static void handleSapienceBehaviorPostStart(Behavior<?> behavior, ServerLevel serverLevel, LivingEntity entity) {
        if(behavior instanceof AnimalMakeLove animalMakeLove && entity instanceof Animal animal){
            Optional<? extends Animal> nearestMate = GeneralHelper.findValidBreedPartner(animal, ReflectionHelper.getPartnerType(animalMakeLove));
            if(nearestMate.isPresent()){
                Animal partner = nearestMate.get();
                serverLevel.broadcastEntityEvent(entity, (byte) AgeableHelper.BREEDING_ID);
                serverLevel.broadcastEntityEvent(partner, (byte) AgeableHelper.BREEDING_ID);
            }
        }
    }

    public static boolean handleSapienceBehaviorPreStart(Behavior<?> behavior, ServerLevel serverLevel, LivingEntity entity) {
        if(behavior instanceof RememberIfHoglinWasKilled<?>){
            entity.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).ifPresent(
                    le -> {
                        if(le.getType().is(PiglinTasksHelper.PIGLINS_HUNT) && le.isDeadOrDying()){
                            entity.getBrain().setMemoryWithExpiry(MemoryModuleType.HUNTED_RECENTLY, true, (long)PiglinTasksHelper.TIME_BETWEEN_HUNTS.sample(entity.level.random));
                        }
                    }
            );
            return false;
        } else if(behavior instanceof StartAttacking<?> && entity instanceof Zoglin zoglin){
            ZoglinTasksHelper.findNearestValidAttackTarget(zoglin).ifPresent(le -> ZoglinTasksHelper.setAttackTarget(zoglin, le));
            return false;
        } else if(behavior instanceof StopHoldingItemIfNoLongerAdmiring<?> && entity instanceof Piglin piglin){
            PiglinTasksHelper.handleStopHoldingOffHandItem(piglin, true);
        } else if(behavior instanceof StartCelebratingIfTargetDead scitd && entity instanceof Piglin piglin){
            GeneralHelper.getAttackTarget(piglin).ifPresent(le -> {
                if (PiglinTasksHelper.wantsToDance(piglin, le)) {
                    piglin.getBrain().setMemoryWithExpiry(MemoryModuleType.DANCING, true, ReflectionHelper.getCelebrateDuration(scitd));
                }

                piglin.getBrain().setMemoryWithExpiry(MemoryModuleType.CELEBRATE_LOCATION, le.blockPosition(), ReflectionHelper.getCelebrateDuration(scitd));
                if (le.getType() != EntityType.PLAYER || serverLevel.getGameRules().getBoolean(GameRules.RULE_FORGIVE_DEAD_PLAYERS)) {
                    piglin.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
                    piglin.getBrain().eraseMemory(MemoryModuleType.ANGRY_AT);
                }
            });
            return false;
        }
        return true;
    }
}
