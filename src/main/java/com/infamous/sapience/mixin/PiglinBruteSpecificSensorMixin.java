package com.infamous.sapience.mixin;

import com.infamous.sapience.util.GeneralHelper;
import com.infamous.sapience.util.PiglinTasksHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.sensing.PiglinBruteSpecificSensor;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.WitherSkeleton;
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

        NearestVisibleLivingEntities nvle = brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).orElse(NearestVisibleLivingEntities.empty());
        Optional<Mob> optionalNemesis = nvle.findClosest(le ->
                PiglinTasksHelper.piglinsHate(le.getType())
                        && le instanceof Mob
                        && GeneralHelper.isNotOnSameTeam(entityIn, le))
                .map(Mob.class::cast);

        brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_NEMESIS, optionalNemesis);
    }
}
