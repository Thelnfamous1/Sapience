package com.infamous.sapience.tasks;

import com.google.common.collect.ImmutableMap;
import com.infamous.sapience.mod.ModMemoryModuleTypes;
import com.infamous.sapience.util.HoglinTasksHelper;
import com.infamous.sapience.util.PiglinTasksHelper;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.InteractionHand;
import net.minecraft.server.level.ServerLevel;

import java.util.Optional;

public class FeedHoglinsTask<T extends Piglin> extends Behavior<T> {

    public FeedHoglinsTask() {
        super(ImmutableMap.of(
                ModMemoryModuleTypes.FED_RECENTLY.get(), MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.HUNTED_RECENTLY, MemoryStatus.VALUE_PRESENT,
                ModMemoryModuleTypes.NEAREST_VISIBLE_ADULT_HOGLIN.get(), MemoryStatus.REGISTERED,
                MemoryModuleType.NEAREST_VISIBLE_BABY_HOGLIN, MemoryStatus.REGISTERED,
                MemoryModuleType.ANGRY_AT, MemoryStatus.VALUE_ABSENT));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel serverWorld, T owner) {
        return PiglinTasksHelper.hasNotFedRecently(owner) && Items.CRIMSON_FUNGUS.m_204114_().m_203656_(HoglinTasksHelper.HOGLIN_FOOD_ITEMS);
    }

    @Override
    protected boolean canStillUse(ServerLevel serverWorld, T owner, long gameTime) {
        return this.checkExtraStartConditions(serverWorld, owner);
    }

    @Override
    protected void start(ServerLevel serverWorld, T owner, long gameTime) {
        boolean foundAdult = false;
        Optional<Hoglin> optionalAdultHoglin = owner.getBrain().getMemory(ModMemoryModuleTypes.NEAREST_VISIBLE_ADULT_HOGLIN.get());
        if(optionalAdultHoglin.isPresent() && optionalAdultHoglin.get().getAge() == 0){
            foundAdult = true;
            BehaviorUtils.lockGazeAndWalkToEachOther(owner, optionalAdultHoglin.get(), 0.5F);
        }
        if(!foundAdult){
            Optional<Hoglin> optionalBabyHoglin = owner.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_BABY_HOGLIN);
            if(optionalBabyHoglin.isPresent()){
                optionalBabyHoglin.ifPresent(hoglinEntity -> BehaviorUtils.lockGazeAndWalkToEachOther(owner, hoglinEntity, 0.5F));
            }
        }
    }

    @Override
    protected void tick(ServerLevel serverWorld, T owner, long gameTime) {
        boolean foundAdult = false;
        Optional<Hoglin> optionalAdultHoglin = owner.getBrain().getMemory(ModMemoryModuleTypes.NEAREST_VISIBLE_ADULT_HOGLIN.get());
        if(optionalAdultHoglin.isPresent() && optionalAdultHoglin.get().getAge() == 0 && owner.distanceToSqr(optionalAdultHoglin.get()) <= 5.0D){
            foundAdult = true;
            owner.swing(InteractionHand.OFF_HAND);
            BehaviorUtils.throwItem(owner, new ItemStack(Items.CRIMSON_FUNGUS), optionalAdultHoglin.get().position());
            PiglinTasksHelper.setFedRecently(owner);
        }
        if(!foundAdult){
            Optional<Hoglin> optionalBabyHoglin = owner.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_BABY_HOGLIN);
            if(optionalBabyHoglin.isPresent() && owner.distanceToSqr(optionalBabyHoglin.get()) <= 5.0D){
                owner.swing(InteractionHand.OFF_HAND);
                BehaviorUtils.throwItem(owner, new ItemStack(Items.CRIMSON_FUNGUS), optionalBabyHoglin.get().position());
                PiglinTasksHelper.setFedRecently(owner);
            }
        }
    }
}
