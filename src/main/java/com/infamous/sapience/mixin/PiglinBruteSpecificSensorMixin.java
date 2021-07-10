package com.infamous.sapience.mixin;

import com.google.common.collect.ImmutableList;
import com.infamous.sapience.util.GeneralHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.PiglinBruteSpecificSensor;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.monster.WitherSkeletonEntity;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(PiglinBruteSpecificSensor.class)
public class PiglinBruteSpecificSensorMixin {

    @Inject(at = @At("RETURN"), method = "update")
    private void update(ServerWorld worldIn, LivingEntity entityIn, CallbackInfo ci){
        Brain<?> brain = entityIn.getBrain();
        Optional<MobEntity> optionalNemesis = Optional.empty();

        for(LivingEntity livingentity : brain.getMemory(MemoryModuleType.VISIBLE_MOBS).orElse(ImmutableList.of())) {
            if (livingentity instanceof WitherSkeletonEntity || livingentity instanceof WitherEntity) {
                if(GeneralHelper.isNotOnSameTeam(entityIn, livingentity)){
                    optionalNemesis = Optional.of((MobEntity)livingentity);
                    break;
                }
            }
        }

        brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_NEMESIS, optionalNemesis);
    }
}
