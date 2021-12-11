package com.infamous.sapience.mixin;

import com.infamous.sapience.util.GeneralHelper;
import com.infamous.sapience.util.HoglinTasksHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.hoglin.HoglinAi;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HoglinAi.class)
public class HoglinTasksMixin {

    @Inject(at = @At("HEAD"), method = "wasHurtBy", cancellable = true)
    private static void wasHurtBy(Hoglin hoglin, LivingEntity attacker, CallbackInfo ci){
        if(GeneralHelper.isOnSameTeam(hoglin, attacker)){
            ci.cancel();
        }
    }

    @Inject(at = @At("HEAD"), method = "initCoreActivity", cancellable = true)
    private static void getCoreTasks(Brain<Hoglin> hoglinEntityBrain, CallbackInfo callbackInfo){
        HoglinTasksHelper.registerCoreTasks(hoglinEntityBrain);
        callbackInfo.cancel();
    }
}
