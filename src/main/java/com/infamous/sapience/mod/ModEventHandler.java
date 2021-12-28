package com.infamous.sapience.mod;

import com.infamous.sapience.Sapience;
import com.infamous.sapience.capability.ageable.Ageable;
import com.infamous.sapience.capability.emotive.Emotive;
import com.infamous.sapience.capability.greed.Greed;
import com.infamous.sapience.capability.reputation.Reputation;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Sapience.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEventHandler {

    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.register(Ageable.class);
        event.register(Greed.class);
        event.register(Reputation.class);
        event.register(Emotive.class);
    }
}
