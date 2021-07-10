package com.infamous.sapience.mixin;

import com.infamous.sapience.util.GeneralHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.piglin.PiglinBruteBrain;
import net.minecraft.entity.monster.piglin.PiglinBruteEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PiglinBruteBrain.class)
public class PiglinBruteBrainMixin {

    @Inject(at = @At("HEAD"), method = "func_242353_a", cancellable = true)
    private static void wasHurtBy(PiglinBruteEntity brute, LivingEntity attacker, CallbackInfo ci){
        if(GeneralHelper.isOnSameTeam(brute, attacker)){
            ci.cancel();
        }
    }
}
