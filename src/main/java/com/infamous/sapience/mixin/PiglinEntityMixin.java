package com.infamous.sapience.mixin;

import com.infamous.sapience.mod.IShakesHead;
import com.infamous.sapience.util.*;
import com.mojang.datafixers.util.Pair;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.merchant.IReputationTracking;
import net.minecraft.entity.merchant.IReputationType;
import net.minecraft.entity.monster.piglin.AbstractPiglinEntity;
import net.minecraft.entity.monster.piglin.PiglinAction;
import net.minecraft.entity.monster.piglin.PiglinEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PiglinEntity.class)
public abstract class PiglinEntityMixin extends AbstractPiglinEntity implements IShakesHead, IReputationTracking {
    private static final DataParameter<Integer> SHAKE_HEAD_TICKS = EntityDataManager.createKey(PiglinEntity.class, DataSerializers.VARINT);
    private static final int NO_TICKS = 40;

    public PiglinEntityMixin(EntityType<? extends AbstractPiglinEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/monster/piglin/PiglinTasks;func_234480_a_(Lnet/minecraft/item/Item;)Z"), method = "func_234424_eM_", cancellable = true)
    private void canPiglinAdmire(CallbackInfoReturnable<PiglinAction> callbackInfoReturnable){
        boolean isPiglinLoved = PiglinTasksHelper.isPiglinLoved(this.getHeldItemOffhand().getItem());
        boolean isPiglinGreedItem = PiglinTasksHelper.isBarterItem(this.getHeldItemOffhand().getItem());
        if(isPiglinLoved || isPiglinGreedItem){
            callbackInfoReturnable.setReturnValue(PiglinAction.ADMIRING_ITEM);
        }
    }

    @Inject(at = @At(value = "HEAD"), method = "func_234436_k_", cancellable = true)
    private void onAddToInventory(ItemStack stack, CallbackInfoReturnable<ItemStack> callbackInfoReturnable) {
        if(PiglinTasksHelper.isBarterItem(stack.getItem())){
            CompoundNBT compoundNBT = stack.getOrCreateTag();
            ItemStack remainder = GreedHelper.addGreedItemToGreedInventory(this, stack, compoundNBT.getBoolean(GreedHelper.BARTERED));
            callbackInfoReturnable.setReturnValue(remainder);
        }
    }

    @Inject(at = @At("RETURN"), method = "func_230254_b_", cancellable = true)
    private void processInteraction(PlayerEntity playerEntity, Hand handIn, CallbackInfoReturnable<ActionResultType> callbackInfoReturnable){
        ActionResultType actionResultType = callbackInfoReturnable.getReturnValue();

        if(!actionResultType.isSuccessOrConsume()){
            // check greed action result type
            actionResultType = PiglinTasksHelper.getGreedActionResultType(this, playerEntity, handIn, actionResultType);
            if(!actionResultType.isSuccessOrConsume()){
                // check ageable action result type
                actionResultType = PiglinTasksHelper.getAgeableActionResultType(this, playerEntity, handIn, actionResultType);
            }
        }

        // handle final action result type now
        if(!actionResultType.isSuccessOrConsume()){
            this.setShakeHeadTicks(NO_TICKS);

            this.playSound(SoundEvents.ENTITY_PIGLIN_ANGRY, this.getSoundVolume(), this.getSoundPitch());
            if(!this.world.isRemote){
                this.world.setEntityState(this, (byte) GeneralHelper.DECLINE_ID);
            }
        }
        else{
            this.playSound(SoundEvents.ENTITY_PIGLIN_CELEBRATE, this.getSoundVolume(), this.getSoundPitch());
            if(!this.world.isRemote){
                this.world.setEntityState(this, (byte) GeneralHelper.ACCEPT_ID);
            }
        }
        callbackInfoReturnable.setReturnValue(actionResultType);
    }
    @Inject(at = @At("HEAD"), method = "dropSpecialItems", cancellable = true)
    private void dropSpecialItems(CallbackInfo callbackInfo){
        //AgeableHelper.dropAllFoodItems(this);
        GreedHelper.dropGreedItems(this);
    }

    @Inject(at = @At("HEAD"), method = "func_234416_a_", cancellable = true)
    private void zombify(CallbackInfo callbackInfo){
        //AgeableHelper.dropAllFoodItems(this);
        GreedHelper.dropGreedItems(this);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void handleStatusUpdate(byte id) {
        if(id == AgeableHelper.BREEDING_ID){
            GeneralHelper.spawnParticles(this, ParticleTypes.HEART);
        }
        else if(id == AgeableHelper.GROWING_ID){
            GeneralHelper.spawnParticles(this, ParticleTypes.COMPOSTER);
        }
        else if(id == GeneralHelper.ANGER_ID){
            GeneralHelper.spawnParticles(this, ParticleTypes.ANGRY_VILLAGER);
        }
        else if(id == GeneralHelper.DECLINE_ID){
            GeneralHelper.spawnParticles(this, ParticleTypes.SMOKE);
        }
        else if(id == GeneralHelper.ACCEPT_ID){
            GeneralHelper.spawnParticles(this, ParticleTypes.HAPPY_VILLAGER);
        }
        else{
            super.handleStatusUpdate(id);
        }
    }

    @Override
    public ItemStack onFoodEaten(World world, ItemStack itemStack) {
        if (itemStack.isFood()) {
            world.playSound((PlayerEntity)null, this.getPosX(), this.getPosY(), this.getPosZ(), this.getEatSound(itemStack), SoundCategory.NEUTRAL, 1.0F, 1.0F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.4F);
            Item item = itemStack.getItem();
            if (item.getFood() != null) {

                int foodValue = item.getFood().getHealing();
                this.heal(foodValue); // heals the piglin by an amount equal to the food's hunger value
                AgeableHelper.increaseFoodLevel(this, foodValue);

                for(Pair<EffectInstance, Float> effectAndChancePair : item.getFood().getEffects()) {
                    EffectInstance effectInstance = effectAndChancePair.getFirst();
                    Float effectChance = effectAndChancePair.getSecond();
                    if (!world.isRemote && effectInstance != null && world.rand.nextFloat() < effectChance) {
                        // only apply negative status effects if the item is not a piglin food
                        // we don't want piglins to get affected by Hunger if they eat a raw porkchop, for example
                        if(effectInstance.getPotion().getEffectType() != EffectType.HARMFUL || !PiglinTasksHelper.isPiglinFoodItem(item)){
                            this.addPotionEffect(new EffectInstance(effectInstance));
                        }
                    }
                }
            }
            itemStack.shrink(1);
            if(!world.isRemote){
                PiglinTasksHelper.setAteRecently(this);
                ReputationHelper.updatePreviousInteractorReputation(this, PiglinReputationType.FOOD_GIFT);
            }
        }

        return itemStack;
    }

    @Inject(at = @At("RETURN"), method = "registerData")
    private void registerData(CallbackInfo callbackInfo){
        this.dataManager.register(SHAKE_HEAD_TICKS, 0);
    }

    @Override
    public int getShakeHeadTicks() {
        return this.dataManager.get(SHAKE_HEAD_TICKS);
    }

    @Override
    public void setShakeHeadTicks(int ticks) {
        this.dataManager.set(SHAKE_HEAD_TICKS, ticks);
    }

    // called by ServerWorld in updateReputation
    @Override
    public void updateReputation(IReputationType type, Entity target) {
        ReputationHelper.updatePiglinReputation(this, type, target);
    }

    @Override
    public void triggerItemPickupTrigger(ItemEntity itemEntity) {
        super.triggerItemPickupTrigger(itemEntity);
        if(itemEntity.getThrowerId() != null && this.world instanceof ServerWorld){
            Entity throwerEntity = ((ServerWorld) this.world).getEntityByUuid(itemEntity.getThrowerId());
            ReputationHelper.setPreviousInteractor(this, throwerEntity);
        }
    }
}
