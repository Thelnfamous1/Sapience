package com.infamous.sapience.mixin;

import com.infamous.sapience.util.HoglinTasksHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.hoglin.HoglinBase;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Hoglin.class)
public abstract class HoglinEntityMixin extends Animal {

    protected HoglinEntityMixin(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(at = @At("RETURN"), method = "isFood", cancellable = true)
    private void isBreedingItem(ItemStack itemStack, CallbackInfoReturnable<Boolean> callbackInfoReturnable){
        if(HoglinTasksHelper.isHoglinFoodItem(itemStack)){
            callbackInfoReturnable.setReturnValue(true);
        }
    }

    @Override
    public boolean wantsToPickUp(ItemStack itemStack) {
        return this.level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)
                && this.canPickUpLoot()
                && HoglinTasksHelper.canPickUpItemStack(this, itemStack);
    }

    @Override
    protected void pickUpItem(ItemEntity itemEntity) {
        this.onItemPickup(itemEntity);
        HoglinTasksHelper.pickUpBreedingItem(this, itemEntity);
    }
}
