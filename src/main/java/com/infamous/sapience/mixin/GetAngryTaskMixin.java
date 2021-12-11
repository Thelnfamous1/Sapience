package com.infamous.sapience.mixin;

import com.infamous.sapience.util.GeneralHelper;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.behavior.StopBeingAngryIfTargetDead;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StopBeingAngryIfTargetDead.class)
public class GetAngryTaskMixin {

    @Inject(at = @At("RETURN"), method = "start")
    private void startExecuting(ServerLevel worldIn, Mob entityIn, long gameTimeIn, CallbackInfo ci){
        BehaviorUtils.getLivingEntityFromUUIDMemory(entityIn, MemoryModuleType.ANGRY_AT).ifPresent((target) -> {
            if (GeneralHelper.isOnSameTeam(entityIn, target)) {
                entityIn.getBrain().eraseMemory(MemoryModuleType.ANGRY_AT);
            }
        });
    }
}
