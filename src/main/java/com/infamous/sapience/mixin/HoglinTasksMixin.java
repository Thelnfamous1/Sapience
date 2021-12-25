package com.infamous.sapience.mixin;

import com.infamous.sapience.util.GeneralHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.hoglin.HoglinAi;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(HoglinAi.class)
public class HoglinTasksMixin {

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getType()Lnet/minecraft/world/entity/EntityType;"), method = "onHitTarget")
    private static EntityType<?> redirectTypeCheckForAttack(LivingEntity livingEntity){
        return GeneralHelper.maybeSpoofPiglin(livingEntity);
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getType()Lnet/minecraft/world/entity/EntityType;"), method = "maybeRetaliate")
    private static EntityType<?> redirectTypeCheckForRetalaiation(LivingEntity livingEntity){
        return GeneralHelper.maybeSpoofHoglinOrPiglin(livingEntity);
    }

}
