package com.infamous.sapience.tasks;

import com.google.common.collect.ImmutableMap;
import com.infamous.sapience.util.AgeableHelper;
import com.infamous.sapience.util.GreedHelper;
import com.infamous.sapience.util.PiglinTasksHelper;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.monster.piglin.PiglinEntity;
import net.minecraft.util.Hand;
import net.minecraft.world.server.ServerWorld;

public class CraftWithGoldTask<T extends PiglinEntity> extends Task<T> {

    public CraftWithGoldTask() {
        super(ImmutableMap.of(
                MemoryModuleType.ADMIRING_ITEM, MemoryModuleStatus.VALUE_ABSENT));
    }

    @Override
    protected boolean shouldExecute(ServerWorld serverWorld, T owner) {
        boolean isNotSharingGold = !GreedHelper.isSharingGold(owner);
        boolean hasGold = GreedHelper.doesGreedInventoryHaveGold(owner);
        boolean isAdult = !owner.isChild();
        boolean isNotAdmiring = PiglinTasksHelper.hasOpenOffhandSlot(owner);
        return isNotSharingGold && hasGold && isAdult && isNotAdmiring;
    }

    @Override
    protected void startExecuting(ServerWorld worldIn, T entityIn, long gameTimeIn) {
        GreedHelper.checkCraftEquipment(entityIn);
    }
}
