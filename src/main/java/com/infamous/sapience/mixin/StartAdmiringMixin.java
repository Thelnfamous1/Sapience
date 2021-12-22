package com.infamous.sapience.mixin;

import com.infamous.sapience.util.PiglinTasksHelper;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.piglin.StartAdmiringItemIfSeen;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(StartAdmiringItemIfSeen.class)
public class StartAdmiringMixin {

    @Inject(at = @At("RETURN"), method = "checkExtraStartConditions", cancellable = true)
    private void shouldExecute(ServerLevel serverWorld, Piglin owner, CallbackInfoReturnable<Boolean> callbackInfoReturnable){
        Optional<ItemEntity> wantedItemEntity = owner.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM);
        if(wantedItemEntity.isPresent()){
            ItemEntity itementity = wantedItemEntity.get();
            boolean piglinLoved = PiglinTasksHelper.isPiglinLoved(itementity.getItem());
            boolean piglinGreed = PiglinTasksHelper.isBarterItem(itementity.getItem());
            boolean piglinFood = PiglinTasksHelper.isPiglinFoodItem(itementity.getItem());
            boolean isAdmirable = piglinLoved || piglinFood || piglinGreed;
            callbackInfoReturnable.setReturnValue(isAdmirable);
        }
        else{
            callbackInfoReturnable.setReturnValue(false);
        }
    }
}
