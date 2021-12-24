package com.infamous.sapience;

import com.infamous.sapience.mod.ModMemoryModuleTypes;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Sapience.MODID)
public class Sapience
{
    // Directly reference a log4j logger.
    public static final String MODID = "sapience";
    public static final Logger LOGGER = LogManager.getLogger();

    public Sapience() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SapienceConfig.COMMON_SPEC);
        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModMemoryModuleTypes.MEMORY_MODULE_TYPES.register(eventBus);
    }
}
