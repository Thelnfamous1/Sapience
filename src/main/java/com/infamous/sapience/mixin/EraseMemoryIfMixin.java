package com.infamous.sapience.mixin;

import com.infamous.sapience.util.PiglinTasksHelper;
import com.mojang.datafixers.kinds.IdF;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.EraseMemoryIf;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.piglin.Piglin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Predicate;

@Mixin(EraseMemoryIf.class)
public class EraseMemoryIfMixin {

    @Inject(method = "lambda$create$0", at = @At("HEAD"), cancellable = true)
    private static void handleReturn(Predicate<LivingEntity> predicate, MemoryAccessor<IdF.Mu, ?> memory, ServerLevel level, LivingEntity entity, long gameTime, CallbackInfoReturnable<Boolean> cir){
        if (MemoryAccessorAccessor.class.cast(memory).getMemoryType().equals(MemoryModuleType.AVOID_TARGET)
                && entity instanceof Piglin piglin
                && PiglinTasksHelper.wantsToStopFleeing(piglin)) {
            memory.erase();
            cir.setReturnValue(true);
        } else {
            cir.setReturnValue(false);
        }
    }

}
