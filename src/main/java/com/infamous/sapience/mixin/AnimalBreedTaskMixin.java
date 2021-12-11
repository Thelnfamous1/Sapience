package com.infamous.sapience.mixin;

import com.infamous.sapience.util.AgeableHelper;
import com.infamous.sapience.util.HoglinTasksHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.behavior.AnimalMakeLove;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.Optional;

@Mixin(AnimalMakeLove.class)
public class AnimalBreedTaskMixin {

    @Shadow
    @Final
    private EntityType<? extends Animal> breedTarget;

    @Shadow
    private long breedTime;

    @Inject(at = @At("HEAD"), method = "start")
    private void startExecuting(ServerLevel serverWorld, Animal parent, long gameTime, CallbackInfo callbackInfo){
        Optional<? extends Animal> nearestMate = this.getNearestMate(parent);
        if(nearestMate.isPresent()){
            Animal partner = nearestMate.get();
            serverWorld.broadcastEntityEvent(parent, (byte) AgeableHelper.BREEDING_ID);
            serverWorld.broadcastEntityEvent(partner, (byte) AgeableHelper.BREEDING_ID);
        }
    }

    @Inject(at = @At("RETURN"), method = "tick")
    private void updateTask(ServerLevel serverWorld, Animal parent, long gameTime, CallbackInfo callbackInfo){
        Animal partner = this.getBreedTarget(parent);
        if (partner != null && parent.closerThan(partner, 3.0D)) {
            if (gameTime < this.breedTime && parent.getRandom().nextInt(35) == 0) {
                serverWorld.broadcastEntityEvent(parent, (byte) HoglinTasksHelper.BREEDING_ID);
                serverWorld.broadcastEntityEvent(partner, (byte) HoglinTasksHelper.BREEDING_ID);
            }
        }
    }

    @Nullable
    private Animal getBreedTarget(Animal animal) {
        if(animal.getBrain().getMemory(MemoryModuleType.BREED_TARGET).isPresent()){
            return (Animal)animal.getBrain().getMemory(MemoryModuleType.BREED_TARGET).get();
        }
        return null;
    }

    private Optional<? extends Animal> getNearestMate(Animal animal) {
        if(animal.getBrain().getMemory(MemoryModuleType.VISIBLE_LIVING_ENTITIES).isPresent()){
            return animal.getBrain().getMemory(MemoryModuleType.VISIBLE_LIVING_ENTITIES)
                    .get()
                    .stream()
                    .filter((livingEntity) -> livingEntity.getType() == this.breedTarget)
                    .map((breedableEntities) -> (Animal)breedableEntities)
                    .filter(animal::canMate)
                    .findFirst();
        }
        else return Optional.empty();
    }
}
