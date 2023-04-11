package com.infamous.sapience.mixin;

import com.infamous.sapience.util.BehaviorHelper;
import com.mojang.datafixers.kinds.IdF;
import com.mojang.datafixers.kinds.OptionalBox;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.monster.piglin.RememberIfHoglinWasKilled;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RememberIfHoglinWasKilled.class)
public class RememberIfHoglinWasKilledMixin {

    @Inject(method = "lambda$create$0", at = @At("HEAD"), cancellable = true)
    private static void handleReturn(BehaviorBuilder.Instance<LivingEntity> instance, MemoryAccessor<IdF.Mu, LivingEntity> at, MemoryAccessor<OptionalBox.Mu, Boolean> hr, ServerLevel level, LivingEntity entity, long gameTime, CallbackInfoReturnable<Boolean> cir){
        BehaviorHelper.handleHuntTargetIfKilled(entity);
        cir.setReturnValue(true);
    }
}
