package com.infamous.sapience.mixin;

import com.infamous.sapience.SapienceConfig;
import com.infamous.sapience.capability.ageable.IAgeable;
import com.infamous.sapience.util.PiglinReputationType;
import com.infamous.sapience.util.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.BrainUtil;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.FirstShuffledTask;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.monster.piglin.AbstractPiglinEntity;
import net.minecraft.entity.monster.piglin.PiglinEntity;
import net.minecraft.entity.monster.piglin.PiglinTasks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.stream.Collectors;

@Mixin(PiglinTasks.class)
public class PiglinTasksMixin {

    @Inject(at = @At("HEAD"), method = "func_234468_a_", cancellable = true)
    private static void wasHurtBy(PiglinEntity piglin, LivingEntity attacker, CallbackInfo ci){
        if(GeneralHelper.isOnSameTeam(piglin, attacker)){
            ci.cancel();
        }
    }

    @Redirect(at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/monster/piglin/PiglinTasks;func_234459_a_(Lnet/minecraft/entity/EntityType;)Z"),
            method = "func_234533_t_")
    private static boolean shouldAvoidZombified(EntityType entityType, PiglinEntity piglin){
        Brain<PiglinEntity> brain = piglin.getBrain();
        LivingEntity livingentity = brain.getMemory(MemoryModuleType.AVOID_TARGET).get();
        return PiglinTasksHelper.isZombified(livingentity)
                && GeneralHelper.isNotOnSameTeam(piglin, livingentity);
    }

    @Inject(at = @At("RETURN"), method = "func_234481_b_", cancellable = true)
    private static void getLookTasks(CallbackInfoReturnable<FirstShuffledTask<PiglinEntity>> callbackInfoReturnable){
        FirstShuffledTask<PiglinEntity> firstShuffledTask = new FirstShuffledTask<>(PiglinTasksHelper.getInteractionTasks());
        callbackInfoReturnable.setReturnValue(firstShuffledTask);
    }

    @Inject(at = @At("HEAD"), method = "func_234497_c_")
    private static void setAngerTarget(AbstractPiglinEntity piglinEntity, LivingEntity target, CallbackInfo callbackInfo){
        if(PiglinTasksHelper.canTarget(target)){
            piglinEntity.world.setEntityState(piglinEntity, (byte) GeneralHelper.ANGER_ID);
        }
    }

    @Inject(at =
    @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/monster/piglin/PiglinTasks;func_234465_a_(Lnet/minecraft/entity/item/ItemEntity;)Lnet/minecraft/item/ItemStack;"),
            method = "func_234470_a_",
            cancellable = true)
    private static void pickUpWantedItem(PiglinEntity piglinEntity, ItemEntity itemEntity, CallbackInfo callbackInfo){
        IAgeable ageable = AgeableHelper.getAgeableCapability(piglinEntity);
        if(ageable != null && PiglinTasksHelper.isPiglinFoodItem(itemEntity.getItem().getItem())){
            ItemStack extractedItemStack = PiglinTasksHelper.extractSingletonFromItemEntity(itemEntity);
            // Needed to get the piglin to stop trying to pick up its food item once it's been picked up
            PiglinTasksHelper.removeTimeTryingToReachAdmireItem(piglinEntity);
            //PiglinTasksHelper.addToFoodInventoryThenDropRemainder(piglinEntity, extractedItemStack);
            PiglinTasksHelper.dropOffhandItemAndSetItemStackToOffhand(piglinEntity, extractedItemStack);
            PiglinTasksHelper.setAdmiringItem(piglinEntity);
            PiglinTasksHelper.clearWalkPath(piglinEntity);
            /*
            if(!PiglinTasksHelper.hasAteRecently(piglinEntity)){
                PiglinTasksHelper.setAteRecently(piglinEntity);
            }
             */
            callbackInfo.cancel();
        }
    }

    @Inject(at = @At("RETURN"), method = "func_234499_c_", cancellable = true)
    private static void isPiglinFoodItem(Item item, CallbackInfoReturnable<Boolean> callbackInfoReturnable){
        boolean isPiglinFoodItem = PiglinTasksHelper.isPiglinFoodItem(item);
        callbackInfoReturnable.setReturnValue(isPiglinFoodItem);
    }


    @Inject(at = @At("RETURN"), method = "func_234474_a_", cancellable = true)
    private static void canPickUpItemStack(PiglinEntity piglinEntity, ItemStack itemStack, CallbackInfoReturnable<Boolean> callbackInfoReturnable){
        boolean hasConsumableOffhandItem = PiglinTasksHelper.hasConsumableOffhandItem(piglinEntity);
        boolean canPickUpItemStack = callbackInfoReturnable.getReturnValue();
        if (PiglinTasksHelper.isPiglinFoodItem(itemStack.getItem())) {
            canPickUpItemStack = PiglinTasksHelper.canPickUpFoodStack(piglinEntity, itemStack);
        }
        callbackInfoReturnable.setReturnValue(canPickUpItemStack && !hasConsumableOffhandItem);
    }

    // when the piglin is going to finish admiring an item
    // the goal here is to check if the piglin should drop from the alternative barter loot tables
    // and also update the player's reputation with the piglin if it can do a barter successfully
    @Inject(at = @At(value = "HEAD"), method = "func_234477_a_", cancellable = true)
    private static void finishAdmiringItem(PiglinEntity piglinEntity, boolean doBarter, CallbackInfo callbackInfo){

        Entity interactorEntity = ReputationHelper.getPreviousInteractor(piglinEntity);
        boolean willDropLoot =
                interactorEntity instanceof LivingEntity && ReputationHelper.isAllowedToBarter(piglinEntity, (LivingEntity) interactorEntity)
                || interactorEntity == null && !SapienceConfig.COMMON.REQUIRE_LIVING_FOR_BARTER.get();

        ItemStack offhandStack = piglinEntity.getHeldItem(Hand.OFF_HAND);

        boolean isIngotBarterGreedItem = PiglinTasksHelper.isNormalBarterItem(offhandStack.getItem());
        if (isIngotBarterGreedItem) {
            GreedHelper.addStackToGreedInventoryCheckBartered(piglinEntity, offhandStack, doBarter && piglinEntity.func_242337_eM());
        }

        if (!piglinEntity.isChild()) { // isAdult
            if(PiglinTasksHelper.isExpensiveBarterItem(offhandStack.getItem()) && doBarter && willDropLoot){
                PiglinTasksHelper.dropBlockBarteringLoot(piglinEntity);
                CompoundNBT compoundNBT = offhandStack.getOrCreateTag();
                compoundNBT.putBoolean(GreedHelper.BARTERED, true);

                ReputationHelper.updatePreviousInteractorReputation(piglinEntity, PiglinReputationType.BARTER);
            }
            else if(PiglinTasksHelper.isCheapBarterItem(offhandStack.getItem()) && doBarter && willDropLoot){
                PiglinTasksHelper.dropNuggetBarteringLoot(piglinEntity);
                CompoundNBT compoundNBT = offhandStack.getOrCreateTag();
                compoundNBT.putBoolean(GreedHelper.BARTERED, true);

                ReputationHelper.updatePreviousInteractorReputation(piglinEntity, PiglinReputationType.BARTER);
            }
            // treat piglin loved items as gold gifts for adults
            else if(PiglinTasksHelper.isPiglinLoved(offhandStack.getItem()) && !PiglinTasksHelper.isBarterItem(offhandStack.getItem())){
                ReputationHelper.updatePreviousInteractorReputation(piglinEntity, PiglinReputationType.GOLD_GIFT);
            }

            // Since baby Piglins don't barter, we can treat barter items as gold gifts in addition to piglin loved items
        } else if(PiglinTasksHelper.isPiglinLoved(offhandStack.getItem()) || PiglinTasksHelper.isBarterItem(offhandStack.getItem())){
            ReputationHelper.updatePreviousInteractorReputation(piglinEntity, PiglinReputationType.GOLD_GIFT);
        }
    }

    // When the piglin drops bartering loot after having been given piglin currency
    // The goal here is to prevent it dropping bartering loot if the player's rep is too low
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/monster/piglin/PiglinTasks;func_234475_a_(Lnet/minecraft/entity/monster/piglin/PiglinEntity;Ljava/util/List;)V", ordinal = 0), method = "func_234477_a_", cancellable = true)
    private static void dropLoot(PiglinEntity piglinEntity, boolean doBarter, CallbackInfo callbackInfo){
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

    @Inject(at = @At(value = "RETURN"), method = "func_234489_b_", cancellable = true)
    private static void canAcceptItemStack(PiglinEntity piglinEntity, ItemStack itemStack, CallbackInfoReturnable<Boolean> callbackInfoReturnable){
        callbackInfoReturnable.setReturnValue(callbackInfoReturnable.getReturnValue() && !PiglinTasksHelper.hasConsumableOffhandItem(piglinEntity));
    }

    @ModifyVariable(at = @At("STORE"), method = "func_234478_a_")
    private static List<PiglinEntity> getPiglinsAngryAtThief(List<PiglinEntity> nearbyPiglinsList, PlayerEntity playerEntity, boolean checkVisible) {
        List<PiglinEntity> filteredNearbyPiglinsList = nearbyPiglinsList
                .stream()
                .filter(PiglinTasksHelper::hasIdle)
                .filter((nearbyPiglin) -> !checkVisible || BrainUtil.isMobVisible(nearbyPiglin, playerEntity))
                .filter((nearbyPiglin) -> !ReputationHelper.isAllowedToTouchGold(playerEntity, nearbyPiglin))
                .collect(Collectors.toList());

        filteredNearbyPiglinsList
                .forEach((nearbyPiglin) -> ReputationHelper.updatePiglinReputation(nearbyPiglin, PiglinReputationType.GOLD_STOLEN, playerEntity));

        return filteredNearbyPiglinsList;
    }

    // used for processing the default interaction for receiving piglin currency
    @Inject(at = @At("RETURN"), method = "func_234471_a_")
    private static void processInteractionForPiglinCurrency(PiglinEntity piglinEntity, PlayerEntity playerEntity, Hand hand, CallbackInfoReturnable<ActionResultType> callbackInfoReturnable){
        if(callbackInfoReturnable.getReturnValue().isSuccessOrConsume()){
            ReputationHelper.setPreviousInteractor(piglinEntity, playerEntity);
        }
    }
}
