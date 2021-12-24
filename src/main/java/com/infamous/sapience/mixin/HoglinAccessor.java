package com.infamous.sapience.mixin;

import com.google.common.collect.ImmutableList;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Hoglin.class)
public interface HoglinAccessor {
    @Accessor("MEMORY_TYPES")
    static ImmutableList<MemoryModuleType<?>> getMEMORY_TYPES() {
        throw new AssertionError();
    }

    @Accessor("SENSOR_TYPES")
    static ImmutableList<? extends SensorType<? extends Sensor<? super Hoglin>>> getSENSOR_TYPES() {
        throw new AssertionError();
    }

}
