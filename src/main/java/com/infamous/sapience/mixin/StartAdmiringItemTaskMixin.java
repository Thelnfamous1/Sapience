package com.infamous.sapience.mixin;

import com.infamous.sapience.util.PiglinTasksHelper;
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
        boolean doesNotHaveShield = !owner.getHeldItemOffhand().isShield(owner);
        boolean doesNotHaveConsumableItem = !PiglinTasksHelper.hasConsumableOffhandItem(owner);
        boolean shouldExecute = hasOffhandItem && doesNotHaveShield && doesNotHaveConsumableItem;
        callbackInfoReturnable.setReturnValue(shouldExecute);
    }
}
