package com.infamous.sapience.mixin;

import com.infamous.sapience.util.GeneralHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.piglin.PiglinBruteAi;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PiglinBruteAi.class)
public class PiglinBruteBrainMixin {

    @Inject(at = @At("HEAD"), method = "wasHurtBy", cancellable = true)
    private static void wasHurtBy(PiglinBrute brute, LivingEntity attacker, CallbackInfo ci){
        if(GeneralHelper.isOnSameTeam(brute, attacker)){
            ci.cancel();
        }
    }
}
