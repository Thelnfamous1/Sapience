package com.infamous.sapience.mixin;

import com.infamous.sapience.util.BruteTasksHelper;
import com.infamous.sapience.util.HoglinTasksHelper;
import com.infamous.sapience.util.PiglinTasksHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Mixin(Sensor.class)
public class SensorMixin<E extends LivingEntity> {
 
    // Potential Forge PR
    // Allows for applying additional logic for a mob's sensor
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/sensing/Sensor;doTick(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;)V", shift = At.Shift.AFTER), method = "tick")
    private void postDoTick(ServerLevel serverLevel, E entity, CallbackInfo ci){
        if(entity instanceof PiglinBrute brute && this.cast() instanceof PiglinBruteSpecificSensor){
            BruteTasksHelper.additionalSensorLogic(brute);
        } else if(entity instanceof Piglin piglin && this.cast() instanceof PiglinSpecificSensor){
            PiglinTasksHelper.additionalSensorLogic(piglin);
        } else if(entity instanceof Hoglin hoglin && this.cast() instanceof HoglinSpecificSensor){
            HoglinTasksHelper.additionalSensorLogic(hoglin);
        }
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/sensing/Sensor;doTick(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;)V"), method = "tick", cancellable = true)
    private void preDoTick(ServerLevel serverLevel, E entity, CallbackInfo ci){
        if(this.cast() instanceof NearestItemSensor && entity instanceof Hoglin hoglin){
            ci.cancel();
            Brain<?> brain = entity.getBrain();
            List<ItemEntity> nearbyItems = serverLevel.getEntitiesOfClass(ItemEntity.class, entity.getBoundingBox().inflate(8.0D, 4.0D, 8.0D), (ie) -> true);
            nearbyItems.sort(Comparator.comparingDouble(entity::distanceToSqr));
            Optional<ItemEntity> wantedItem = nearbyItems
                    .stream()
                    .filter((ie) -> HoglinTasksHelper.wantsToPickUp(hoglin, ie.getItem()))
                    .filter((ie) -> ie.closerThan(entity, 9.0D))
                    .filter(entity::hasLineOfSight).findFirst();
            brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM, wantedItem);
        }
    }

    private Sensor<?> cast(){
        //noinspection ConstantConditions
        return (Sensor<?>) (Object) this;
    }
}
