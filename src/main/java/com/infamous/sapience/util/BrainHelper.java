package com.infamous.sapience.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.infamous.sapience.mixin.BrainAccessor;
import com.infamous.sapience.mixin.GateBehaviorAccessor;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.GateBehavior;
import net.minecraft.world.entity.schedule.Activity;

import java.util.Arrays;
import java.util.List;

public class BrainHelper {

    public static ImmutableList<? extends Pair<Integer, ? extends Behavior<?>>> createPriorityPairs(int priorityStart, List<? extends Behavior<?>> tasks) {
        int priorityIndex = priorityStart;
        ImmutableList.Builder<Pair<Integer, ? extends Behavior<?>>> priorityPairs = ImmutableList.builder();

        for(Behavior<?> task : tasks) {
            priorityPairs.add(Pair.of(priorityIndex++, task));
        }

        return priorityPairs.build();
    }

    public static BrainAccessor castToAccessor(Brain<?> brain) {
        return (BrainAccessor)brain;
    }

    public static void addPrioritizedBehaviors(Activity activity, List<? extends Pair<Integer, ? extends Behavior<?>>> prioritizedTasks, Brain<?> brain) {
        BrainAccessor brainAccessor = castToAccessor(brain);

        for(Pair<Integer, ? extends Behavior<?>> pair : prioritizedTasks) {
            brainAccessor.getAvailableBehaviorsByPriority()
                    .computeIfAbsent(pair.getFirst(), (p) -> Maps.newHashMap())
                    .computeIfAbsent(activity, (a) -> Sets.newLinkedHashSet())
                    .add(pair.getSecond());
        }
    }

    public static void addAdditionalTasks(Brain<?> brain, Activity activityType, int priorityStart, Behavior<?>... tasks) {
        List<? extends Behavior<?>> additionalCoreTasks = Arrays.asList(tasks);

        ImmutableList<? extends Pair<Integer, ? extends Behavior<?>>> prioritizedAdditionalCoreTasks =
                createPriorityPairs(priorityStart, additionalCoreTasks);

        addPrioritizedBehaviors(activityType, prioritizedAdditionalCoreTasks, brain);
    }

    public static GateBehaviorAccessor castToAccessor(GateBehavior<?> gateBehavior){
        return (GateBehaviorAccessor) gateBehavior;
    }

    public static void addToGateBehavior(GateBehavior<?> gateBehavior, Pair<Behavior<?>, Integer>... weightedTasks){
        GateBehaviorAccessor accessor = castToAccessor(gateBehavior);
        for(Pair<Behavior<?>, Integer> weightedBehavior : weightedTasks){
            accessor.getBehaviors().add(weightedBehavior.getFirst(), weightedBehavior.getSecond());
        }
    }
}
