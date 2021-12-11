package com.infamous.sapience.tasks;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.infamous.sapience.util.GreedHelper;
import com.infamous.sapience.util.PiglinTasksHelper;
import com.infamous.sapience.util.ReputationHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.item.Item;
import net.minecraft.server.level.ServerLevel;

import java.util.Set;

public class ShareGoldTask<T extends Piglin> extends Behavior<T> {
    private Set<Item> allyDesiredItems = ImmutableSet.of();

    public ShareGoldTask() {
        super(ImmutableMap.of(
                MemoryModuleType.INTERACTION_TARGET, MemoryStatus.VALUE_PRESENT,
                MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryStatus.VALUE_PRESENT));
    }

    protected boolean checkExtraStartConditions(ServerLevel serverWorld, T owner) {
        EntityType<?> entityType = owner.getType();
        boolean isSharingGold = GreedHelper.isSharingGold(owner);
        boolean hasGold = GreedHelper.doesGreedInventoryHaveGold(owner);
        boolean ownerIsAdult = !owner.isBaby();

        boolean hasOpenOffhandSlot = true;
        if(owner.getBrain().getMemory(MemoryModuleType.INTERACTION_TARGET).isPresent()) {
            LivingEntity ally = (LivingEntity) owner.getBrain().getMemory(MemoryModuleType.INTERACTION_TARGET).get();
            if(ally instanceof Piglin && !PiglinTasksHelper.hasOpenOffhandSlot((Piglin) ally)){
                hasOpenOffhandSlot = false;
            }
        }
        return isSharingGold
                && hasGold
                && ownerIsAdult
                && hasOpenOffhandSlot
                && BehaviorUtils.targetIsValid(owner.getBrain(), MemoryModuleType.INTERACTION_TARGET, entityType);
    }

    protected boolean canStillUse(ServerLevel serverWorld, T owner, long gameTime) {
        return this.checkExtraStartConditions(serverWorld, owner);
    }

    protected void start(ServerLevel serverWorld, T owner, long gameTime) {
        if(owner.getBrain().getMemory(MemoryModuleType.INTERACTION_TARGET).isPresent()){
            LivingEntity ally = (LivingEntity)owner.getBrain().getMemory(MemoryModuleType.INTERACTION_TARGET).get();
            BehaviorUtils.lockGazeAndWalkToEachOther(owner, ally, 0.5F);
            this.allyDesiredItems = GreedHelper.getDesiredItems(ally);
        }
    }

    protected void tick(ServerLevel serverWorld, T owner, long gameTime) {
        if(owner.getBrain().getMemory(MemoryModuleType.INTERACTION_TARGET).isPresent()){
            LivingEntity ally = (LivingEntity)owner.getBrain().getMemory(MemoryModuleType.INTERACTION_TARGET).get();
            if (owner.distanceToSqr(ally) <= 5.0D) {
                BehaviorUtils.lockGazeAndWalkToEachOther(owner, ally, 0.5F);
                ReputationHelper.spreadGossip(owner, ally, gameTime); // mimics what villagers do in ShareItemsTask
                if (!this.allyDesiredItems.isEmpty() && ally instanceof Piglin) {
                    GreedHelper.giveAllyDesiredItem(this.allyDesiredItems, owner, (Piglin) ally);
                }
            }
        }
    }

    protected void stop(ServerLevel serverWorld, T owner, long gameTime) {
        owner.getBrain().eraseMemory(MemoryModuleType.INTERACTION_TARGET);
    }

}