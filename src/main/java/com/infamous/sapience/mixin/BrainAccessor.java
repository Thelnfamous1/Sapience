package com.infamous.sapience.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.schedule.Activity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;
import java.util.Set;

@Mixin(Brain.class)
public interface BrainAccessor {

    @Accessor("availableBehaviorsByPriority")
    Map<Integer, Map<Activity, Set<Behavior<? extends LivingEntity>>>> getAvailableBehaviorsByPriority();

}
