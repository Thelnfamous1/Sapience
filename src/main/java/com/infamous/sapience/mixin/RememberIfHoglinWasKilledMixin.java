package com.infamous.sapience.mixin;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.piglin.RememberIfHoglinWasKilled;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RememberIfHoglinWasKilled.class)
public class RememberIfHoglinWasKilledMixin {

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getType()Lnet/minecraft/world/entity/EntityType;"), method = "isAttackTargetDeadHoglin")
    private static EntityType<?> redirectEntityTypeCheckAnger(LivingEntity attackTarget){
        // spoofs the check for the hoglin entity type if it is a mob that can be hunted by piglins
        return attackTarget instanceof Hoglin ? EntityType.HOGLIN : attackTarget.getType();
    }
}
