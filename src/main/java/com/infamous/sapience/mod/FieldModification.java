package com.infamous.sapience.mod;

import com.google.common.collect.ImmutableList;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.entity.monster.HoglinEntity;
import net.minecraft.entity.monster.piglin.PiglinEntity;

public class FieldModification {

    public static void init(){

        PiglinEntity.field_234414_c_ = new ImmutableList.Builder<MemoryModuleType<?>>()
                .addAll(PiglinEntity.field_234414_c_)
                .add(ModMemoryModuleTypes.BREEDING_TARGET.get())
                .add(ModMemoryModuleTypes.FED_RECENTLY.get())
                .add(ModMemoryModuleTypes.NEAREST_VISIBLE_ADULT_HOGLIN.get())
                .build();


        HoglinEntity.field_234354_bu_ = new ImmutableList.Builder<SensorType<? extends Sensor<? super HoglinEntity>>>()
                .addAll(HoglinEntity.field_234354_bu_)
                .add(SensorType.NEAREST_ITEMS)
                .build();


        HoglinEntity.field_234355_bv_ = new ImmutableList.Builder<MemoryModuleType<?>>()
                .addAll(HoglinEntity.field_234355_bv_)
                .add(MemoryModuleType.ATE_RECENTLY)
                .add(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM)
                /*
                .add(MemoryModuleType.ADMIRING_ITEM)
                .add(MemoryModuleType.TIME_TRYING_TO_REACH_ADMIRE_ITEM)
                .add(MemoryModuleType.ADMIRING_DISABLED)

                 */
                .build();
    }
}
