package com.infamous.sapience.tasks;

import com.google.common.collect.ImmutableMap;
import com.infamous.sapience.mod.ModMemoryModuleTypes;
import com.infamous.sapience.util.PiglinTasksHelper;
import net.minecraft.entity.ai.brain.BrainUtil;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.monster.HoglinEntity;
import net.minecraft.entity.monster.piglin.PiglinEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.world.server.ServerWorld;

import java.util.Optional;

public class FeedHoglinsTask<T extends PiglinEntity> extends Task<T> {

    public FeedHoglinsTask() {
        super(ImmutableMap.of(
                ModMemoryModuleTypes.FED_RECENTLY.get(), MemoryModuleStatus.VALUE_ABSENT,
                MemoryModuleType.HUNTED_RECENTLY, MemoryModuleStatus.VALUE_PRESENT,
                ModMemoryModuleTypes.NEAREST_VISIBLE_ADULT_HOGLIN.get(), MemoryModuleStatus.REGISTERED,
                MemoryModuleType.NEAREST_VISIBLE_BABY_HOGLIN, MemoryModuleStatus.REGISTERED,
                MemoryModuleType.ANGRY_AT, MemoryModuleStatus.VALUE_ABSENT));
    }

    @Override
    protected boolean shouldExecute(ServerWorld serverWorld, T owner) {
        return PiglinTasksHelper.hasNotFedRecently(owner);
    }

    @Override
    protected boolean shouldContinueExecuting(ServerWorld serverWorld, T owner, long gameTime) {
        return this.shouldExecute(serverWorld, owner);
    }

    @Override
    protected void startExecuting(ServerWorld serverWorld, T owner, long gameTime) {
        boolean foundAdult = false;
        Optional<HoglinEntity> optionalAdultHoglin = owner.getBrain().getMemory(ModMemoryModuleTypes.NEAREST_VISIBLE_ADULT_HOGLIN.get());
        if(optionalAdultHoglin.isPresent() && optionalAdultHoglin.get().getGrowingAge() == 0){
            foundAdult = true;
            BrainUtil.lookApproachEachOther(owner, optionalAdultHoglin.get(), 0.5F);
        }
        if(!foundAdult){
            Optional<HoglinEntity> optionalBabyHoglin = owner.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_BABY_HOGLIN);
            if(optionalBabyHoglin.isPresent()){
                optionalBabyHoglin.ifPresent(hoglinEntity -> BrainUtil.lookApproachEachOther(owner, hoglinEntity, 0.5F));
            }
        }
    }

    @Override
    protected void updateTask(ServerWorld serverWorld, T owner, long gameTime) {
        boolean foundAdult = false;
        Optional<HoglinEntity> optionalAdultHoglin = owner.getBrain().getMemory(ModMemoryModuleTypes.NEAREST_VISIBLE_ADULT_HOGLIN.get());
        if(optionalAdultHoglin.isPresent() && optionalAdultHoglin.get().getGrowingAge() == 0 && owner.getDistanceSq(optionalAdultHoglin.get()) <= 5.0D){
            foundAdult = true;
            owner.swingArm(Hand.OFF_HAND);
            BrainUtil.spawnItemNearEntity(owner, new ItemStack(Items.CRIMSON_FUNGUS), optionalAdultHoglin.get().getPositionVec());
            PiglinTasksHelper.setFedRecently(owner);
        }
        if(!foundAdult){
            Optional<HoglinEntity> optionalBabyHoglin = owner.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_BABY_HOGLIN);
            if(optionalBabyHoglin.isPresent() && owner.getDistanceSq(optionalBabyHoglin.get()) <= 5.0D){
                owner.swingArm(Hand.OFF_HAND);
                BrainUtil.spawnItemNearEntity(owner, new ItemStack(Items.CRIMSON_FUNGUS), optionalBabyHoglin.get().getPositionVec());
                PiglinTasksHelper.setFedRecently(owner);
            }
        }
    }
}
