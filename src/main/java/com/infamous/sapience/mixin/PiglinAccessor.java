package com.infamous.sapience.mixin;

import com.google.common.collect.ImmutableList;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.piglin.Piglin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Piglin.class)
public interface PiglinAccessor {
    @Accessor("MEMORY_TYPES")
    static ImmutableList<MemoryModuleType<?>>  getMEMORY_TYPES() {
        throw new AssertionError();
    }
}
