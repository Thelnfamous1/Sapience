package com.infamous.sapience.util;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.piglin.Piglin;

import java.util.Optional;

public class BehaviorHelper {

    public static boolean canStopHoldingItemIfNoLongerAdmiring(boolean canStartVanilla, AbstractPiglin piglin) {
        return canStartVanilla && !PiglinTasksHelper.hasConsumableOffhandItem(piglin);
    }

    public static boolean canStartAdmiring(LivingEntity entity, boolean canStartVanilla) {
        Optional<ItemEntity> itemMemory = entity.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM);
        return itemMemory.map(ie -> canStartVanilla || PiglinTasksHelper.isBarterItem(ie.getItem()) || PiglinTasksHelper.isPiglinFoodItem(ie.getItem()))
                .orElse(canStartVanilla);
    }

    public static void handleWantsToDance(Piglin piglin, LivingEntity le, int duration) {
        if (PiglinTasksHelper.wantsToDance(piglin, le)) {
            piglin.getBrain().setMemoryWithExpiry(MemoryModuleType.DANCING, true, duration);
        }
    }

    public static void handleHuntTargetIfKilled(LivingEntity entity) {
        entity.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).ifPresent(
                le -> {
                    if(le.getType().is(PiglinTasksHelper.PIGLINS_HUNT) && le.isDeadOrDying()){
                        entity.getBrain().setMemoryWithExpiry(MemoryModuleType.HUNTED_RECENTLY, true, PiglinTasksHelper.TIME_BETWEEN_HUNTS.sample(entity.level().random));
                    }
                }
        );
    }
}
