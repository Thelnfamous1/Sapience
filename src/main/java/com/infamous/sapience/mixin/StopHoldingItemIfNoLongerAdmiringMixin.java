package com.infamous.sapience.mixin;

import com.infamous.sapience.util.BehaviorHelper;
import com.infamous.sapience.util.PiglinTasksHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.StopHoldingItemIfNoLongerAdmiring;
import net.minecraftforge.common.ToolActions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StopHoldingItemIfNoLongerAdmiring.class)
public class StopHoldingItemIfNoLongerAdmiringMixin {

    @Inject(method = "lambda$create$0", at = @At("HEAD"), cancellable = true)
    private static void handleReturn(ServerLevel level, Piglin piglin, long gameTime, CallbackInfoReturnable<Boolean> cir){
        boolean canStopVanilla = !piglin.getOffhandItem().isEmpty() && !piglin.getOffhandItem().canPerformAction(ToolActions.SHIELD_BLOCK);
        boolean canStopHolding = BehaviorHelper.canStopHoldingItemIfNoLongerAdmiring(canStopVanilla, piglin);
        cir.setReturnValue(canStopHolding);
        if(canStopHolding) PiglinTasksHelper.stopHoldingOffHandItem(piglin, true);
    }
}
