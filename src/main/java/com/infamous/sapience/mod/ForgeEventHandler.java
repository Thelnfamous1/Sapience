package com.infamous.sapience.mod;

import com.infamous.sapience.Sapience;
import com.infamous.sapience.capability.ageable.AgeableProvider;
import com.infamous.sapience.capability.ageable.IAgeable;
import com.infamous.sapience.capability.greed.GreedProvider;
import com.infamous.sapience.capability.reputation.ReputationProvider;
import com.infamous.sapience.util.AgeableHelper;
import com.infamous.sapience.util.PiglinReputationType;
import com.infamous.sapience.util.PiglinTasksHelper;
import com.infamous.sapience.util.ReputationHelper;
import net.minecraft.Util;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ReputationEventHandler;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Sapience.MODID)
public class ForgeEventHandler {

    public static final ResourceLocation AGEABLE_LOCATION = new ResourceLocation(Sapience.MODID, "ageable");
    public static final ResourceLocation GREED_LOCATION = new ResourceLocation(Sapience.MODID, "greed");
    public static final ResourceLocation REPUTATION_LOCATION = new ResourceLocation(Sapience.MODID, "reputation");
    public static final String REPUTATION_DISPLAY_LOCALIZATION = "sapience.reputation_display";
    public static final String REP_INTERACT_COOLDOWN_TAG = "sapience:reputation_interaction_cooldown";

    @SubscribeEvent
    public static void onAttachEntityCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Piglin) {
            event.addCapability(AGEABLE_LOCATION, new AgeableProvider());
            event.addCapability(GREED_LOCATION, new GreedProvider());
            event.addCapability(REPUTATION_LOCATION, new ReputationProvider());
        }
    }

    // SERVER ONLY - Note that AgeableHelper#createChild already initializes the child,
    // so the call to AgeableHelper#initializeChild here won't do anything for a piglin that was spawned from breeding
    // This is strictly for initializing the Ageable capability for naturally spawning baby piglins
    @SubscribeEvent
    public static void onPiglinSpawn(EntityJoinWorldEvent event){
        if(event.getEntity() instanceof Piglin piglinEntity && !event.getWorld().isClientSide){
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
        LivingEntity victim = event.getEntityLiving();
        if(murderer == null || murderer.level.isClientSide) return;

        if(victim instanceof PiglinBrute){
            ReputationHelper.makeWitnessesOfMurder(victim, murderer, PiglinReputationType.BRUTE_KILLED);
        } else if(victim instanceof Piglin){
            ReputationHelper.makeWitnessesOfMurder(victim, murderer,
                    victim.isBaby() ? PiglinReputationType.BABY_PIGLIN_KILLED : PiglinReputationType.ADULT_PIGLIN_KILLED);
        }
        else if(victim.getType().is(PiglinTasksHelper.PIGLINS_HATE)){
            ReputationHelper.makeWitnessesOfMurder(victim, murderer,
                    isBossMob(victim) ? PiglinReputationType.WITHER_KILLED : PiglinReputationType.WITHER_SKELETON_KILLED);
        }
    }

    private static boolean isBossMob(LivingEntity victim) {
        // TODO: Generalize
        return victim instanceof WitherBoss || victim instanceof EnderDragon;
    }

    // SERVER ONLY
    @SubscribeEvent
    public static void onPiglinAttacked(LivingAttackEvent event){
        Entity attacker = event.getSource().getEntity();
        if (event.getEntityLiving() instanceof Piglin piglinEntity
                && event.getEntityLiving() instanceof ReputationEventHandler
                && attacker != null && attacker.level instanceof ServerLevel) {
            ((ServerLevel)attacker.level).onReputationEvent(
                    piglinEntity.isBaby() ? PiglinReputationType.BABY_PIGLIN_HURT : PiglinReputationType.ADULT_PIGLIN_HURT,
                    attacker,
                    (ReputationEventHandler) piglinEntity);
        }
    }

    @SubscribeEvent
    public static void onPiglinUpdate(LivingEvent.LivingUpdateEvent event){
        if(event.getEntityLiving() instanceof Player player && player.level.isClientSide){
            updateRepInteractCooldown(player);
        }

        if(event.getEntityLiving() instanceof Piglin piglinEntity){

            ReputationHelper.updateGossip(piglinEntity);

            if(PiglinTasksHelper.hasConsumableOffhandItem(piglinEntity) // checks for food or drink
                    && !piglinEntity.isUsingItem()
                    // this additional check for a non-piglin food item allows for drinks to be consumed
                    && (!PiglinTasksHelper.isPiglinFoodItem(piglinEntity.getOffhandItem()) || !PiglinTasksHelper.hasAteRecently(piglinEntity))){
                piglinEntity.startUsingItem(InteractionHand.OFF_HAND);
            }

            if(piglinEntity instanceof IShakesHead shakesHead){
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

    private static void updateRepInteractCooldown(Player player) {
        int repInteractCooldown = player.getPersistentData().getInt(REP_INTERACT_COOLDOWN_TAG);
        if(repInteractCooldown > 0){
            player.getPersistentData().putInt(REP_INTERACT_COOLDOWN_TAG, repInteractCooldown - 1);
        }
    }

    @SubscribeEvent
    public static void onReputationInteract(PlayerInteractEvent.EntityInteract event){
        Player player = event.getPlayer();
        InteractionHand hand = event.getHand();
        ItemStack stack = player.getItemInHand(hand);
        Entity target = event.getTarget();
        if(!event.getWorld().isClientSide
                && target instanceof ReputationEventHandler
                && stack.isEmpty()
                && player.isSecondaryUseActive()){
            int reputation = target instanceof Villager villager ?
                    villager.getPlayerReputation(player) :
                    ReputationHelper.getEntityReputation(player, target);
            sendReputation(player, target, reputation);
        }
    }

    private static void sendReputation(Player player, Entity target, int reputation) {
        if(player.getPersistentData().getInt(REP_INTERACT_COOLDOWN_TAG) <= 0){
            player.sendMessage(new TranslatableComponent(REPUTATION_DISPLAY_LOCALIZATION, target, reputation), Util.NIL_UUID);
            player.getPersistentData().putInt(REP_INTERACT_COOLDOWN_TAG, 20);
        }
    }
}
