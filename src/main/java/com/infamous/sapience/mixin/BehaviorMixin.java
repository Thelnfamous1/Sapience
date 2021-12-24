package com.infamous.sapience.mixin;

import com.infamous.sapience.util.BehaviorHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Behavior.class)
public abstract class BehaviorMixin<E extends LivingEntity> {

    @Shadow protected abstract boolean hasRequiredMemories(E p_22544_);

    @Shadow protected abstract boolean checkExtraStartConditions(ServerLevel p_22538_, E p_22539_);

    @Shadow private Behavior.Status status;

    @Shadow @Final private int minDuration;

    @Shadow @Final private int maxDuration;

    @Shadow private long endTimestamp;

    @Shadow protected abstract void start(ServerLevel p_22540_, E p_22541_, long p_22542_);

    // Potential Forge PR
    @Inject(at = @At("HEAD"), method = "tryStart", cancellable = true)
    private void handleTryStart(ServerLevel serverLevel, E entity, long gameTime, CallbackInfoReturnable<Boolean> cir){
        if (this.hasRequiredMemories(entity) && enhancedCESC(serverLevel, entity)) {
            this.status = Behavior.Status.RUNNING;
            int i = this.minDuration + serverLevel.getRandom().nextInt(this.maxDuration + 1 - this.minDuration);
            this.endTimestamp = gameTime + (long)i;
            if(this.preStart(serverLevel, entity, gameTime)){
                this.start(serverLevel, entity, gameTime);
                this.postStart(serverLevel, entity, gameTime);
            }
            cir.setReturnValue(true);
        } else {
            cir.setReturnValue(false);
        }
    }

    // Used to run custom "start" logic instead of the original behavior's start logic
    private boolean preStart(ServerLevel serverLevel, E entity, long gameTime){
        return BehaviorHelper.handleSapienceBehaviorPreStart(this.cast(), serverLevel, entity);
    }

    // Potential Forge PR
    // Used to run additional "tick" logic after the original behavior's tick logic
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/behavior/Behavior;tick(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;J)V", shift = At.Shift.AFTER), method = "tickOrStop")
    private void postTick(ServerLevel serverLevel, E entity, long gameTime, CallbackInfo ci){
        BehaviorHelper.handleSapienceBehaviorPostTick(this.cast(), serverLevel, entity, gameTime);
    }

    // Potential Forge PR
    // Used to run additional "start" logic after the original behavior's start logic
    private void postStart(ServerLevel serverLevel, E entity, long gameTime){
        BehaviorHelper.handleSapienceBehaviorPostStart(this.cast(), serverLevel, entity);
    }

    private boolean enhancedCESC(ServerLevel serverLevel, E entity) {
        return BehaviorHelper.handleSapienceBehaviorCESC(this.cast(), entity, this.checkExtraStartConditions(serverLevel, entity));
    }

    private Behavior<?> cast(){
        //noinspection ConstantConditions
        return (Behavior<?>)(Object)this;
    }

}
