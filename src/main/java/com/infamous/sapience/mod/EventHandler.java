package com.infamous.sapience.mod;

import com.infamous.sapience.Sapience;
import com.infamous.sapience.capability.ageable.AgeableProvider;
import com.infamous.sapience.capability.ageable.IAgeable;
import com.infamous.sapience.capability.greed.GreedProvider;
import com.infamous.sapience.capability.reputation.ReputationProvider;
import com.infamous.sapience.util.PiglinReputationType;
import com.infamous.sapience.util.AgeableHelper;
import com.infamous.sapience.util.PiglinTasksHelper;
import com.infamous.sapience.util.ReputationHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.ReputationEventHandler;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.InteractionHand;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Sapience.MODID)
public class EventHandler {
    @SubscribeEvent
    public static void onAttachEntityCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Piglin) {
            event.addCapability(new ResourceLocation(Sapience.MODID, "ageable"), new AgeableProvider());
            event.addCapability(new ResourceLocation(Sapience.MODID, "greed"), new GreedProvider());
            event.addCapability(new ResourceLocation(Sapience.MODID, "reputation"), new ReputationProvider());
        }
    }

    // SERVER ONLY - Note that AgeableHelper#createChild already initializes the child,
    // so the call to AgeableHelper#initializeChild here won't do anything for a piglin that was spawned from breeding
    // This is strictly for initializing the Ageable capability for naturally spawning baby piglins
    @SubscribeEvent
    public static void onPiglinSpawn(EntityJoinWorldEvent event){
        if(event.getEntity() instanceof Piglin && !event.getWorld().isClientSide){
            Piglin piglinEntity = (Piglin)event.getEntity();
                // Manually setting the piglin's growing age to -24000
                // Normally, setChild would automatically set the growing age based on the boolean given
                // But since Piglins don't extend from AgeableEntity, we have to do it manually here
                // We choose EntityJoinWorldEvent since this is guaranteed to work whenever the Piglin is initially spawned
                // But we also need to set the capability's wasBorn field to true,
                // so that the growing age is not reset when the world is loaded back up from the disk again
            if(piglinEntity.isBaby()){
                AgeableHelper.initializeChild(piglinEntity);
            }
        }
    }

    // SERVER ONLY
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event){
        Entity murderer = event.getSource().getEntity();
        if(event.getEntityLiving() instanceof Piglin && murderer != null && murderer.level instanceof ServerLevel){
            Piglin piglinEntity = (Piglin) event.getEntityLiving();
            ReputationHelper.makeWitnessesOfMurder(piglinEntity, murderer,
                    piglinEntity.isBaby() ? PiglinReputationType.BABY_PIGLIN_KILLED : PiglinReputationType.ADULT_PIGLIN_KILLED);
        }
        else if(event.getEntityLiving() instanceof PiglinBrute && murderer != null && murderer.level instanceof ServerLevel){
            PiglinBrute bruteEntity = (PiglinBrute) event.getEntityLiving();
            ReputationHelper.makeWitnessesOfMurder(bruteEntity, murderer, PiglinReputationType.BRUTE_KILLED);
        }
        else if(event.getEntityLiving() instanceof WitherSkeleton && murderer != null && murderer.level instanceof ServerLevel){
            WitherSkeleton witherSkeletonEntity = (WitherSkeleton)event.getEntityLiving();
            ReputationHelper.makeWitnessesOfMurder(witherSkeletonEntity, murderer, PiglinReputationType.WITHER_SKELETON_KILLED);
        }
        else if(event.getEntityLiving() instanceof WitherBoss && murderer != null && murderer.level instanceof ServerLevel){
            WitherBoss witherEntity = (WitherBoss)event.getEntityLiving();
            ReputationHelper.makeWitnessesOfMurder(witherEntity, murderer, PiglinReputationType.WITHER_KILLED);
        }
    }

    // SERVER ONLY
    @SubscribeEvent
    public static void onPiglinAttacked(LivingAttackEvent event){
        Entity attacker = event.getSource().getEntity();
        if (event.getEntityLiving() instanceof Piglin
                && event.getEntityLiving() instanceof ReputationEventHandler
                && attacker != null && attacker.level instanceof ServerLevel) {
            Piglin piglinEntity = (Piglin) event.getEntityLiving();
            ((ServerLevel)attacker.level).onReputationEvent(
                    piglinEntity.isBaby() ? PiglinReputationType.BABY_PIGLIN_HURT : PiglinReputationType.ADULT_PIGLIN_HURT,
                    attacker,
                    (ReputationEventHandler) piglinEntity);
        }
    }

    @SubscribeEvent
    public static void onPiglinUpdate(LivingEvent.LivingUpdateEvent event){
        if(event.getEntityLiving() instanceof Piglin){
            Piglin piglinEntity = (Piglin)event.getEntityLiving();

            ReputationHelper.updateGossip(piglinEntity);

            if(PiglinTasksHelper.hasConsumableOffhandItem(piglinEntity) // checks for food or drink
                    && !piglinEntity.isUsingItem()
                    // this additional check for a non-piglin food item allows for drinks to be consumed
                    && (!PiglinTasksHelper.isPiglinFoodItem(piglinEntity.getOffhandItem().getItem()) || !PiglinTasksHelper.hasAteRecently(piglinEntity))){
                piglinEntity.startUsingItem(InteractionHand.OFF_HAND);
            }

            if(piglinEntity instanceof IShakesHead){
                IShakesHead shakesHead = (IShakesHead)piglinEntity;
                if (shakesHead.getShakeHeadTicks() > 0) {
                    shakesHead.setShakeHeadTicks(shakesHead.getShakeHeadTicks() - 1);
                }
            }

            if(!piglinEntity.level.isClientSide){
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
