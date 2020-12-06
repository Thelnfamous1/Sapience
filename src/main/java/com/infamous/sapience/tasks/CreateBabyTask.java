package com.infamous.sapience.tasks;

import com.google.common.collect.ImmutableMap;
import com.infamous.sapience.mod.ModMemoryModuleTypes;
import com.infamous.sapience.util.AgeableHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.BrainUtil;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.world.server.ServerWorld;

import java.util.Optional;

public class CreateBabyTask<T extends MobEntity> extends Task<T> {
    private long duration;

    public CreateBabyTask() {
        super(ImmutableMap.of(
                ModMemoryModuleTypes.BREEDING_TARGET.get(),
                MemoryModuleStatus.VALUE_PRESENT,
                MemoryModuleType.VISIBLE_MOBS,
                MemoryModuleStatus.VALUE_PRESENT),
                350, 350);
    }

    @Override
    protected boolean shouldExecute(ServerWorld worldIn, T owner) {
        return this.canBreed(owner);
    }

    @Override
    protected boolean shouldContinueExecuting(ServerWorld worldIn, T entityIn, long gameTimeIn) {
        return gameTimeIn <= this.duration && this.canBreed(entityIn);
    }

    private boolean canBreed(MobEntity owner) {
        Brain<?> brain = owner.getBrain();
        EntityType<?> ownerEntityType = owner.getType();
        Optional<MobEntity> breedingTarget =
                brain.getMemory(ModMemoryModuleTypes.BREEDING_TARGET.get())
                        .filter((breedTarget) -> breedTarget.getType() == ownerEntityType);
        // CAPABILITY
        return breedingTarget.filter(partner -> AgeableHelper.canPartnersBreed(owner, partner)).isPresent()
                && BrainUtil.isCorrectVisibleType(brain, ModMemoryModuleTypes.BREEDING_TARGET.get(), ownerEntityType);
    }

    @Override
    protected void startExecuting(ServerWorld worldIn, T entityIn, long gameTimeIn) {
        if(entityIn.getBrain().getMemory(ModMemoryModuleTypes.BREEDING_TARGET.get()).isPresent()){
            MobEntity breedTarget = entityIn.getBrain().getMemory(ModMemoryModuleTypes.BREEDING_TARGET.get()).get();

            BrainUtil.lookApproachEachOther(entityIn, breedTarget, 0.5F);
            worldIn.setEntityState(breedTarget, (byte) AgeableHelper.BREEDING_ID);
            worldIn.setEntityState(entityIn, (byte) AgeableHelper.BREEDING_ID);
            //
            int i = 275 + entityIn.getRNG().nextInt(50);
            this.duration = gameTimeIn + (long)i;
        }
    }

    @Override
    protected void updateTask(ServerWorld worldIn, T owner, long gameTime) {
        if(owner.getBrain().getMemory(ModMemoryModuleTypes.BREEDING_TARGET.get()).isPresent()){
            //OhGrowUp.LOGGER.info("Updating CreateBabyTask!");
            MobEntity breedTarget = owner.getBrain().getMemory(ModMemoryModuleTypes.BREEDING_TARGET.get()).get();
            if (owner.getDistanceSq(breedTarget) <= 5.0D) {
                BrainUtil.lookApproachEachOther(owner, breedTarget, 0.5F);
                if (gameTime >= this.duration) {
                    // CAPABILITY
                    AgeableHelper.depleteParentsFoodReserves(owner, breedTarget);
                    //worldIn.setEntityState(owner, (byte)CapabilityHelper.FINISHED_BREEDING_ID);
                    //worldIn.setEntityState(breedTarget, (byte)CapabilityHelper.FINISHED_BREEDING_ID);
                    this.createChild(worldIn, owner, breedTarget);
                } else if(owner.getRNG().nextInt(35) == 0){
                    worldIn.setEntityState(breedTarget, (byte) AgeableHelper.BREEDING_ID);
                    worldIn.setEntityState(owner, (byte) AgeableHelper.BREEDING_ID);
                }
            }
        }
    }

    private void createChild(ServerWorld world, MobEntity parent, MobEntity partner) {
        MobEntity child = AgeableHelper.createChild(world, parent, partner);
        if (child != null) {
            // CAPABILITY
            AgeableHelper.setParentsOnBreedCooldown(parent, partner);
            child.setLocationAndAngles(parent.getPosX(), parent.getPosY(), parent.getPosZ(), 0.0F, 0.0F);
            world.func_242417_l(child);
            world.setEntityState(child, (byte) AgeableHelper.BREEDING_ID);
        }
    }

    protected void resetTask(ServerWorld worldIn, T entityIn, long gameTimeIn) {
        entityIn.getBrain().removeMemory(ModMemoryModuleTypes.BREEDING_TARGET.get());
    }

}