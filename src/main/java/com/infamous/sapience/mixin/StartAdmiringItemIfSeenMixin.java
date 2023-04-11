package com.infamous.sapience.mixin;

import com.infamous.sapience.util.BehaviorHelper;
import com.infamous.sapience.util.PiglinTasksHelper;
import com.mojang.datafixers.kinds.Const;
import com.mojang.datafixers.kinds.IdF;
import com.mojang.datafixers.util.Unit;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.StartAdmiringItemIfSeen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StartAdmiringItemIfSeen.class)
public class StartAdmiringItemIfSeenMixin {

    @Inject(method = "lambda$create$0", at = @At("HEAD"), cancellable = true)
    private static void handleReturn(BehaviorBuilder.Instance<LivingEntity> instance, MemoryAccessor<IdF.Mu, ItemEntity> nvle, MemoryAccessor<Const.Mu<Unit>, Boolean> ai, int duration, ServerLevel level, LivingEntity entity, long gameTime, CallbackInfoReturnable<Boolean> cir){
        if(entity instanceof Piglin piglin){
            boolean canStartAdmiring = BehaviorHelper.canStartAdmiring(piglin, PiglinTasksHelper.isPiglinLoved(instance.get(nvle).getItem()));
            cir.setReturnValue(canStartAdmiring);
            if(canStartAdmiring){
                ai.setWithExpiry(true, duration);
            }
        }
    }
}
