package com.infamous.sapience.mod;

import com.infamous.sapience.Sapience;
import com.infamous.sapience.capability.ageable.AgeableProvider;
import com.infamous.sapience.capability.ageable.IAgeable;
import com.infamous.sapience.capability.greed.GreedProvider;
import com.infamous.sapience.capability.reputation.ReputationProvider;
import com.infamous.sapience.util.*;
import net.minecraft.Util;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ReputationEventHandler;
import net.minecraft.world.entity.ai.behavior.GoToWantedItem;
import net.minecraft.world.entity.ai.behavior.RunOne;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;

@Mod.EventBusSubscriber(modid = Sapience.MODID)
public class ForgeEventHandler {

    public static final ResourceLocation AGEABLE_LOCATION = new ResourceLocation(Sapience.MODID, "ageable");
    public static final ResourceLocation GREED_LOCATION = new ResourceLocation(Sapience.MODID, "greed");
    public static final ResourceLocation REPUTATION_LOCATION = new ResourceLocation(Sapience.MODID, "reputation");
    public static final String REPUTATION_DISPLAY_LOCALIZATION = "sapience.reputation_display";

    private static final ThreadLocal<Map<Piglin, Boolean>> THREADED_SKIP_MOUNT_CHECKS = ThreadLocal.withInitial(HashMap::new);
    private static final ThreadLocal<Map<Player, Boolean>> THREADED_SKIP_INTERACT_CHECKS = ThreadLocal.withInitial(HashMap::new);

    @SubscribeEvent
    public static void onAttachEntityCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Piglin) {
            event.addCapability(AGEABLE_LOCATION, new AgeableProvider());
            event.addCapability(GREED_LOCATION, new GreedProvider());
            event.addCapability(REPUTATION_LOCATION, new ReputationProvider());
        }
    }
    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinWorldEvent event){
        Entity entity = event.getEntity();
        if(entity instanceof Piglin piglin){
            if(piglin.isBaby() && !entity.level.isClientSide){
                AgeableHelper.initializeChild(piglin);
            }

            // Better, Forge-supported way of adding tasks to entity brains
            BrainHelper
                    .retrieveFirstAvailableTask(piglin.getBrain(), Activity.IDLE, 16, b -> b instanceof RunOne<?>)
                    .map(RunOne.class::cast)
                    .ifPresent(PiglinTasksHelper::addAdditionalIdleMovementBehaviors);
            BrainHelper
                    .retrieveFirstAvailableTask(piglin.getBrain(), Activity.AVOID, 12, b -> b instanceof RunOne<?>)
                    .map(RunOne.class::cast)
                    .ifPresent(PiglinTasksHelper::addAdditionalIdleMovementBehaviors);
        }
        // Better, Forge-supported way of adding tasks to entity brains
        if(entity instanceof Hoglin hoglin){
            hoglin.setCanPickUpLoot(true);
            BrainHelper.addAdditionalTasks(hoglin.getBrain(), Activity.CORE, 2,
                    new GoToWantedItem<>(1.0F, true, 9));
        }
    }

    // SERVER ONLY
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event){
        Entity murderer = event.getSource().getEntity();
        LivingEntity victim = event.getEntityLiving();
        if(murderer == null || murderer.level.isClientSide) return;

        if(victim instanceof AbstractPiglin){
            ReputationHelper.makeWitnessesOfMurder(victim, murderer,
                    victim.isBaby() ?
                            PiglinReputationType.BABY_PIGLIN_KILLED :
                            victim instanceof PiglinBrute ?
                                    PiglinReputationType.BRUTE_KILLED :
                                    PiglinReputationType.ADULT_PIGLIN_KILLED);
        }
        else if(victim.getType().is(PiglinTasksHelper.PIGLINS_HATE)){
            ReputationHelper.makeWitnessesOfMurder(victim, murderer,
                    isBoss(victim) ?
                            PiglinReputationType.WITHER_KILLED :
                            PiglinReputationType.WITHER_SKELETON_KILLED);
        }
    }

    private static boolean isBoss(LivingEntity victim) {
        return victim.getType().is(GeneralHelper.BOSSES);
    }

    // SERVER ONLY
    @SubscribeEvent
    public static void onPiglinAttacked(LivingAttackEvent event){
        Entity attacker = event.getSource().getEntity();
        if (event.getEntityLiving() instanceof Piglin piglin
                && event.getEntityLiving() instanceof ReputationEventHandler
                && attacker != null && attacker.level instanceof ServerLevel) {
            ((ServerLevel)attacker.level).onReputationEvent(
                    piglin.isBaby() ? PiglinReputationType.BABY_PIGLIN_HURT : PiglinReputationType.ADULT_PIGLIN_HURT,
                    attacker,
                    (ReputationEventHandler) piglin);
        }
    }

    @SubscribeEvent
    public static void onPiglinUpdate(LivingEvent.LivingUpdateEvent event){
        if(event.getEntityLiving() instanceof Piglin piglin){

            ReputationHelper.updateGossip(piglin);

            if(PiglinTasksHelper.hasConsumableOffhandItem(piglin) // checks for food or drink
                    && !piglin.isUsingItem()
                    // this additional check for a non-piglin food item allows for drinks to be consumed
                    && (!PiglinTasksHelper.isPiglinFoodItem(piglin.getOffhandItem()) || !PiglinTasksHelper.hasAteRecently(piglin))){
                piglin.startUsingItem(InteractionHand.OFF_HAND);
            }

            if(piglin instanceof IShakesHead shakesHead){
                if (shakesHead.getShakeHeadTicks() > 0) {
                    shakesHead.setShakeHeadTicks(shakesHead.getShakeHeadTicks() - 1);
                }
            }

            if(!piglin.level.isClientSide){
                IAgeable ageable = AgeableHelper.getAgeableCapability(piglin);
                if(ageable != null){
                    if (piglin.isAlive()) {
                        AgeableHelper.updateSelfAge(piglin);
                        AgeableHelper.updateForcedAge(piglin);
                        AgeableHelper.updateGrowingAge(piglin);
                    }
                }
            }
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
                && hand == InteractionHand.MAIN_HAND // prevents two messages being sent
                && stack.isEmpty()
                && player.isSecondaryUseActive()){
            int reputation = target instanceof Villager villager ?
                    villager.getPlayerReputation(player) :
                    ReputationHelper.getEntityReputation(target, player);
            sendReputation(player, target, reputation);
        }
    }

    private static void sendReputation(Player player, Entity target, int reputation) {
            player.sendMessage(new TranslatableComponent(REPUTATION_DISPLAY_LOCALIZATION, target, reputation), Util.NIL_UUID);
    }

    @SubscribeEvent
    public static void onLivingConversion(LivingConversionEvent.Post event){
        if(event.getEntityLiving() instanceof Piglin piglin){
            GreedHelper.dropGreedItems(piglin);
        }
    }

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event){
        if(event.getEntityLiving() instanceof Piglin piglin){
            Collection<ItemStack> greedItemsForDrop = GreedHelper.getGreedItemsForDrop(piglin);
            Collection<ItemEntity> drops = event.getDrops();
            greedItemsForDrop.forEach(stack -> {
                ItemEntity itemEntity = new ItemEntity(piglin.level, piglin.getX(), piglin.getY(), piglin.getZ(), stack);
                itemEntity.setDefaultPickUpDelay();
                drops.add(itemEntity);
            });
        }
    }

    @SubscribeEvent
    public static void onMount(EntityMountEvent event){
        if(event.getEntity() instanceof Piglin piglin && piglin.isBaby() && event.isMounting()){

            Map<Piglin, Boolean> skipMountChecks = THREADED_SKIP_MOUNT_CHECKS.get();
            if (checkSkipMap(piglin, skipMountChecks)) return;

            Entity mount = event.getEntityBeingMounted();
            if(mount instanceof Hoglin && mount.getType() != EntityType.HOGLIN){
                event.setCanceled(true);
                skipMountChecks.put(piglin, true);
                piglin.startRiding(getTopPassenger(mount, 3));
            }
        }
    }

    private static boolean checkSkipMap(Entity entity, Map<? extends Entity, Boolean> skipMap) {
        if (skipMap.getOrDefault(entity, false)) {
            skipMap.remove(entity);
            return true;
        }
        return false;
    }

    private static Entity getTopPassenger(Entity mount, int index) {
        List<Entity> passengers = mount.getPassengers();
        return index != 1 && !passengers.isEmpty() ?
                getTopPassenger(passengers.get(0), index - 1) :
                mount;
    }

    @SubscribeEvent
    public static void onFinishUsingItem(LivingEntityUseItemEvent.Finish event){
        if(event.getEntityLiving() instanceof Piglin piglin){
            ItemStack itemStack = event.getItem();
            FoodProperties foodProperties = itemStack.getItem().getFoodProperties();
            if (foodProperties != null) {
                int nutrition = foodProperties.getNutrition();
                piglin.heal(nutrition); // heals the piglin by an amount equal to the food's hunger value
                AgeableHelper.increaseFoodLevel(piglin, nutrition);
                if(!piglin.level.isClientSide){
                    PiglinTasksHelper.setAteRecently(piglin);
                    ReputationHelper.updatePreviousInteractorReputation(piglin, PiglinReputationType.FOOD_GIFT);
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event){
        Player player = event.getPlayer();
        InteractionHand hand = event.getHand();
        ItemStack stack = player.getItemInHand(hand);
        Entity target = event.getTarget();
        if(target instanceof Piglin){
            Map<Player, Boolean> skipInteractChecks = THREADED_SKIP_INTERACT_CHECKS.get();
            if (checkSkipMap(player, skipInteractChecks)) return;

            event.setCanceled(true);
            skipInteractChecks.put(player, true);

            InteractionResult interactionResult = player.interactOn(target, hand);
            if(player instanceof ServerPlayer serverPlayer){
                if (interactionResult.consumesAction()) {
                    CriteriaTriggers.PLAYER_INTERACTED_WITH_ENTITY.trigger(serverPlayer, stack, target);
                    if (interactionResult.shouldSwing()) {
                        player.swing(hand, true);
                    }
                }
            }
        }
    }
}
