package com.infamous.sapience.mixin;

import net.minecraft.entity.monster.piglin.PiglinEntity;
import net.minecraft.entity.monster.piglin.StartAdmiringItemTask;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StartAdmiringItemTask.class)
public class StartAdmiringItemTaskMixin {

    @Inject(at = @At("RETURN"), method = "shouldExecute", cancellable = true)
    private void shouldExecute(ServerWorld serverWorld, PiglinEntity owner, CallbackInfoReturnable<Boolean> callbackInfoReturnable){
        boolean hasOffhandItem = !owner.getHeldItemOffhand().isEmpty();
        boolean doesNotHaveShield = !owner.getHeldItemOffhand().getItem().isShield(owner.getHeldItemOffhand(), owner);
        boolean shouldExecute = hasOffhandItem && doesNotHaveShield;
        callbackInfoReturnable.setReturnValue(shouldExecute);
    }
}
