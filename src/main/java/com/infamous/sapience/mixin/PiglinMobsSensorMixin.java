package com.infamous.sapience.mixin;

import com.google.common.collect.ImmutableList;
import com.infamous.sapience.mod.ModMemoryModuleTypes;
import com.infamous.sapience.util.GeneralHelper;
import com.infamous.sapience.util.PiglinTasksHelper;
import com.infamous.sapience.util.ReputationHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.PiglinMobsSensor;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.monster.HoglinEntity;
import net.minecraft.entity.monster.WitherSkeletonEntity;
import net.minecraft.entity.monster.piglin.PiglinTasks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.EntityPredicates;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(PiglinMobsSensor.class)
public class PiglinMobsSensorMixin {

    @Inject(at = @At("RETURN"), method = "update", cancellable = true)
    private void update(ServerWorld serverWorld, LivingEntity entityIn, CallbackInfo callbackInfo){
        Brain<?> brain = entityIn.getBrain();

        Optional<MobEntity> optionalNemesis = Optional.empty();
        Optional<HoglinEntity> optionalHuntableHoglin = Optional.empty();
        Optional<PlayerEntity> optionalPlayerNotGilded = Optional.empty();
        Optional<LivingEntity> optionalZombified = Optional.empty();

        Optional<HoglinEntity> optionalNearestVisibleAdultHoglin = Optional.empty();

        for(LivingEntity livingentity : brain.getMemory(MemoryModuleType.VISIBLE_MOBS).orElse(ImmutableList.of())) {
            if (livingentity instanceof HoglinEntity) {
                HoglinEntity hoglinentity = (HoglinEntity) livingentity;
                if (hoglinentity.func_234363_eJ_() && !optionalNearestVisibleAdultHoglin.isPresent()) {
                    optionalNearestVisibleAdultHoglin = Optional.of(hoglinentity);
                }
                if (!optionalHuntableHoglin.isPresent()
                        && hoglinentity.func_234365_eM_()
                        && GeneralHelper.isNotOnSameTeam(entityIn, hoglinentity)) {
                    optionalHuntableHoglin = Optional.of(hoglinentity);
                }
            } else if (livingentity instanceof PlayerEntity) {
                PlayerEntity playerentity = (PlayerEntity)livingentity;
                if (!optionalPlayerNotGilded.isPresent()
                        && EntityPredicates.CAN_HOSTILE_AI_TARGET.test(livingentity)
                        && !ReputationHelper.hasAcceptableAttire(livingentity, entityIn)
                        && GeneralHelper.isNotOnSameTeam(entityIn, livingentity)) {
                    optionalPlayerNotGilded = Optional.of(playerentity);
                }
            }  else if (optionalNemesis.isPresent() || !(livingentity instanceof WitherSkeletonEntity) && !(livingentity instanceof WitherEntity)) {
                if (!optionalZombified.isPresent()
                        && PiglinTasksHelper.isZombified(livingentity)
                        && GeneralHelper.isNotOnSameTeam(entityIn, livingentity)) {
                    optionalZombified = Optional.of(livingentity);
                }
            } else if(GeneralHelper.isNotOnSameTeam(entityIn, livingentity)){
                optionalNemesis = Optional.of((MobEntity)livingentity);
            }
        }
        brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_NEMESIS, optionalNemesis);
        brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_HUNTABLE_HOGLIN, optionalHuntableHoglin);
        brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED, optionalZombified);
        brain.setMemory(MemoryModuleType.NEAREST_TARGETABLE_PLAYER_NOT_WEARING_GOLD, optionalPlayerNotGilded);
        brain.setMemory(ModMemoryModuleTypes.NEAREST_VISIBLE_ADULT_HOGLIN.get(), optionalNearestVisibleAdultHoglin);
    }

}
