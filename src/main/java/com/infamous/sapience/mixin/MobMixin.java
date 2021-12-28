package com.infamous.sapience.mixin;

import com.infamous.sapience.util.HoglinTasksHelper;
import com.infamous.sapience.util.ReputationHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mob.class)
public abstract class MobMixin extends LivingEntity {

    @Shadow public abstract boolean canPickUpLoot();

    @Shadow protected abstract void pickUpItem(ItemEntity itemEntity);

    @Shadow public abstract boolean wantsToPickUp(ItemStack stack);

    protected MobMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;aiStep()V", shift = At.Shift.AFTER), method = "aiStep", cancellable = true)
    private void handlePickUp(CallbackInfo ci){
        if(this.cast() instanceof Hoglin || this.cast() instanceof Piglin){
            ci.cancel();
            this.customPickUp();
        }
    }

    private Mob cast() {
        return (Mob) (Object) this;
    }

    private void customPickUp() {
        this.level.getProfiler().push("looting");
        if (!this.level.isClientSide && this.canPickUpLoot() && this.isAlive() && !this.dead && net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(this.level, this)) {
            for(ItemEntity itementity : this.level.getEntitiesOfClass(ItemEntity.class, this.getBoundingBox().inflate(1.0D, 0.0D, 1.0D))) {
                if (!itementity.isRemoved()
                        && !itementity.getItem().isEmpty()
                        && !itementity.hasPickUpDelay()
                        && this.customWantsToPickUp(itementity)) {
                    this.customPickUpItem(itementity);
                    if(this.cast() instanceof Piglin piglin && itementity.getThrower() != null && this.level instanceof ServerLevel){
                        Entity throwerEntity = ((ServerLevel) this.level).getEntity(itementity.getThrower());
                        ReputationHelper.setPreviousInteractor(piglin, throwerEntity);
                    }
                }
            }
        }
        this.level.getProfiler().pop();
    }

    private void customPickUpItem(ItemEntity itemEntity) {
        if(this.cast() instanceof Hoglin hoglin){
            this.onItemPickup(itemEntity);
            HoglinTasksHelper.pickUpBreedingItem(hoglin, itemEntity);
        } else{
            this.pickUpItem(itemEntity);
        }
    }

    private boolean customWantsToPickUp(ItemEntity itemEntity) {
        return this.cast() instanceof Hoglin hoglin ? HoglinTasksHelper.wantsToPickUp(hoglin, itemEntity.getItem()) : this.wantsToPickUp(itemEntity.getItem());
    }

}
