package com.infamous.sapience.mixin;

import com.infamous.sapience.SapienceConfig;
import com.infamous.sapience.util.GeneralHelper;
import com.infamous.sapience.util.PiglinReputationType;
import com.infamous.sapience.util.PiglinTasksHelper;
import com.infamous.sapience.util.ReputationHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
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

    // When the piglin drops bartering loot after having been given piglin currency
    // The goal here is to prevent it dropping bartering loot if the player's rep is too low
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/piglin/PiglinAi;throwItems(Lnet/minecraft/world/entity/monster/piglin/Piglin;Ljava/util/List;)V", ordinal = 0), method = "stopHoldingOffHandItem", cancellable = true)
    private static void dropLoot(Piglin piglinEntity, boolean doBarter, CallbackInfo callbackInfo){
        Entity interactorEntity = ReputationHelper.getPreviousInteractor(piglinEntity);

        if(doBarter){
            ReputationHelper.updatePreviousInteractorReputation(piglinEntity, PiglinReputationType.BARTER);
        }
        boolean willDropLoot =
                interactorEntity instanceof LivingEntity && ReputationHelper.isAllowedToBarter(piglinEntity, (LivingEntity) interactorEntity)
                || interactorEntity == null && !SapienceConfig.COMMON.REQUIRE_LIVING_FOR_BARTER.get();

        if(!willDropLoot){
            callbackInfo.cancel();
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
    // fixing hardcoded checks for "EntityType.HOGLIN"

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getType()Lnet/minecraft/world/entity/EntityType;"), method = "wasHurtBy")
    private static EntityType<?> redirectEntityTypeCheckHurt(LivingEntity attacker){
        // spoofs the check for the hoglin entity type if it is a mob that can be hunted by piglins
        return GeneralHelper.maybeSpoofHoglin(attacker);
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getType()Lnet/minecraft/world/entity/EntityType;"), method = "broadcastAngerTarget")
    private static EntityType<?> redirectEntityTypeCheckBroadcastAnger(LivingEntity angerTarget){
        // spoofs the check for the hoglin entity type if it is a mob that can be hunted by piglins
        return GeneralHelper.maybeSpoofHoglin(angerTarget);
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getType()Lnet/minecraft/world/entity/EntityType;", ordinal = 0), method = "setAngerTarget")
    private static EntityType<?> redirectEntityTypeCheckAnger(LivingEntity angerTarget){
        // spoofs the check for the hoglin entity type if it is a mob that can be hunted by piglins
        return GeneralHelper.maybeSpoofHoglin(angerTarget);
    }

}
