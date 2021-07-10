package com.infamous.sapience.mixin;

import com.google.common.collect.ImmutableList;
import com.infamous.sapience.util.GeneralHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.monster.ZoglinEntity;
import net.minecraft.util.EntityPredicates;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(ZoglinEntity.class)
public abstract class ZoglinEntityMixin extends MonsterEntity {

    protected ZoglinEntityMixin(EntityType<? extends MonsterEntity> type, World worldIn) {
        super(type, worldIn);
    }

    @Inject(at = @At("HEAD"), method = "func_234338_k_", cancellable = true)
    private void wasHurtBy(LivingEntity attacker, CallbackInfo ci){
        if(this.isOnSameTeam(attacker)){
            ci.cancel();
        }
    }

    @Inject(at = @At("HEAD"), method = "func_234335_eQ_", cancellable = true)
    private void findNearestAttackableTarget(CallbackInfoReturnable<Optional<? extends LivingEntity>> cir){
        cir.setReturnValue(
                this.getBrain().getMemory(MemoryModuleType.VISIBLE_MOBS)
                        .orElse(ImmutableList.of())
                        .stream()
                        .filter((visibleEntity) -> canZoglinAttack(this, visibleEntity))
                        .findFirst()
        );
    }

    private static boolean canZoglinAttack(LivingEntity attacker, LivingEntity target) {
        return !(target instanceof ZoglinEntity)
                && !(target instanceof CreeperEntity)
                && EntityPredicates.CAN_HOSTILE_AI_TARGET.test(target)
                && GeneralHelper.isNotOnSameTeam(attacker, target);
    }
}
