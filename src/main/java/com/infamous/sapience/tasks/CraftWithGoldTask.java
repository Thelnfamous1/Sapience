package com.infamous.sapience.tasks;

import com.google.common.collect.ImmutableMap;
import com.infamous.sapience.util.GreedHelper;
import com.infamous.sapience.util.PiglinTasksHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.monster.piglin.Piglin;

public class CraftWithGoldTask<T extends Piglin> extends Behavior<T> {

    public CraftWithGoldTask() {
        super(ImmutableMap.of(
                MemoryModuleType.ADMIRING_ITEM, MemoryStatus.VALUE_ABSENT));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel serverWorld, T owner) {
        boolean isNotSharingGold = !GreedHelper.isSharingGold(owner);
        boolean hasGold = GreedHelper.doesGreedInventoryHaveGold(owner);
        boolean isAdult = !owner.isBaby();
        boolean isNotAdmiring = PiglinTasksHelper.hasOpenOffhandSlot(owner);
        return isNotSharingGold && hasGold && isAdult && isNotAdmiring;
    }

    @Override
    protected void start(ServerLevel worldIn, T entityIn, long gameTimeIn) {
        GreedHelper.checkCraftEquipment(entityIn);
    }
}
