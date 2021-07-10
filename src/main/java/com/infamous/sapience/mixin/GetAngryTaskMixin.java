package com.infamous.sapience.mixin;

import com.infamous.sapience.util.GeneralHelper;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.brain.BrainUtil;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.GetAngryTask;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GetAngryTask.class)
public class GetAngryTaskMixin {

    @Inject(at = @At("RETURN"), method = "startExecuting")
    private void startExecuting(ServerWorld worldIn, MobEntity entityIn, long gameTimeIn, CallbackInfo ci){
        BrainUtil.getTargetFromMemory(entityIn, MemoryModuleType.ANGRY_AT).ifPresent((target) -> {
            if (GeneralHelper.isOnSameTeam(entityIn, target)) {
                entityIn.getBrain().removeMemory(MemoryModuleType.ANGRY_AT);
            }
        });
    }
}
