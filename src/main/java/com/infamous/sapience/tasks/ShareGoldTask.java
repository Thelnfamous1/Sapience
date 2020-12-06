package com.infamous.sapience.tasks;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.infamous.sapience.util.GreedHelper;
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
        boolean isAdult = !owner.isChild();
        return isSharingGold && hasGold && BrainUtil.isCorrectVisibleType(owner.getBrain(), MemoryModuleType.INTERACTION_TARGET, entityType) && isAdult;
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
                //owner.func_242368_a(serverWorld, ally, gameTime);
                if (!this.allyDesiredItems.isEmpty()) {
                    GreedHelper.giveAllyDesiredItem(this.allyDesiredItems, owner, ally);
                }
            }
        }
    }

    protected void resetTask(ServerWorld serverWorld, T owner, long gameTime) {
        owner.getBrain().removeMemory(MemoryModuleType.INTERACTION_TARGET);
    }

}