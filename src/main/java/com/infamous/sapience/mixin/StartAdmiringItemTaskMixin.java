package com.infamous.sapience.mixin;

import com.infamous.sapience.util.PiglinTasksHelper;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.StopHoldingItemIfNoLongerAdmiring;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.ToolActions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StopHoldingItemIfNoLongerAdmiring.class)
public class StartAdmiringItemTaskMixin {

    @Inject(at = @At("RETURN"), method = "checkExtraStartConditions", cancellable = true)
    private void shouldExecute(ServerLevel serverWorld, Piglin owner, CallbackInfoReturnable<Boolean> callbackInfoReturnable){
        boolean hasOffhandItem = !owner.getOffhandItem().isEmpty();
        boolean doesNotHaveShield = !owner.getOffhandItem().canPerformAction(ToolActions.SHIELD_BLOCK);
        boolean doesNotHaveConsumableItem = !PiglinTasksHelper.hasConsumableOffhandItem(owner);
        boolean shouldExecute = hasOffhandItem && doesNotHaveShield && doesNotHaveConsumableItem;
        callbackInfoReturnable.setReturnValue(shouldExecute);
    }
}
