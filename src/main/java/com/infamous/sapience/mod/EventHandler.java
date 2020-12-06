package com.infamous.sapience.mod;

import com.infamous.sapience.Sapience;
import com.infamous.sapience.capability.ageable.AgeableProvider;
import com.infamous.sapience.capability.ageable.IAgeable;
import com.infamous.sapience.capability.greed.GreedProvider;
import com.infamous.sapience.util.AgeableHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.piglin.PiglinEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Sapience.MODID)
public class EventHandler {
    @SubscribeEvent
    public static void onAttachEntityCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof PiglinEntity) {
            event.addCapability(new ResourceLocation(Sapience.MODID, "ageable"), new AgeableProvider());
            event.addCapability(new ResourceLocation(Sapience.MODID, "greed"), new GreedProvider());
        }
    }

    // SERVER ONLY - Note that CapabilityHelper#createChild already initializes the child,
    // so the call to CapabilityHelper#initializeChild here won't do anything for a piglin that was spawned from breeding
    // This is strictly for initializing the Ageable capability for naturally spawning baby piglins
    @SubscribeEvent
    public static void onPiglinSpawn(EntityJoinWorldEvent event){
        if(event.getEntity() instanceof PiglinEntity && !event.getWorld().isRemote){
            PiglinEntity piglinEntity = (PiglinEntity)event.getEntity();
                // Manually setting the piglin's growing age to -24000
                // Normally, setChild would automatically set the growing age based on the boolean given
                // But since Piglins don't extend from AgeableEntity, we have to do it manually here
                // We choose EntityJoinWorldEvent since this is guaranteed to work whenever the Piglin is initially spawned
                // But we also need to set the capability's wasBorn field to true,
                // so that the growing age is not reset when the world is loaded back up from the disk again
            if(piglinEntity.isChild()){
                AgeableHelper.initializeChild(piglinEntity);
            }
        }
    }

    // SERVER ONLY
    @SubscribeEvent
    public static void onPiglinUpdate(LivingEvent.LivingUpdateEvent event){
        if(event.getEntityLiving() instanceof PiglinEntity){
            PiglinEntity piglinEntity = (PiglinEntity)event.getEntityLiving();
            if(piglinEntity instanceof IShakesHead){
                IShakesHead shakesHead = (IShakesHead)piglinEntity;
                if (shakesHead.getShakeHeadTicks() > 0) {
                    shakesHead.setShakeHeadTicks(shakesHead.getShakeHeadTicks() - 1);
                }
            }
            if(!event.getEntityLiving().world.isRemote){
                IAgeable ageable = AgeableHelper.getAgeableCapability(piglinEntity);
                if(ageable != null){
                    if (piglinEntity.isAlive()) {
                        AgeableHelper.updateSelfAge(piglinEntity);
                        AgeableHelper.updateForcedAge(piglinEntity);
                        AgeableHelper.updateGrowingAge(piglinEntity);
                    }
                }
            }
        }
    }
}
