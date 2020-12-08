package com.infamous.sapience.tasks;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.infamous.sapience.util.GreedHelper;
import com.infamous.sapience.util.PiglinTasksHelper;
import com.infamous.sapience.util.ReputationHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.BrainUtil;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.monster.piglin.PiglinEntity;
import net.minecraft.item.Item;
import net.minecraft.world.server.ServerWorld;

import java.util.Set;

public class ShareGoldTask<T extends PiglinEntity> extends Task<T> {
    private Set<Item> allyDesiredItems = ImmutableSet.of();

    public ShareGoldTask() {
        super(ImmutableMap.of(
                MemoryModuleType.INTERACTION_TARGET, MemoryModuleStatus.VALUE_PRESENT,
                MemoryModuleType.VISIBLE_MOBS, MemoryModuleStatus.VALUE_PRESENT));
    }

    protected boolean shouldExecute(ServerWorld serverWorld, T owner) {
        EntityType<?> entityType = owner.getType();
        boolean isSharingGold = GreedHelper.isSharingGold(owner);
        boolean hasGold = GreedHelper.doesGreedInventoryHaveGold(owner);
        boolean ownerIsAdult = !owner.isChild();

        boolean hasOpenOffhandSlot = true;
        if(owner.getBrain().getMemory(MemoryModuleType.INTERACTION_TARGET).isPresent()) {
            LivingEntity ally = (LivingEntity) owner.getBrain().getMemory(MemoryModuleType.INTERACTION_TARGET).get();
            if(ally instanceof PiglinEntity && !PiglinTasksHelper.hasOpenOffhandSlot((PiglinEntity) ally)){
                hasOpenOffhandSlot = false;
            }
        }
        return isSharingGold
                && hasGold
                && ownerIsAdult
                && hasOpenOffhandSlot
                && BrainUtil.isCorrectVisibleType(owner.getBrain(), MemoryModuleType.INTERACTION_TARGET, entityType);
    }

    protected boolean shouldContinueExecuting(ServerWorld serverWorld, T owner, long gameTime) {
        return this.shouldExecute(serverWorld, owner);
    }

    protected void startExecuting(ServerWorld serverWorld, T owner, long gameTime) {
        if(owner.getBrain().getMemory(MemoryModuleType.INTERACTION_TARGET).isPresent()){
            LivingEntity ally = (LivingEntity)owner.getBrain().getMemory(MemoryModuleType.INTERACTION_TARGET).get();
            BrainUtil.lookApproachEachOther(owner, ally, 0.5F);
            this.allyDesiredItems = GreedHelper.getDesiredItems(ally);
        }
    }

    protected void updateTask(ServerWorld serverWorld, T owner, long gameTime) {
        if(owner.getBrain().getMemory(MemoryModuleType.INTERACTION_TARGET).isPresent()){
            LivingEntity ally = (LivingEntity)owner.getBrain().getMemory(MemoryModuleType.INTERACTION_TARGET).get();
            if (owner.getDistanceSq(ally) <= 5.0D) {
                BrainUtil.lookApproachEachOther(owner, ally, 0.5F);
                ReputationHelper.spreadGossip(owner, ally, gameTime); // mimics what villagers do in ShareItemsTask
                if (!this.allyDesiredItems.isEmpty() && ally instanceof PiglinEntity) {
                    GreedHelper.giveAllyDesiredItem(this.allyDesiredItems, owner, (PiglinEntity) ally);
                }
            }
        }
    }

    protected void resetTask(ServerWorld serverWorld, T owner, long gameTime) {
        owner.getBrain().removeMemory(MemoryModuleType.INTERACTION_TARGET);
    }

}