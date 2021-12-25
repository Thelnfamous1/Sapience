package com.infamous.sapience.mixin;

import com.infamous.sapience.mod.ModMemoryModuleTypes;
import com.infamous.sapience.util.BrainHelper;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Collection;

@Mixin(Brain.class)
public class BrainMixin {

    // Potential Forge PR
    // Allows for modification of a mob's memory types before they are sent to the Brain.Provider
    @ModifyVariable(at = @At("STORE"), method = "provider", argsOnly = true)
    private static Collection<? extends MemoryModuleType<?>> modifyMemoryTypes(Collection<? extends MemoryModuleType<?>> memoryTypes){
        if(memoryTypes == PiglinAccessor.getMEMORY_TYPES()){
            return BrainHelper.addMemoryModules(memoryTypes,
                    ModMemoryModuleTypes.BREEDING_TARGET.get(),
                    ModMemoryModuleTypes.FED_RECENTLY.get(),
                    ModMemoryModuleTypes.NEAREST_VISIBLE_ADULT_HOGLIN.get());
        }
        else if(memoryTypes == HoglinAccessor.getMEMORY_TYPES()){
            return BrainHelper.addMemoryModules(memoryTypes,
                    MemoryModuleType.ATE_RECENTLY,
                    MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM);
        }
        return memoryTypes;
    }

    // Potential Forge PR
    // Allows for modification of a mob's sensor types before they are sent to the Brain.Provider
    @ModifyVariable(at = @At("STORE"), method = "provider", argsOnly = true)
    private static Collection<? extends SensorType<? extends Sensor<?>>> modifySensorTypes(Collection<? extends SensorType<? extends Sensor<?>>> sensorTypes){
        if(sensorTypes == HoglinAccessor.getSENSOR_TYPES()){
            return BrainHelper.addSensorTypes(sensorTypes, SensorType.NEAREST_ITEMS);
        }
        return sensorTypes;
    }
}
