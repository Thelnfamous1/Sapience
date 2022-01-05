package com.infamous.sapience.util;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;

import java.util.Optional;

public class BruteTasksHelper {

    public static void additionalSensorLogic(PiglinBrute entityIn) {
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
