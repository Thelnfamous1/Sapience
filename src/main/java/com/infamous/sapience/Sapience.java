package com.infamous.sapience;

import com.infamous.sapience.capability.ageable.Ageable;
import com.infamous.sapience.capability.ageable.AgeableStorage;
import com.infamous.sapience.capability.ageable.IAgeable;
import com.infamous.sapience.capability.greed.Greed;
import com.infamous.sapience.capability.greed.GreedStorage;
import com.infamous.sapience.capability.greed.IGreed;
import com.infamous.sapience.capability.reputation.IReputation;
import com.infamous.sapience.capability.reputation.Reputation;
import com.infamous.sapience.capability.reputation.ReputationStorage;
import com.infamous.sapience.mod.FieldModification;
import com.infamous.sapience.mod.ModMemoryModuleTypes;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
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
        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        // Register the doClientStuff method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModMemoryModuleTypes.MEMORY_MODULE_TYPES.register(eventBus);
    }

    private void setup(final FMLCommonSetupEvent event)
    {
        CapabilityManager.INSTANCE.register(IAgeable.class, new AgeableStorage(), Ageable::new);
        CapabilityManager.INSTANCE.register(IGreed.class, new GreedStorage(), Greed::new);
        CapabilityManager.INSTANCE.register(IReputation.class, new ReputationStorage(), Reputation::new);
        FieldModification.init();
    }

    private void doClientStuff(final FMLClientSetupEvent event) {
    }
}
