package com.infamous.sapience.mixin;

import com.infamous.sapience.util.HoglinTasksHelper;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.monster.HoglinEntity;
import net.minecraft.entity.monster.HoglinTasks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HoglinTasks.class)
public class HoglinTasksMixin {

    @Inject(at = @At("HEAD"), method = "func_234382_b_", cancellable = true)
    private static void getCoreTasks(Brain<HoglinEntity> hoglinEntityBrain, CallbackInfo callbackInfo){
        HoglinTasksHelper.registerCoreTasks(hoglinEntityBrain);
        callbackInfo.cancel();
    }
}
