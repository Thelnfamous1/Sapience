package com.infamous.sapience.mixin;

import com.infamous.sapience.util.PiglinTasksHelper;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.monster.piglin.AdmireItemTask;
import net.minecraft.entity.monster.piglin.PiglinEntity;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(AdmireItemTask.class)
public class AdmireItemTaskMixin {

    @Inject(at = @At("RETURN"), method = "shouldExecute", cancellable = true)
    private void shouldExecute(ServerWorld serverWorld, PiglinEntity owner, CallbackInfoReturnable<Boolean> callbackInfoReturnable){
        Optional<ItemEntity> wantedItemEntity = owner.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM);
        if(wantedItemEntity.isPresent()){
            ItemEntity itementity = wantedItemEntity.get();
            boolean piglinLoved = PiglinTasksHelper.isPiglinLoved(itementity.getItem().getItem());
            boolean piglinGreed = PiglinTasksHelper.isPiglinGreedItem(itementity.getItem().getItem());
            boolean piglinFood = PiglinTasksHelper.isPiglinFoodItem(itementity.getItem().getItem());
            boolean isAdmirable = piglinLoved || piglinFood || piglinGreed;
            callbackInfoReturnable.setReturnValue(isAdmirable);
        }
        else{
            callbackInfoReturnable.setReturnValue(false);
        }
    }
}
