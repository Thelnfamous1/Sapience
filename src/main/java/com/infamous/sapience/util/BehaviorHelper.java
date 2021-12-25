package com.infamous.sapience.util;

import com.infamous.sapience.mixin.AnimalMakeLoveAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.AnimalMakeLove;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.StartAttacking;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Zoglin;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.piglin.RememberIfHoglinWasKilled;
import net.minecraft.world.entity.monster.piglin.StartAdmiringItemIfSeen;
import net.minecraft.world.entity.monster.piglin.StopHoldingItemIfNoLongerAdmiring;

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
            AnimalMakeLoveAccessor accessor = (AnimalMakeLoveAccessor) animalMakeLove;
            Animal partner = accessor.callGetBreedTarget(animal);
            if (entity.closerThan(partner, 3.0D)) {
                if (gameTime < accessor.getSpawnChildAtTime() && entity.getRandom().nextInt(35) == 0) {
                    serverLevel.broadcastEntityEvent(entity, (byte) HoglinTasksHelper.BREEDING_ID);
                    serverLevel.broadcastEntityEvent(partner, (byte) HoglinTasksHelper.BREEDING_ID);
                }
            }
        }
    }

    public static void handleSapienceBehaviorPostStart(Behavior<?> behavior, ServerLevel serverLevel, LivingEntity entity) {
        if(behavior instanceof AnimalMakeLove animalMakeLove && entity instanceof Animal animal){
            Optional<? extends Animal> nearestMate = ((AnimalMakeLoveAccessor)animalMakeLove).callFindValidBreedPartner(animal);
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
        }
        return true;
    }
}
