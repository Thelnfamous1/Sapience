package com.infamous.sapience.tasks;

import com.google.common.collect.ImmutableMap;
import com.infamous.sapience.mod.ModMemoryModuleTypes;
import com.infamous.sapience.util.AgeableHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.server.level.ServerLevel;

import java.util.Optional;

public class CreateBabyTask<T extends Mob> extends Behavior<T> {
    private long duration;

    public CreateBabyTask() {
        super(ImmutableMap.of(
                ModMemoryModuleTypes.BREEDING_TARGET.get(),
                MemoryStatus.VALUE_PRESENT,
                MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
                MemoryStatus.VALUE_PRESENT),
                350, 350);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel worldIn, T owner) {
        return this.canBreed(owner);
    }

    @Override
    protected boolean canStillUse(ServerLevel worldIn, T entityIn, long gameTimeIn) {
        return gameTimeIn <= this.duration && this.canBreed(entityIn);
    }

    private boolean canBreed(Mob owner) {
        Brain<?> brain = owner.getBrain();
        EntityType<?> ownerEntityType = owner.getType();
        Optional<Mob> breedingTarget =
                brain.getMemory(ModMemoryModuleTypes.BREEDING_TARGET.get())
                        .filter((breedTarget) -> breedTarget.getType() == ownerEntityType);
        // CAPABILITY
        return breedingTarget.filter(partner -> AgeableHelper.canPartnersBreed(owner, partner)).isPresent()
                && BehaviorUtils.targetIsValid(brain, ModMemoryModuleTypes.BREEDING_TARGET.get(), ownerEntityType);
    }

    @Override
    protected void start(ServerLevel worldIn, T entityIn, long gameTimeIn) {
        if(entityIn.getBrain().getMemory(ModMemoryModuleTypes.BREEDING_TARGET.get()).isPresent()){
            Mob breedTarget = entityIn.getBrain().getMemory(ModMemoryModuleTypes.BREEDING_TARGET.get()).get();

            BehaviorUtils.lockGazeAndWalkToEachOther(entityIn, breedTarget, 0.5F);
            worldIn.broadcastEntityEvent(breedTarget, (byte) AgeableHelper.BREEDING_ID);
            worldIn.broadcastEntityEvent(entityIn, (byte) AgeableHelper.BREEDING_ID);
            //
            int i = 275 + entityIn.getRandom().nextInt(50);
            this.duration = gameTimeIn + (long)i;
        }
    }

    @Override
    protected void tick(ServerLevel worldIn, T owner, long gameTime) {
        if(owner.getBrain().getMemory(ModMemoryModuleTypes.BREEDING_TARGET.get()).isPresent()){
            //OhGrowUp.LOGGER.info("Updating CreateBabyTask!");
            Mob breedTarget = owner.getBrain().getMemory(ModMemoryModuleTypes.BREEDING_TARGET.get()).get();
            if (owner.distanceToSqr(breedTarget) <= 5.0D) {
                BehaviorUtils.lockGazeAndWalkToEachOther(owner, breedTarget, 0.5F);
                if (gameTime >= this.duration) {
                    // CAPABILITY
                    AgeableHelper.depleteParentsFoodValue(owner, breedTarget);
                    this.createChild(worldIn, owner, breedTarget);
                } else if(owner.getRandom().nextInt(35) == 0){
                    worldIn.broadcastEntityEvent(breedTarget, (byte) AgeableHelper.BREEDING_ID);
                    worldIn.broadcastEntityEvent(owner, (byte) AgeableHelper.BREEDING_ID);
                }
            }
        }
    }

    private void createChild(ServerLevel world, Mob parent, Mob partner) {
        Mob child = AgeableHelper.createChild(world, parent, partner);
        if (child != null) {
            // CAPABILITY
            AgeableHelper.setParentsOnBreedCooldown(parent, partner);
            child.moveTo(parent.getX(), parent.getY(), parent.getZ(), 0.0F, 0.0F);
            world.addFreshEntityWithPassengers(child);
            world.broadcastEntityEvent(child, (byte) AgeableHelper.BREEDING_ID);
        }
    }

    protected void stop(ServerLevel worldIn, T entityIn, long gameTimeIn) {
        entityIn.getBrain().eraseMemory(ModMemoryModuleTypes.BREEDING_TARGET.get());
    }

}