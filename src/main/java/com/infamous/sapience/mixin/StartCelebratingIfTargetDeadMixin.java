package com.infamous.sapience.mixin;

import com.infamous.sapience.util.BehaviorHelper;
import com.mojang.datafixers.kinds.Const;
import com.mojang.datafixers.kinds.IdF;
import com.mojang.datafixers.kinds.OptionalBox;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.StartCelebratingIfTargetDead;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.monster.piglin.Piglin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;
import java.util.function.BiPredicate;

@Mixin(StartCelebratingIfTargetDead.class)
public class StartCelebratingIfTargetDeadMixin {

    @Inject(method = "lambda$create$0", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/behavior/declarative/MemoryAccessor;setWithExpiry(Ljava/lang/Object;J)V"))
    private static void handleReturn(BehaviorBuilder.Instance<LivingEntity> instance, MemoryAccessor<IdF.Mu, LivingEntity> at, BiPredicate<LivingEntity, LivingEntity> wtd, MemoryAccessor<IdF.Mu, UUID> aa, int duration, MemoryAccessor<Const.Mu<Unit>, BlockPos> cl, MemoryAccessor<OptionalBox.Mu, Boolean> d, ServerLevel level, LivingEntity entity, long gameTime, CallbackInfoReturnable<Boolean> cir){
        if(entity instanceof Piglin piglin){
            BehaviorHelper.handleWantsToDance(piglin, instance.get(at), duration);
        }
    }
}
