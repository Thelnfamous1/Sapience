package com.infamous.sapience.mixin;

import net.minecraft.world.entity.ai.behavior.AnimalMakeLove;
import net.minecraft.world.entity.animal.Animal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Optional;

@Mixin(AnimalMakeLove.class)
public interface AnimalMakeLoveAccessor {

    @Invoker("getBreedTarget")
    Animal callGetBreedTarget(Animal animal);

    @Invoker("findValidBreedPartner")
    Optional<? extends Animal> callFindValidBreedPartner(Animal animal);

    @Accessor("spawnChildAtTime")
    long getSpawnChildAtTime();
}
