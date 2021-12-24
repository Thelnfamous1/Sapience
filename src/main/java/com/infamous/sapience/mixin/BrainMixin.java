package com.infamous.sapience.mixin;

import com.infamous.sapience.mod.ModMemoryModuleTypes;
import com.infamous.sapience.util.BrainHelper;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;

@Mixin(Brain.class)
public class BrainMixin {

    // Potential Forge PR
    @Inject(at = @At("HEAD"), method = "provider")
    private static void handleProvider(Collection<? extends MemoryModuleType<?>> memoryModuleTypes, Collection<? extends SensorType<? extends Sensor<?>>> sensorTypes, CallbackInfoReturnable<Brain.Provider<?>> cir){
        if(memoryModuleTypes == PiglinAccessor.getMEMORY_TYPES()){
            memoryModuleTypes = BrainHelper.addMemoryModules(memoryModuleTypes,
                    ModMemoryModuleTypes.BREEDING_TARGET.get(),
                    ModMemoryModuleTypes.FED_RECENTLY.get(),
                    ModMemoryModuleTypes.NEAREST_VISIBLE_ADULT_HOGLIN.get());
        }
        else if(memoryModuleTypes == HoglinAccessor.getMEMORY_TYPES()){
            memoryModuleTypes = BrainHelper.addMemoryModules(memoryModuleTypes,
                    MemoryModuleType.ATE_RECENTLY,
                    MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM);
        }
        if(sensorTypes == HoglinAccessor.getSENSOR_TYPES()){
            sensorTypes = BrainHelper.addSensorTypes(sensorTypes, SensorType.NEAREST_ITEMS);
        }
    }
}
