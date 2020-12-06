package com.infamous.sapience.mixin;

import com.infamous.sapience.util.HoglinTasksHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.monster.HoglinEntity;
import net.minecraft.entity.monster.IFlinging;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HoglinEntity.class)
public abstract class HoglinEntityMixin extends AnimalEntity implements IMob, IFlinging {

    protected HoglinEntityMixin(EntityType<? extends AnimalEntity> p_i48568_1_, World p_i48568_2_) {
        super(p_i48568_1_, p_i48568_2_);
    }

    @Inject(at = @At("RETURN"), method = "<init>")
    private void initHoglin(EntityType<? extends HoglinEntity> entityType, World world, CallbackInfo callbackInfo){
        this.setCanPickUpLoot(true);
    }

    @Inject(at = @At("RETURN"), method = "isBreedingItem", cancellable = true)
    private void isBreedingItem(ItemStack itemStack, CallbackInfoReturnable<Boolean> callbackInfoReturnable){
        if(HoglinTasksHelper.isHoglinFoodItem(itemStack.getItem())){
            callbackInfoReturnable.setReturnValue(true);
        }
    }

    @Inject(at = @At("RETURN"), method = "func_230254_b_")
    private void onEntityInteract(PlayerEntity playerEntity, Hand hand, CallbackInfoReturnable<ActionResultType> callbackInfoReturnable){
        ActionResultType returnValue = callbackInfoReturnable.getReturnValue();
        if(returnValue.isSuccessOrConsume()){
            if(!this.world.isRemote){
                HoglinTasksHelper.setAteRecently(this);
            }
            else{
                this.playSound(SoundEvents.ENTITY_HOGLIN_AMBIENT, this.getSoundVolume(), this.getSoundPitch());
            }
        }
        else{
            if(this.world.isRemote){
                this.playSound(SoundEvents.ENTITY_HOGLIN_ANGRY, this.getSoundVolume(), this.getSoundPitch());
            }
        }
    }

    @Override
    public boolean func_230293_i_(ItemStack itemStack) {
        return this.world.getGameRules().getBoolean(GameRules.MOB_GRIEFING)
                && this.canPickUpLoot()
                && HoglinTasksHelper.canPickUpItemStack(this, itemStack);
    }

    @Override
    protected void updateEquipmentIfNeeded(ItemEntity itemEntity) {
        this.triggerItemPickupTrigger(itemEntity);
        HoglinTasksHelper.pickUpBreedingItem(this, itemEntity);
    }

    /*
    @Inject(at = @At("HEAD"), method = "handleStatusUpdate", cancellable = true)
    private void handleStatusUpdate(byte id, CallbackInfo callbackInfo){
        if(id == HoglinTasksHelper.GROWING_ID){
            for(int i = 0; i < 7; ++i) {
                double d0 = this.rand.nextGaussian() * 0.02D;
                double d1 = this.rand.nextGaussian() * 0.02D;
                double d2 = this.rand.nextGaussian() * 0.02D;
                this.world.addParticle(ParticleTypes.HAPPY_VILLAGER, this.getPosXRandom(1.0D), this.getPosYRandom() + 0.5D, this.getPosZRandom(1.0D), d0, d1, d2);
            }
            callbackInfo.cancel();
        }
    }

     */
}
