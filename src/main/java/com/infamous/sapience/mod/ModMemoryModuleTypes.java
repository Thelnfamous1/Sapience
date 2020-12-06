package com.infamous.sapience.mod;

import com.infamous.sapience.Sapience;
import com.mojang.serialization.Codec;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.monster.HoglinEntity;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Optional;

public class ModMemoryModuleTypes {

    public static final DeferredRegister<MemoryModuleType<?>> MEMORY_MODULE_TYPES = DeferredRegister.create(ForgeRegistries.MEMORY_MODULE_TYPES, Sapience.MODID);

    public static final RegistryObject<MemoryModuleType<MobEntity>> BREEDING_TARGET = MEMORY_MODULE_TYPES.register(
            "breeding_target", () -> new MemoryModuleType<>(Optional.empty())
    );

    public static final RegistryObject<MemoryModuleType<HoglinEntity>> NEAREST_VISIBLE_ADULT_HOGLIN = MEMORY_MODULE_TYPES.register(
            "nearest_visible_adult_hoglin", () -> new MemoryModuleType<>(Optional.empty())
    );

    public static final RegistryObject<MemoryModuleType<Boolean>> FED_RECENTLY = MEMORY_MODULE_TYPES.register(
            "fed_recently", () -> new MemoryModuleType<>(Optional.of(Codec.BOOL))
    );
}
