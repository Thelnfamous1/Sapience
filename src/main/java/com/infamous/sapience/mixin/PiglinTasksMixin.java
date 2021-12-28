package com.infamous.sapience.mixin;

import com.infamous.sapience.util.GeneralHelper;
import com.infamous.sapience.util.PiglinReputationType;
import com.infamous.sapience.util.PiglinTasksHelper;
import com.infamous.sapience.util.ReputationHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.stream.Collectors;

@Mixin(PiglinAi.class)
public class PiglinTasksMixin {

    @Inject(at = @At("HEAD"), method = "setAngerTarget")
    private static void setAngerTarget(AbstractPiglin piglinEntity, LivingEntity target, CallbackInfo callbackInfo){
        if(Sensor.isEntityAttackableIgnoringLineOfSight(piglinEntity, target)){
            piglinEntity.level.broadcastEntityEvent(piglinEntity, (byte) GeneralHelper.ANGER_ID);
        }
    }

    @ModifyVariable(at = @At("STORE"), method = "angerNearbyPiglins")
    private static List<Piglin> getPiglinsAngryAtThief(List<Piglin> nearbyPiglinsList, Player playerEntity, boolean checkVisible) {
        List<Piglin> filteredNearbyPiglinsList = nearbyPiglinsList
                .stream()
                .filter(PiglinTasksHelper::hasIdle)
                .filter((nearbyPiglin) -> !checkVisible || BehaviorUtils.canSee(nearbyPiglin, playerEntity))
                .filter((nearbyPiglin) -> !ReputationHelper.isAllowedToTouchGold(playerEntity, nearbyPiglin))
                .collect(Collectors.toList());

        filteredNearbyPiglinsList
                .forEach((nearbyPiglin) -> ReputationHelper.updatePiglinReputation(nearbyPiglin, PiglinReputationType.GOLD_STOLEN, playerEntity));

        return filteredNearbyPiglinsList;
    }

}
