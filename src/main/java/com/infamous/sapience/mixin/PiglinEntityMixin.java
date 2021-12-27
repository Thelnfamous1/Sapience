package com.infamous.sapience.mixin;

import com.infamous.sapience.mod.IShakesHead;
import com.infamous.sapience.util.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ReputationEventHandler;
import net.minecraft.world.entity.ai.village.ReputationEventType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinArmPose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Piglin.class)
public abstract class PiglinEntityMixin extends AbstractPiglin implements IShakesHead, ReputationEventHandler {
    private static final EntityDataAccessor<Integer> SHAKE_HEAD_TICKS = SynchedEntityData.defineId(Piglin.class, EntityDataSerializers.INT);

    public PiglinEntityMixin(EntityType<? extends AbstractPiglin> entityType, Level world) {
        super(entityType, world);
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/piglin/PiglinAi;isLovedItem(Lnet/minecraft/world/item/ItemStack;)Z"), method = "getArmPose", cancellable = true)
    private void canPiglinAdmire(CallbackInfoReturnable<PiglinArmPose> callbackInfoReturnable){
        if(PiglinTasksHelper.isPiglinLoved(this.getOffhandItem()) || PiglinTasksHelper.isBarterItem(this.getOffhandItem())){
            callbackInfoReturnable.setReturnValue(PiglinArmPose.ADMIRING_ITEM);
        }
    }

    @Inject(at = @At(value = "HEAD"), method = "addToInventory", cancellable = true)
    private void onAddToInventory(ItemStack stack, CallbackInfoReturnable<ItemStack> cir) {
        if(PiglinTasksHelper.isBarterItem(stack)){
            CompoundTag compoundNBT = stack.getOrCreateTag();
            ItemStack remainder = GreedHelper.addGreedItemToGreedInventory(this, stack, compoundNBT.getBoolean(GreedHelper.BARTERED));
            cir.setReturnValue(remainder);
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void handleEntityEvent(byte id) {
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
            super.handleEntityEvent(id);
        }
    }

    @Inject(at = @At("RETURN"), method = "defineSynchedData")
    private void registerData(CallbackInfo callbackInfo){
        this.entityData.define(SHAKE_HEAD_TICKS, 0);
    }

    @Override
    public int getShakeHeadTicks() {
        return this.entityData.get(SHAKE_HEAD_TICKS);
    }

    @Override
    public void setShakeHeadTicks(int ticks) {
        this.entityData.set(SHAKE_HEAD_TICKS, ticks);
    }

    // called by ServerWorld in updateReputation
    @Override
    public void onReputationEventFrom(ReputationEventType type, Entity target) {
        ReputationHelper.updatePiglinReputation(this, type, target);
    }

    @Override
    public void onItemPickup(ItemEntity itemEntity) {
        super.onItemPickup(itemEntity);
        if(itemEntity.getThrower() != null && this.level instanceof ServerLevel){
            Entity throwerEntity = ((ServerLevel) this.level).getEntity(itemEntity.getThrower());
            ReputationHelper.setPreviousInteractor(this, throwerEntity);
        }
    }
}
