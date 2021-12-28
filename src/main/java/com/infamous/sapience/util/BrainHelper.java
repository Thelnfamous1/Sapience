package com.infamous.sapience.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.GateBehavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class BrainHelper {

    public static ImmutableList<? extends Pair<Integer, ? extends Behavior<?>>> createPriorityPairs(int priorityStart, List<? extends Behavior<?>> tasks) {
        int priorityIndex = priorityStart;
        ImmutableList.Builder<Pair<Integer, ? extends Behavior<?>>> priorityPairs = ImmutableList.builder();

        for(Behavior<?> task : tasks) {
            priorityPairs.add(Pair.of(priorityIndex++, task));
        }

        return priorityPairs.build();
    }
    public static void addPrioritizedBehaviors(Activity activity, List<? extends Pair<Integer, ? extends Behavior<?>>> prioritizedTasks, Brain<?> brain) {

        for(Pair<Integer, ? extends Behavior<?>> pair : prioritizedTasks) {
            ReflectionHelper.getAvailableBehaviorsByPriority(brain)
                    .computeIfAbsent(pair.getFirst(), (p) -> Maps.newHashMap())
                    .computeIfAbsent(activity, (a) -> Sets.newLinkedHashSet())
                    .add(pair.getSecond());
        }
    }

    public static Optional<Behavior<?>> retrieveFirstAvailableTask(Brain<?> brain, Activity activityType, int priority, Predicate<Behavior<?>> predicate){
        return ReflectionHelper.getAvailableBehaviorsByPriority(brain)
                .get(priority)
                .get(activityType)
                .stream()
                .filter(predicate)
                .findFirst();
    }

    public static void addAdditionalTasks(Brain<?> brain, Activity activityType, int priorityStart, Behavior<?>... tasks) {
        List<? extends Behavior<?>> additionalCoreTasks = Arrays.asList(tasks);

        ImmutableList<? extends Pair<Integer, ? extends Behavior<?>>> prioritizedAdditionalCoreTasks =
                createPriorityPairs(priorityStart, additionalCoreTasks);

        addPrioritizedBehaviors(activityType, prioritizedAdditionalCoreTasks, brain);
    }

    public static void addToGateBehavior(GateBehavior<?> gateBehavior, Pair<Behavior<?>, Integer>... weightedTasks){
        for(Pair<Behavior<?>, Integer> weightedBehavior : weightedTasks){
            ReflectionHelper.getBehaviors(gateBehavior).add(weightedBehavior.getFirst(), weightedBehavior.getSecond());
        }
    }

    public static Collection<? extends SensorType<? extends Sensor<?>>> addSensorTypes(Collection<? extends SensorType<? extends Sensor<?>>> original, SensorType<? extends Sensor<?>>... additional) {
        ImmutableList.Builder<SensorType<? extends Sensor<?>>> builder = new ImmutableList.Builder<>();
        builder.addAll(original);
        for(SensorType<? extends Sensor<?>> sensorType : additional){
            builder.add(sensorType);
        }
        return builder.build();
    }

    public static Collection<? extends MemoryModuleType<?>> addMemoryModules(Collection<? extends MemoryModuleType<?>> original, MemoryModuleType<?>... additional) {
        ImmutableList.Builder<MemoryModuleType<?>> builder = new ImmutableList.Builder<>();
        builder.addAll(original);
        for(MemoryModuleType<?> memoryModuleType : additional){
            builder.add(memoryModuleType);
        }
        return builder.build();
    }
}
