package com.infamous.sapience.mixin;

import com.infamous.sapience.mod.IShakesHead;
import com.infamous.sapience.util.AgeableHelper;
import com.infamous.sapience.util.GreedHelper;
import com.infamous.sapience.util.PiglinTasksHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.monster.piglin.AbstractPiglinEntity;
import net.minecraft.entity.monster.piglin.PiglinAction;
import net.minecraft.entity.monster.piglin.PiglinEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PiglinEntity.class)
public abstract class PiglinEntityMixin extends AbstractPiglinEntity implements IShakesHead {
    private static final DataParameter<Integer> SHAKE_HEAD_TICKS = EntityDataManager.createKey(PiglinEntity.class, DataSerializers.VARINT);
    private static final int NO_TICKS = 40;


    public PiglinEntityMixin(EntityType<? extends AbstractPiglinEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/monster/piglin/PiglinTasks;func_234480_a_(Lnet/minecraft/item/Item;)Z"), method = "func_234424_eM_", cancellable = true)
    private void canPiglinAdmire(CallbackInfoReturnable<PiglinAction> callbackInfoReturnable){
        boolean isPiglinLoved = PiglinTasksHelper.isPiglinLoved(this.getHeldItemOffhand().getItem());
        boolean isPiglinGreedItem = PiglinTasksHelper.isPiglinGreedItem(this.getHeldItemOffhand().getItem());
        if(isPiglinLoved || isPiglinGreedItem){
            callbackInfoReturnable.setReturnValue(PiglinAction.ADMIRING_ITEM);
        }
    }


    @Inject(at = @At(value = "HEAD"), method = "func_234436_k_", cancellable = true)
    private void onAddToInventory(ItemStack stack, CallbackInfoReturnable<ItemStack> callbackInfoReturnable) {
        if(PiglinTasksHelper.isPiglinCurrencyRelated(stack.getItem())){
            CompoundNBT compoundNBT = stack.getOrCreateTag();
            ItemStack remainder = GreedHelper.addGreedItemToGreedInventory(this, stack, compoundNBT.getBoolean(GreedHelper.BARTERED));
            callbackInfoReturnable.setReturnValue(remainder);
        }
    }

    @Inject(at = @At("RETURN"), method = "func_230254_b_", cancellable = true)
    private void processInteraction(PlayerEntity playerEntity, Hand handIn, CallbackInfoReturnable<ActionResultType> callbackInfoReturnable){
        ActionResultType actionResultType = callbackInfoReturnable.getReturnValue();
        if(!actionResultType.isSuccessOrConsume()){
            actionResultType = PiglinTasksHelper.getGreedActionResultType(this, playerEntity, handIn, actionResultType);
            actionResultType = PiglinTasksHelper.getAgeableActionResultType(this, playerEntity, handIn, actionResultType);
            if(!actionResultType.isSuccessOrConsume()){
                this.setShakeHeadTicks(NO_TICKS);
                if(this.world.isRemote){
                    this.playSound(SoundEvents.ENTITY_PIGLIN_ANGRY, this.getSoundVolume(), this.getSoundPitch());
                }
                else{
                    this.world.setEntityState(this, (byte) AgeableHelper.DECLINE_ID);
                }
            }
            else{
                if(this.world.isRemote){
                    this.playSound(SoundEvents.ENTITY_PIGLIN_CELEBRATE, this.getSoundVolume(), this.getSoundPitch());
                }
                else{
                    this.world.setEntityState(this, (byte) AgeableHelper.ACCEPT_ID);
                }
            }
            callbackInfoReturnable.setReturnValue(actionResultType);
        }
    }

    @Inject(at = @At("RETURN"), method = "dropSpecialItems", cancellable = true)
    private void dropSpecialItems(DamageSource damageSource, int lootingLevel, boolean recentlyHit, CallbackInfo callbackInfo){
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
            this.spawnParticles(ParticleTypes.HEART);
        }
        else if(id == AgeableHelper.GROWING_ID){
            this.spawnParticles(ParticleTypes.COMPOSTER);
        }
        else if(id == AgeableHelper.ANGER_ID){
            this.spawnParticles(ParticleTypes.ANGRY_VILLAGER);
        }
        else if(id == AgeableHelper.DECLINE_ID){
            this.spawnParticles(ParticleTypes.SMOKE);
        }
        else if(id == AgeableHelper.ACCEPT_ID){
            this.spawnParticles(ParticleTypes.HAPPY_VILLAGER);
        }
        else{
            super.handleStatusUpdate(id);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void spawnParticles(IParticleData particleData) {
        for(int i = 0; i < 5; ++i) {
            double randomXSpeed = this.rand.nextGaussian() * 0.02D;
            double randomYSpeed = this.rand.nextGaussian() * 0.02D;
            double randomZSpeed = this.rand.nextGaussian() * 0.02D;
            this.world.addParticle(particleData, this.getPosXRandom(1.0D), this.getPosYRandom() + 1.0D, this.getPosZRandom(1.0D), randomXSpeed, randomYSpeed, randomZSpeed);
        }
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
}
