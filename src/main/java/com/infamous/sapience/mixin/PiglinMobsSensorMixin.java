package com.infamous.sapience.mixin;

import com.infamous.sapience.mod.ModMemoryModuleTypes;
import com.infamous.sapience.util.GeneralHelper;
import com.infamous.sapience.util.PiglinTasksHelper;
import com.infamous.sapience.util.ReputationHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.sensing.PiglinSpecificSensor;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(PiglinSpecificSensor.class)
public class PiglinMobsSensorMixin {

    @Inject(at = @At("RETURN"), method = "doTick", cancellable = true)
    private void update(ServerLevel serverWorld, LivingEntity entityIn, CallbackInfo callbackInfo){
        Brain<?> brain = entityIn.getBrain();

        Optional<Mob> optionalNemesis = Optional.empty();
        Optional<Hoglin> optionalHuntableHoglin = Optional.empty();
        Optional<Player> optionalPlayerNotGilded = Optional.empty();
        Optional<LivingEntity> optionalZombified = Optional.empty();

        Optional<Hoglin> optionalNearestVisibleAdultHoglin = Optional.empty();

        NearestVisibleLivingEntities nearestvisiblelivingentities = brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).orElse(NearestVisibleLivingEntities.empty());

        for(LivingEntity livingentity : nearestvisiblelivingentities.findAll((p_186157_) -> true)){
            if (livingentity instanceof Hoglin hoglinentity) {
                if (hoglinentity.isAdult() && optionalNearestVisibleAdultHoglin.isEmpty()) {
                    optionalNearestVisibleAdultHoglin = Optional.of(hoglinentity);
                }
                if (optionalHuntableHoglin.isEmpty()
                        && hoglinentity.canBeHunted()
                        && GeneralHelper.isNotOnSameTeam(entityIn, hoglinentity)) {
                    optionalHuntableHoglin = Optional.of(hoglinentity);
                }
            } else if (livingentity instanceof Player playerentity) {
                if (optionalPlayerNotGilded.isEmpty()
                        && entityIn.canAttack(livingentity)
                        && !ReputationHelper.hasAcceptableAttire(livingentity, entityIn)
                        && GeneralHelper.isNotOnSameTeam(entityIn, livingentity)) {
                    optionalPlayerNotGilded = Optional.of(playerentity);
                }
            }  else if (optionalNemesis.isPresent() || !(livingentity instanceof WitherSkeleton) && !(livingentity instanceof WitherBoss)) {
                if (optionalZombified.isEmpty()
                        && PiglinTasksHelper.isZombified(livingentity)
                        && GeneralHelper.isNotOnSameTeam(entityIn, livingentity)) {
                    optionalZombified = Optional.of(livingentity);
                }
            } else if(GeneralHelper.isNotOnSameTeam(entityIn, livingentity)){
                optionalNemesis = Optional.of((Mob)livingentity);
            }
        }
        brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_NEMESIS, optionalNemesis);
        brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_HUNTABLE_HOGLIN, optionalHuntableHoglin);
        brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED, optionalZombified);
        brain.setMemory(MemoryModuleType.NEAREST_TARGETABLE_PLAYER_NOT_WEARING_GOLD, optionalPlayerNotGilded);
        brain.setMemory(ModMemoryModuleTypes.NEAREST_VISIBLE_ADULT_HOGLIN.get(), optionalNearestVisibleAdultHoglin);
    }

}
