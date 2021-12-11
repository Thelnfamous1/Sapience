package com.infamous.sapience.mixin;

import com.infamous.sapience.util.GeneralHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Zoglin;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(Zoglin.class)
public abstract class ZoglinEntityMixin extends Monster {

    protected ZoglinEntityMixin(EntityType<? extends Monster> type, Level worldIn) {
        super(type, worldIn);
    }

    @Inject(at = @At("HEAD"), method = "setAttackTarget", cancellable = true)
    private void wasHurtBy(LivingEntity attacker, CallbackInfo ci){
        if(this.isAlliedTo(attacker)){
            ci.cancel();
        }
    }

    @Inject(at = @At("HEAD"), method = "findNearestValidAttackTarget", cancellable = true)
    private void findNearestAttackableTarget(CallbackInfoReturnable<Optional<? extends LivingEntity>> cir){
        cir.setReturnValue(
                this.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)
                        .orElse(NearestVisibleLivingEntities.empty())
                        .findClosest((visibleEntity) -> canZoglinAttack(this, visibleEntity))
        );
    }

    private static boolean canZoglinAttack(LivingEntity attacker, LivingEntity target) {
        return !(target instanceof Zoglin)
                && !(target instanceof Creeper)
                && attacker.canAttack(target)
                && GeneralHelper.isNotOnSameTeam(attacker, target);
    }
}
