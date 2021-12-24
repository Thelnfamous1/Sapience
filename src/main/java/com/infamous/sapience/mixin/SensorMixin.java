package com.infamous.sapience.mixin;

import com.infamous.sapience.util.BruteTasksHelper;
import com.infamous.sapience.util.HoglinTasksHelper;
import com.infamous.sapience.util.PiglinTasksHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Sensor.class)
public class SensorMixin<E extends LivingEntity> {
 
    // Potential Forge PR
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/sensing/Sensor;doTick(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;)V", shift = At.Shift.AFTER), method = "tick")
    private void postDoTick(ServerLevel serverLevel, E entity, CallbackInfo ci){
        if(entity instanceof PiglinBrute brute){
            BruteTasksHelper.additionalSensorLogic(brute);
        } else if(entity instanceof Piglin piglin){
            PiglinTasksHelper.additionalSensorLogic(piglin);
        } else if(entity instanceof Hoglin hoglin){
            HoglinTasksHelper.additionalSensorLogic(hoglin);
        }
    }
}
