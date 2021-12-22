package com.infamous.sapience.mixin;

import com.infamous.sapience.util.GeneralHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.sensing.Sensor;
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

    @Inject(at = @At("HEAD"), method = "isTargetable", cancellable = true)
    private void handleTargetable(LivingEntity target, CallbackInfoReturnable<Boolean> cir){
        EntityType<?> targetType = target.getType();
        cir.setReturnValue(!targetType.is(GeneralHelper.ZOGLINS_IGNORE)
                && Sensor.isEntityAttackable(this, target));

    }
}
