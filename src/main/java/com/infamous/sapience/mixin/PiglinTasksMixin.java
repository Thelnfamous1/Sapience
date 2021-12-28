package com.infamous.sapience.mixin;

import com.infamous.sapience.SapienceConfig;
import com.infamous.sapience.capability.ageable.Ageable;
import com.infamous.sapience.util.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Mixin(PiglinAi.class)
public class PiglinTasksMixin {

    @Redirect(at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/monster/piglin/PiglinAi;isZombified(Lnet/minecraft/world/entity/EntityType;)Z"),
            method = "wantsToStopFleeing")
    private static boolean shouldAvoidZombified(EntityType<?> entityType, Piglin piglin){
        return piglin.getBrain().getMemory(MemoryModuleType.AVOID_TARGET)
                .map(le -> PiglinTasksHelper.piglinsAvoid(le.getType())
                        && GeneralHelper.isNotOnSameTeam(piglin, le))
                .orElse(false);
    }

    @Inject(at = @At("HEAD"), method = "setAngerTarget")
    private static void setAngerTarget(AbstractPiglin piglinEntity, LivingEntity target, CallbackInfo callbackInfo){
        if(Sensor.isEntityAttackableIgnoringLineOfSight(piglinEntity, target)){
            piglinEntity.level.broadcastEntityEvent(piglinEntity, (byte) GeneralHelper.ANGER_ID);
        }
    }

    @Inject(at =
    @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/monster/piglin/PiglinAi;removeOneItemFromItemEntity(Lnet/minecraft/world/entity/item/ItemEntity;)Lnet/minecraft/world/item/ItemStack;"),
            method = "pickUpItem",
            cancellable = true)
    private static void pickUpWantedItem(Piglin piglinEntity, ItemEntity itemEntity, CallbackInfo callbackInfo){
        Ageable ageable = AgeableHelper.getAgeableCapability(piglinEntity);
        if(ageable != null && PiglinTasksHelper.isPiglinFoodItem(itemEntity.getItem())){
            ItemStack extractedItemStack = PiglinTasksHelper.extractSingletonFromItemEntity(itemEntity);
            // Needed to get the piglin to stop trying to pick up its food item once it's been picked up
            PiglinTasksHelper.removeTimeTryingToReachAdmireItem(piglinEntity);
            PiglinTasksHelper.dropOffhandItemAndSetItemStackToOffhand(piglinEntity, extractedItemStack);
            PiglinTasksHelper.setAdmiringItem(piglinEntity);
            PiglinTasksHelper.clearWalkPath(piglinEntity);
            callbackInfo.cancel();
        }
    }

    @Inject(at = @At("RETURN"), method = "isFood", cancellable = true)
    private static void isPiglinFoodItem(ItemStack item, CallbackInfoReturnable<Boolean> callbackInfoReturnable){
        callbackInfoReturnable.setReturnValue(PiglinTasksHelper.isPiglinFoodItem(item));
    }


    @Inject(at = @At("RETURN"), method = "wantsToPickup", cancellable = true)
    private static void canPickUpItemStack(Piglin piglinEntity, ItemStack itemStack, CallbackInfoReturnable<Boolean> callbackInfoReturnable){
        boolean hasConsumableOffhandItem = PiglinTasksHelper.hasConsumableOffhandItem(piglinEntity);
        boolean canPickUpItemStack = callbackInfoReturnable.getReturnValue();
        if (PiglinTasksHelper.isPiglinFoodItem(itemStack)) {
            canPickUpItemStack = PiglinTasksHelper.canPickUpFoodStack(piglinEntity, itemStack);
        }
        callbackInfoReturnable.setReturnValue(canPickUpItemStack && !hasConsumableOffhandItem);
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

    @Inject(at = @At(value = "HEAD"), method = "putInInventory", cancellable = true)
    private static void handlePutInInventory(Piglin piglin, ItemStack stack, CallbackInfo ci){
        if(PiglinTasksHelper.isBarterItem(stack)){
            CompoundTag compoundNBT = stack.getOrCreateTag();
            ItemStack remainder = GreedHelper.addGreedItemToGreedInventory(piglin, stack, compoundNBT.getBoolean(GreedHelper.BARTERED));
            PiglinTasksHelper.dropItemsAccountingForNearbyPlayer(piglin, Collections.singletonList(remainder));
            ci.cancel();
        }
    }

    // fixing hardcoded checks for "EntityType.ZOMBIFIED_PIGLIN" and "EntityType.ZOGLIN"

    @Inject(at = @At("RETURN"), method = "isZombified", cancellable = true)
    private static void handleZombified(EntityType<?> entityType, CallbackInfoReturnable<Boolean> cir){
        cir.setReturnValue(PiglinTasksHelper.piglinsAvoid(entityType));
    }

    // fixing hardcoded checks for "EntityType.HOGLIN"

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getType()Lnet/minecraft/world/entity/EntityType;"), method = "wantsToDance")
    private static EntityType<?> redirectEntityTypeCheckDance(LivingEntity victim){
        // spoofs the check for the hoglin entity type if it is a mob that can be hunted by piglins
        return GeneralHelper.maybeSpoofPiglinsHunt(victim);
    }

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

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getType()Lnet/minecraft/world/entity/EntityType;"), method = "wantsToStopFleeing")
    private static EntityType<?> redirectEntityTypeCheckFleeing(LivingEntity avoidTarget){
        // spoofs the check for the hoglin entity type if it is a mob that can be hunted by piglins
        return GeneralHelper.maybeSpoofHoglin(avoidTarget);
    }

}
