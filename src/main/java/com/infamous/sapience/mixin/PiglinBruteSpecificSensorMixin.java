package com.infamous.sapience.mixin;

import com.google.common.collect.ImmutableList;
import com.infamous.sapience.util.GeneralHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.PiglinBruteSpecificSensor;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(PiglinBruteSpecificSensor.class)
public class PiglinBruteSpecificSensorMixin {

    @Inject(at = @At("RETURN"), method = "doTick")
    private void update(ServerLevel worldIn, LivingEntity entityIn, CallbackInfo ci){
        Brain<?> brain = entityIn.getBrain();
        Optional<Mob> optionalNemesis = Optional.empty();

        for(LivingEntity livingentity : brain.getMemory(MemoryModuleType.VISIBLE_LIVING_ENTITIES).orElse(ImmutableList.of())) {
            if (livingentity instanceof WitherSkeleton || livingentity instanceof WitherBoss) {
                if(GeneralHelper.isNotOnSameTeam(entityIn, livingentity)){
                    optionalNemesis = Optional.of((Mob)livingentity);
                    break;
                }
            }
        }

        brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_NEMESIS, optionalNemesis);
    }
}
