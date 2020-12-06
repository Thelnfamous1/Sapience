package com.infamous.sapience.mixin;

import com.infamous.sapience.util.AgeableHelper;
import com.infamous.sapience.util.HoglinTasksHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.AnimalBreedTask;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.Optional;

@Mixin(AnimalBreedTask.class)
public class AnimalBreedTaskMixin {

    @Shadow
    @Final
    private EntityType<? extends AnimalEntity> breedTarget;

    @Shadow
    private long breedTime;

    @Inject(at = @At("HEAD"), method = "startExecuting")
    private void startExecuting(ServerWorld serverWorld, AnimalEntity parent, long gameTime, CallbackInfo callbackInfo){
        Optional<? extends AnimalEntity> nearestMate = this.getNearestMate(parent);
        if(nearestMate.isPresent()){
            AnimalEntity partner = nearestMate.get();
            serverWorld.setEntityState(parent, (byte) AgeableHelper.BREEDING_ID);
            serverWorld.setEntityState(partner, (byte) AgeableHelper.BREEDING_ID);
        }
    }

    @Inject(at = @At("RETURN"), method = "updateTask")
    private void updateTask(ServerWorld serverWorld, AnimalEntity parent, long gameTime, CallbackInfo callbackInfo){
        AnimalEntity partner = this.getBreedTarget(parent);
        if (partner != null && parent.isEntityInRange(partner, 3.0D)) {
            if (gameTime < this.breedTime && parent.getRNG().nextInt(35) == 0) {
                serverWorld.setEntityState(parent, (byte) HoglinTasksHelper.BREEDING_ID);
                serverWorld.setEntityState(partner, (byte) HoglinTasksHelper.BREEDING_ID);
            }
        }
    }

    @Nullable
    private AnimalEntity getBreedTarget(AnimalEntity animal) {
        if(animal.getBrain().getMemory(MemoryModuleType.BREED_TARGET).isPresent()){
            return (AnimalEntity)animal.getBrain().getMemory(MemoryModuleType.BREED_TARGET).get();
        }
        return null;
    }

    private Optional<? extends AnimalEntity> getNearestMate(AnimalEntity animal) {
        if(animal.getBrain().getMemory(MemoryModuleType.VISIBLE_MOBS).isPresent()){
            return animal.getBrain().getMemory(MemoryModuleType.VISIBLE_MOBS)
                    .get()
                    .stream()
                    .filter((livingEntity) -> livingEntity.getType() == this.breedTarget)
                    .map((breedableEntities) -> (AnimalEntity)breedableEntities)
                    .filter(animal::canMateWith)
                    .findFirst();
        }
        else return Optional.empty();
    }
}
