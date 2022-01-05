package com.infamous.sapience.util;

import com.google.common.collect.ImmutableList;
import com.infamous.sapience.Sapience;
import net.minecraft.client.model.PiglinModel;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class ReflectionHelper {

    private static final Map<String, Method> CACHED_METHODS = new HashMap<>();

    private static final Map<String, Field> CACHED_FIELDS = new HashMap<>();

    public static Optional<Object> callMethod(String methodName, Object invokeOn, Class<?> inClass, Object[] parameters, Class<?> [] parameterTypes){
        Method method = CACHED_METHODS.computeIfAbsent(methodName, k -> ObfuscationReflectionHelper.findMethod(inClass, methodName, parameterTypes));
        try {
            return Optional.ofNullable(method.invoke(invokeOn, parameters));
        } catch (IllegalAccessException | InvocationTargetException e) {
            Sapience.LOGGER.info("Reflection error for method {}! Invoked on {} with parameter {}", methodName, inClass, Arrays.toString(parameterTypes));
            return Optional.empty();
        }
    }

    public static Optional<Object> accessField(String fieldName, Object accessFrom, Class<?> inClass){
        Field field = CACHED_FIELDS.computeIfAbsent(fieldName, k -> ObfuscationReflectionHelper.findField(inClass, fieldName));
        try {
            return Optional.ofNullable(field.get(accessFrom));
        } catch (IllegalAccessException e) {
            Sapience.LOGGER.info("Reflection error for field {}! Invoked on {}", fieldName, inClass);
            return Optional.empty();
        }
    }

    public static void callUsePlayerItem(Animal animal, Player player, InteractionHand hand, ItemStack stack) {
        callMethod("m_142075_", animal, Animal.class, new Object[]{player, hand, stack}, new Class<?>[]{Player.class, InteractionHand.class, ItemStack.class});
    }

    public static long getSpawnChildAtTime(AnimalMakeLove behavior){
        return (long)accessField("f_22389_", behavior, behavior.getClass()).get();
    }

    public static int getCelebrateDuration(StartCelebratingIfTargetDead behavior){
        return (int)accessField("f_24219_", behavior, behavior.getClass()).get();
    }

    public static EntityType<? extends Animal> getPartnerType(AnimalMakeLove behavior){
        return (EntityType<? extends Animal>) accessField("f_22387_", behavior, behavior.getClass()).get();
    }

    public static Map<Integer, Map<Activity, Set<Behavior<? extends LivingEntity>>>> getAvailableBehaviorsByPriority(Brain<?> brain){
        return (Map<Integer, Map<Activity, Set<Behavior<? extends LivingEntity>>>>) accessField("f_21845_", brain, brain.getClass()).get();
    }


    public static ShufflingList<Behavior<?>> getBehaviors(GateBehavior<?> gateBehavior){
        return (ShufflingList<Behavior<?>>) accessField("f_22871_", gateBehavior, GateBehavior.class).get();
    }

    public static ImmutableList<MemoryModuleType<?>> getMEMORY_TYPES(@Nullable Piglin piglin) {
        return (ImmutableList<MemoryModuleType<?>>) accessField("f_34672_", piglin, Piglin.class).get();
    }

    public static ImmutableList<MemoryModuleType<?>> getMEMORY_TYPES(@Nullable Hoglin hoglin) {
        return (ImmutableList<MemoryModuleType<?>>) accessField("f_34481_", hoglin, Hoglin.class).get();
    }

    public static ImmutableList<? extends SensorType<? extends Sensor<? super Hoglin>>> getSENSOR_TYPES(@Nullable Hoglin hoglin) {
        return (ImmutableList<? extends SensorType<? extends Sensor<? super Hoglin>>>) accessField("f_34480_", hoglin, Hoglin.class).get();
    }

    public static PartPose getHeadDefault(PiglinModel<?> piglinModel){
        return (PartPose) accessField("f_103338_", piglinModel, PiglinModel.class).get();
    }

    public static PartPose getLeftArmDefault(PiglinModel<?> piglinModel){
        return (PartPose) accessField("f_103333_", piglinModel, PiglinModel.class).get();
    }

    public static PartPose getRightArmDefault(PiglinModel<?> piglinModel){
        return (PartPose) accessField("f_103334_", piglinModel, PiglinModel.class).get();
    }

    public static boolean callCanReplaceCurrentItem(Piglin piglin, ItemStack stack){
        return (boolean) callMethod("m_34787_", piglin, Piglin.class, new Object[]{stack}, new Class<?>[]{ItemStack.class}).get();
    }

    public static MemoryModuleType<?> getMemoryTypeToErase(EraseMemoryIf<?> behavior){
        return (MemoryModuleType<?>) accessField("f_22857_", behavior, EraseMemoryIf.class).get();
    }

    public static boolean getDead(Mob mob){
        return (boolean) accessField("f_20890_", mob, Mob.class).get();
    }

    public static void callSetAngerTargetIfCloserThanCurrent(AbstractPiglin piglin, LivingEntity target){
        callMethod("m_34962_", (PiglinAi)null, PiglinAi.class, new Object[]{piglin, target}, new Class[]{AbstractPiglin.class, LivingEntity.class});
    }

}
