package com.infamous.sapience.mod;

import com.google.common.collect.ImmutableList;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.piglin.Piglin;

public class FieldModification {

    public static void init(){

        Piglin.MEMORY_TYPES = new ImmutableList.Builder<MemoryModuleType<?>>()
                .addAll(Piglin.MEMORY_TYPES)
                .add(ModMemoryModuleTypes.BREEDING_TARGET.get())
                .add(ModMemoryModuleTypes.FED_RECENTLY.get())
                .add(ModMemoryModuleTypes.NEAREST_VISIBLE_ADULT_HOGLIN.get())
                .build();


        Hoglin.SENSOR_TYPES = new ImmutableList.Builder<SensorType<? extends Sensor<? super Hoglin>>>()
                .addAll(Hoglin.SENSOR_TYPES)
                .add(SensorType.NEAREST_ITEMS)
                .build();


        Hoglin.MEMORY_TYPES = new ImmutableList.Builder<MemoryModuleType<?>>()
                .addAll(Hoglin.MEMORY_TYPES)
                .add(MemoryModuleType.ATE_RECENTLY)
                .add(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM)
                .build();
    }
}
