package com.infamous.sapience.mixin;

import com.google.common.collect.ImmutableList;
import com.infamous.sapience.mod.ModMemoryModuleTypes;
import com.infamous.sapience.util.GeneralHelper;
import com.infamous.sapience.util.PiglinTasksHelper;
import com.infamous.sapience.util.ReputationHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
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

        for(LivingEntity livingentity : brain.getMemory(MemoryModuleType.VISIBLE_LIVING_ENTITIES).orElse(ImmutableList.of())) {
            if (livingentity instanceof Hoglin) {
                Hoglin hoglinentity = (Hoglin) livingentity;
                if (hoglinentity.isAdult() && !optionalNearestVisibleAdultHoglin.isPresent()) {
                    optionalNearestVisibleAdultHoglin = Optional.of(hoglinentity);
                }
                if (!optionalHuntableHoglin.isPresent()
                        && hoglinentity.canBeHunted()
                        && GeneralHelper.isNotOnSameTeam(entityIn, hoglinentity)) {
                    optionalHuntableHoglin = Optional.of(hoglinentity);
                }
            } else if (livingentity instanceof Player) {
                Player playerentity = (Player)livingentity;
                if (!optionalPlayerNotGilded.isPresent()
                        && EntitySelector.ATTACK_ALLOWED.test(livingentity)
                        && !ReputationHelper.hasAcceptableAttire(livingentity, entityIn)
                        && GeneralHelper.isNotOnSameTeam(entityIn, livingentity)) {
                    optionalPlayerNotGilded = Optional.of(playerentity);
                }
            }  else if (optionalNemesis.isPresent() || !(livingentity instanceof WitherSkeleton) && !(livingentity instanceof WitherBoss)) {
                if (!optionalZombified.isPresent()
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
