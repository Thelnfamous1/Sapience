package com.infamous.sapience.mixin;

import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MemoryAccessor.class)
public interface MemoryAccessorAccessor {

    @Accessor
    MemoryModuleType<?> getMemoryType();
}
