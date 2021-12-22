package com.infamous.sapience.mixin;

import com.infamous.sapience.util.BrainHelper;
import com.infamous.sapience.util.GeneralHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.GoToWantedItem;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.hoglin.HoglinAi;
import net.minecraft.world.entity.schedule.Activity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HoglinAi.class)
public class HoglinTasksMixin {

    @Inject(at = @At("RETURN"), method = "initCoreActivity", cancellable = true)
    private static void getCoreTasks(Brain<Hoglin> hoglinEntityBrain, CallbackInfo callbackInfo){
        BrainHelper.addAdditionalTasks(hoglinEntityBrain, Activity.CORE, 2,
                new GoToWantedItem<>(1.0F, true, 9));
    }

}
