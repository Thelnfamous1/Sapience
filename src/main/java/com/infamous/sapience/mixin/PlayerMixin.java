package com.infamous.sapience.mixin;

import com.infamous.sapience.util.HoglinTasksHelper;
import com.infamous.sapience.util.PiglinTasksHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity {

    protected PlayerMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @ModifyVariable(at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/entity/Entity;interact(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;"), method = "interactOn", ordinal = 1)
    private InteractionResult handleInteractOn(InteractionResult interactionResult, Entity entity, InteractionHand hand){
        if(entity instanceof Hoglin hoglin){
            HoglinTasksHelper.handleHoglinInteractPost(hoglin, this.cast(), hand, interactionResult);
        } else if(entity instanceof Piglin piglin){
            PiglinTasksHelper.handlePiglinInteractPost(piglin, this.cast(), interactionResult);

        }
        return interactionResult;
    }

    private Player cast() {
        return (Player)(Object)this;
    }
}
