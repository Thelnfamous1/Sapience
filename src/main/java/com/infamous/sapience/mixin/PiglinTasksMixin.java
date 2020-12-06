package com.infamous.sapience.mixin;

import com.infamous.sapience.capability.ageable.IAgeable;
import com.infamous.sapience.util.AgeableHelper;
import com.infamous.sapience.util.GreedHelper;
import com.infamous.sapience.util.PiglinTasksHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.task.FirstShuffledTask;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.monster.piglin.AbstractPiglinEntity;
import net.minecraft.entity.monster.piglin.PiglinEntity;
import net.minecraft.entity.monster.piglin.PiglinTasks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PiglinTasks.class)
public class PiglinTasksMixin {

    @Inject(at = @At("RETURN"), method = "func_234481_b_", cancellable = true)
    private static void getLookTasks(CallbackInfoReturnable<FirstShuffledTask<PiglinEntity>> callbackInfoReturnable){
        FirstShuffledTask<PiglinEntity> firstShuffledTask = new FirstShuffledTask<>(PiglinTasksHelper.getInteractionTasks());
        callbackInfoReturnable.setReturnValue(firstShuffledTask);
    }

    @Inject(at = @At("HEAD"), method = "func_234497_c_")
    private static void setAngerTarget(AbstractPiglinEntity piglinEntity, LivingEntity target, CallbackInfo callbackInfo){
        if(PiglinTasksHelper.canTarget(target)){
            piglinEntity.world.setEntityState(piglinEntity, (byte) AgeableHelper.ANGER_ID);
        }
    }

    @Inject(at =
    @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/monster/piglin/PiglinTasks;func_234465_a_(Lnet/minecraft/entity/item/ItemEntity;)Lnet/minecraft/item/ItemStack;"),
            method = "func_234470_a_",
            cancellable = true)
    private static void pickUpTemptationItem(PiglinEntity piglinEntity, ItemEntity itemEntity, CallbackInfo callbackInfo){
        IAgeable ageable = AgeableHelper.getAgeableCapability(piglinEntity);
        if(ageable != null && PiglinTasksHelper.isPiglinFoodItem(itemEntity.getItem().getItem())){
            ItemStack extractedItemStack = PiglinTasksHelper.extractSingletonFromItemEntity(itemEntity);
            // Needed to get the piglin to stop trying to pick up its food item once it's been picked up
            PiglinTasksHelper.stopTryingToReachAdmireItem(piglinEntity);
            //PiglinTasksHelper.setAdmiringItem(piglinEntity);
            PiglinTasksHelper.addToFoodInventoryThenDropRemainder(piglinEntity, extractedItemStack);
            if(!PiglinTasksHelper.hasAteRecently(piglinEntity)){
                PiglinTasksHelper.setAteRecently(piglinEntity);
            }
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
        boolean canPickUpItemStack = callbackInfoReturnable.getReturnValue();
        if (PiglinTasksHelper.isPiglinFoodItem(itemStack.getItem())) {
            canPickUpItemStack = PiglinTasksHelper.canPickUpFoodStack(piglinEntity, itemStack);
        }
        callbackInfoReturnable.setReturnValue(canPickUpItemStack);
    }

    @Inject(at = @At(value = "HEAD"), method = "func_234477_a_")
    private static void finishAdmiringItem(PiglinEntity piglinEntity, boolean doBarter, CallbackInfo callbackInfo){
        ItemStack itemstack = piglinEntity.getHeldItem(Hand.OFF_HAND);
        if (piglinEntity.func_242337_eM()) { // isAdult
            boolean isPiglinCurrency = PiglinTasksHelper.isPiglinCurrency(itemstack.getItem());
            if (isPiglinCurrency) {
                GreedHelper.addStackToGreedInventoryCheckTraded(piglinEntity, itemstack, doBarter);
            }
            else{
                if(PiglinTasksHelper.isBlockBarterGreedItem(itemstack.getItem()) && doBarter){
                    PiglinTasksHelper.dropBlockBarteringLoot(piglinEntity);
                    CompoundNBT compoundNBT = itemstack.getOrCreateTag();
                    compoundNBT.putBoolean(GreedHelper.BARTERED, true);
                }
                else if(PiglinTasksHelper.isNuggetBarterGreedItem(itemstack.getItem()) && doBarter){
                    PiglinTasksHelper.dropNuggetBarteringLoot(piglinEntity);
                    CompoundNBT compoundNBT = itemstack.getOrCreateTag();
                    compoundNBT.putBoolean(GreedHelper.BARTERED, true);
                }
            }
        }
    }
}
