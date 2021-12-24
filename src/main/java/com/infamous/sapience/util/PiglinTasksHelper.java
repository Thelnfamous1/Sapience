package com.infamous.sapience.util;

import com.google.common.collect.Lists;
import com.infamous.sapience.Sapience;
import com.infamous.sapience.mod.ModMemoryModuleTypes;
import com.infamous.sapience.tasks.CraftWithGoldTask;
import com.infamous.sapience.tasks.CreateBabyTask;
import com.infamous.sapience.tasks.FeedHoglinsTask;
import com.infamous.sapience.tasks.ShareGoldTask;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinArmPose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.Tags;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class PiglinTasksHelper {
    public static final Tags.IOptionalNamedTag<EntityType<?>> PIGLINS_HUNT = EntityTypeTags.createOptional(new ResourceLocation(Sapience.MODID, "piglins_hunt"));
    public static final Tags.IOptionalNamedTag<EntityType<?>> PIGLINS_AVOID = EntityTypeTags.createOptional(new ResourceLocation(Sapience.MODID, "piglins_avoid"));
    public static final Tags.IOptionalNamedTag<EntityType<?>> PIGLINS_HATE = EntityTypeTags.createOptional(new ResourceLocation(Sapience.MODID, "piglins_hate"));

    public static final Tags.IOptionalNamedTag<Item> PIGLINS_BARTER = ItemTags.createOptional(new ResourceLocation(Sapience.MODID, "piglins_barter"));
    public static final Tags.IOptionalNamedTag<Item> PIGLINS_BARTER_CHEAP = ItemTags.createOptional(new ResourceLocation(Sapience.MODID, "piglins_barter_cheap"));
    public static final Tags.IOptionalNamedTag<Item> PIGLINS_BARTER_EXPENSIVE = ItemTags.createOptional(new ResourceLocation(Sapience.MODID, "piglins_barter_expensive"));

    public static final ResourceLocation PIGLIN_BARTERING_CHEAP = new ResourceLocation(Sapience.MODID, "gameplay/piglin_bartering_cheap");
    public static final ResourceLocation PIGLIN_BARTERING_EXPENSIVE = new ResourceLocation(Sapience.MODID, "gameplay/piglin_bartering_expensive");

    private static final UniformInt RANGED_FEEDING_TIMER = TimeUtil.rangeOfSeconds(30, 120);

    public static boolean canPickUpFoodStack(Piglin piglinEntity, ItemStack itemStack) {
        if (itemStack.is(ItemTags.PIGLIN_REPELLENTS)) {
            return false;
        } else if (PiglinTasksHelper.hasAdmiringDisabled(piglinEntity) && piglinEntity.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET)) {
            return false;
        } else if (itemStack.isPiglinCurrency()) {
            return PiglinTasksHelper.hasOpenOffhandSlot(piglinEntity);
        } else {
            if (PiglinTasksHelper.isPiglinFoodItem(itemStack)) {
                return !PiglinTasksHelper.hasAteRecently(piglinEntity) && hasOpenOffhandSlot(piglinEntity);
            }
            else{
                return false;
            }
        }
    }

    private static void dropItems(AbstractPiglin piglinEntity, List<ItemStack> itemStacks, Vec3 vector3d) {
        if (!itemStacks.isEmpty()) {
            piglinEntity.swing(InteractionHand.OFF_HAND);

            for(ItemStack itemstack : itemStacks) {
                BehaviorUtils.throwItem(piglinEntity, itemstack, vector3d.add(0.0D, 1.0D, 0.0D));
            }
        }
    }

    private static Vec3 getNearbyVectorOrPositionVector(AbstractPiglin piglinEntity) {
        Vec3 vector3d = LandRandomPos.getPos(piglinEntity, 4, 2);
        return vector3d == null ? piglinEntity.position() : vector3d;
    }

    private static void dropItemsNearSelf(AbstractPiglin piglinEntity, List<ItemStack> itemStacks) {
        dropItems(piglinEntity, itemStacks, getNearbyVectorOrPositionVector(piglinEntity));
    }

    public static void setAteRecently(AbstractPiglin piglinEntity) {
        piglinEntity.getBrain().setMemoryWithExpiry(MemoryModuleType.ATE_RECENTLY, true, 200L);
    }

    public static ItemStack extractSingletonFromItemEntity(ItemEntity itemEntity) {
        ItemStack itemstack = itemEntity.getItem();
        ItemStack itemstack1 = itemstack.split(1);
        if (itemstack.isEmpty()) {
            itemEntity.remove(Entity.RemovalReason.DISCARDED);
        } else {
            itemEntity.setItem(itemstack);
        }

        return itemstack1;
    }



    public static boolean isPiglinFoodItem(ItemStack item) {
        return item.is(ItemTags.PIGLIN_FOOD) && item.isEdible();
    }

    public static boolean hasAteRecently(AbstractPiglin piglinEntity) {
        return piglinEntity.getBrain().hasMemoryValue(MemoryModuleType.ATE_RECENTLY);
    }

    public static boolean hasOpenOffhandSlot(Piglin piglinEntity) {
        return piglinEntity.getOffhandItem().isEmpty() || !isPiglinLoved(piglinEntity.getOffhandItem());
    }

    public static boolean isPiglinLoved(ItemStack item) {
        return item.is(ItemTags.PIGLIN_LOVED);
    }

    private static boolean hasAdmiringDisabled(AbstractPiglin piglinEntity) {
        return piglinEntity.getBrain().hasMemoryValue(MemoryModuleType.ADMIRING_DISABLED);
    }


    public static boolean hasNotFedRecently(Piglin piglinEntity){
        return !hasFedRecently(piglinEntity);
    }

    public static void setFedRecently(Piglin piglinEntity) {
        piglinEntity.getBrain().setMemoryWithExpiry(ModMemoryModuleTypes.FED_RECENTLY.get(), true, (long) RANGED_FEEDING_TIMER.sample(piglinEntity.level.random));
    }

    private static boolean hasFedRecently(Piglin piglinEntity){
        return piglinEntity.getBrain().hasMemoryValue(ModMemoryModuleTypes.FED_RECENTLY.get());
    }

    public static void removeTimeTryingToReachAdmireItem(Piglin piglinEntity){
        piglinEntity.getBrain().eraseMemory(MemoryModuleType.TIME_TRYING_TO_REACH_ADMIRE_ITEM);
    }

    public static void dropOffhandItemAndSetItemStackToOffhand(AbstractPiglin piglinEntity, ItemStack itemStack) {
        if (hasOffhandItem(piglinEntity)) {
            piglinEntity.spawnAtLocation(piglinEntity.getItemInHand(InteractionHand.OFF_HAND));
        }
        setItemStackToOffhandAndPersist(piglinEntity, itemStack);
    }

    private static boolean hasOffhandItem(AbstractPiglin piglinEntity) {
        return !piglinEntity.getOffhandItem().isEmpty();
    }

    private static void setItemStackToOffhandAndPersist(AbstractPiglin piglinEntity, ItemStack itemStack) {
        piglinEntity.setItemSlot(EquipmentSlot.OFFHAND, itemStack);
        piglinEntity.setGuaranteedDrop(EquipmentSlot.OFFHAND);
        piglinEntity.setPersistenceRequired();
    }

    public static void clearWalkPath(AbstractPiglin piglinEntity) {
        piglinEntity.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        piglinEntity.getNavigation().stop();
    }

    public static boolean hasConsumableOffhandItem(AbstractPiglin piglinEntity) {
        ItemStack offhandStack = piglinEntity.getOffhandItem();
        return isPiglinFoodItem(offhandStack) // this accounts for piglin foods
                || offhandStack.getUseAnimation() == UseAnim.DRINK; // this accounts for honey bottles, milk buckets, and potions
    }

    public static void setAdmiringItem(LivingEntity livingEntity) {
        livingEntity.getBrain().setMemoryWithExpiry(MemoryModuleType.ADMIRING_ITEM, true, 120L);
    }

    private static boolean hasAdmiringItem(AbstractPiglin piglinEntity) {
        return piglinEntity.getBrain().hasMemoryValue(MemoryModuleType.ADMIRING_ITEM);
    }

    public static boolean isNormalBarterItem(ItemStack item){
        return item.is(PIGLINS_BARTER);
    }

    public static boolean isBarterItem(ItemStack item) {
        return isNormalBarterItem(item)|| isAlternativeGreedItem(item);
    }

    private static boolean isAlternativeGreedItem(ItemStack item) {
        return isExpensiveBarterItem(item) || isCheapBarterItem(item);
    }

    private static InteractionResult processInteractionForFoodItem(AbstractPiglin piglinEntity, Player playerEntity, InteractionHand hand) {
        ItemStack itemstack = playerEntity.getItemInHand(hand);
        if (canAcceptFoodItem(piglinEntity, itemstack)) {
            ItemStack foodStack = itemstack.split(1);
            PiglinTasksHelper.dropOffhandItemAndSetItemStackToOffhand(piglinEntity, foodStack);
            PiglinTasksHelper.setAdmiringItem(piglinEntity);
            PiglinTasksHelper.clearWalkPath(piglinEntity);

            // REPUTATION
            ReputationHelper.setPreviousInteractor(piglinEntity, playerEntity);
            return InteractionResult.CONSUME;
        } else {
            return InteractionResult.PASS;
        }
    }


    private static InteractionResult processInteractionForPiglinGreedItem(AbstractPiglin piglinEntity, Player playerEntity, InteractionHand handIn) {
        ItemStack itemstack = playerEntity.getItemInHand(handIn);
        if (canAcceptPiglinGreedItem(piglinEntity, itemstack)) {
            ItemStack greedStack = itemstack.split(1);
            PiglinTasksHelper.dropOffhandItemAndSetItemStackToOffhand(piglinEntity, greedStack);
            PiglinTasksHelper.setAdmiringItem(piglinEntity);
            PiglinTasksHelper.clearWalkPath(piglinEntity);

            // REPUTATION
            ReputationHelper.setPreviousInteractor(piglinEntity, playerEntity);
            return InteractionResult.CONSUME;
        } else {
            return InteractionResult.PASS;
        }
    }


    private static InteractionResult processInteractionForPiglinLovedItem(AbstractPiglin piglinEntity, Player playerEntity, InteractionHand handIn) {
        ItemStack itemstack = playerEntity.getItemInHand(handIn);
        if (canAcceptPiglinLovedItem(piglinEntity, itemstack)) {
            ItemStack greedStack = itemstack.split(1);
            PiglinTasksHelper.dropOffhandItemAndSetItemStackToOffhand(piglinEntity, greedStack);
            PiglinTasksHelper.setAdmiringItem(piglinEntity);
            PiglinTasksHelper.clearWalkPath(piglinEntity);

            // REPUTATION
            ReputationHelper.setPreviousInteractor(piglinEntity, playerEntity);
            return InteractionResult.CONSUME;
        } else {
            return InteractionResult.PASS;
        }
    }

    private static boolean canAcceptPiglinGreedItem(AbstractPiglin piglinEntity, ItemStack itemStack) {
        return !hasAdmiringDisabled(piglinEntity)
                && !hasAdmiringItem(piglinEntity)

                && (isAlternativeGreedItem(itemStack) // a specific check for baby piglins to be able to accept piglin currency
                || isNormalBarterItem(itemStack) && piglinEntity.isBaby())

                && !hasConsumableOffhandItem(piglinEntity);
    }

    private static boolean canAcceptPiglinLovedItem(AbstractPiglin piglinEntity, ItemStack itemStack) {
        return !hasAdmiringDisabled(piglinEntity)
                && !hasAdmiringItem(piglinEntity)
                && isPiglinLoved(itemStack)
                && !hasConsumableOffhandItem(piglinEntity);
    }

    private static boolean canAcceptFoodItem(AbstractPiglin piglinEntity, ItemStack itemStack) {
        return !hasAdmiringDisabled(piglinEntity)
                && !hasAdmiringItem(piglinEntity)
                && isPiglinFoodItem(itemStack)
                && !hasAteRecently(piglinEntity)
                && !piglinEntity.isAggressive()
                && !hasConsumableOffhandItem(piglinEntity);
    }

    public static InteractionResult getAgeableActionResultType(AbstractPiglin piglinEntity, Player playerEntity, InteractionHand handIn, InteractionResult actionResultTypeIn) {
        ItemStack itemStack = playerEntity.getItemInHand(handIn);
        if(isPiglinFoodItem(itemStack)){
            if(!piglinEntity.level.isClientSide){
                actionResultTypeIn = processInteractionForFoodItem(piglinEntity, playerEntity, handIn);
            }
            else{
                boolean canAcceptFoodItem = canAcceptFoodItem(piglinEntity, itemStack)  && piglinEntity.getArmPose() != PiglinArmPose.ADMIRING_ITEM;
                actionResultTypeIn = canAcceptFoodItem ? InteractionResult.SUCCESS : InteractionResult.PASS;
            }
        }
        return actionResultTypeIn;
    }

    public static InteractionResult getGreedActionResultType(AbstractPiglin piglinEntity, Player playerEntity, InteractionHand handIn, InteractionResult actionResultTypeIn) {
        ItemStack itemStack = playerEntity.getItemInHand(handIn);
        if(isBarterItem(itemStack)){
            if(!piglinEntity.level.isClientSide){
                actionResultTypeIn = processInteractionForPiglinGreedItem(piglinEntity, playerEntity, handIn);
            }
            else{
                boolean canTakeGreedItem = canAcceptPiglinGreedItem(piglinEntity, itemStack)  && piglinEntity.getArmPose() != PiglinArmPose.ADMIRING_ITEM;
                actionResultTypeIn = canTakeGreedItem ? InteractionResult.SUCCESS : InteractionResult.PASS;
            }
        }
        else if(isPiglinLoved(itemStack)){
            if(!piglinEntity.level.isClientSide){
                actionResultTypeIn = processInteractionForPiglinLovedItem(piglinEntity, playerEntity, handIn);
            }
            else{
                boolean canTakeLovedItem = canAcceptPiglinLovedItem(piglinEntity, itemStack)  && piglinEntity.getArmPose() != PiglinArmPose.ADMIRING_ITEM;
                actionResultTypeIn = canTakeLovedItem ? InteractionResult.SUCCESS : InteractionResult.PASS;
            }
        }
        return actionResultTypeIn;
    }

    private static List<ItemStack> getBlockBarteringLoot(AbstractPiglin piglinEntity){
        if(piglinEntity.level.getServer() != null){
            LootTable loottable = piglinEntity.level.getServer().getLootTables().get(PIGLIN_BARTERING_EXPENSIVE);
            return loottable.getRandomItems((new LootContext.Builder((ServerLevel)piglinEntity.level)).withParameter(LootContextParams.THIS_ENTITY, piglinEntity).withRandom(piglinEntity.level.random).create(LootContextParamSets.PIGLIN_BARTER));
        }
        return Collections.emptyList();
    }

    private static List<ItemStack> getNuggetBarteringLoot(AbstractPiglin piglinEntity){
        if(piglinEntity.level.getServer() != null){
            LootTable loottable = piglinEntity.level.getServer().getLootTables().get(PIGLIN_BARTERING_CHEAP);
            return loottable.getRandomItems((new LootContext.Builder((ServerLevel)piglinEntity.level)).withParameter(LootContextParams.THIS_ENTITY, piglinEntity).withRandom(piglinEntity.level.random).create(LootContextParamSets.PIGLIN_BARTER));

        }
        return Collections.emptyList();
    }

    public static boolean isExpensiveBarterItem(ItemStack item) {
        return item.is(PIGLINS_BARTER_EXPENSIVE);
    }

    public static boolean isCheapBarterItem(ItemStack item) {
        return item.is(PIGLINS_BARTER_CHEAP);
    }

    public static void dropBlockBarteringLoot(AbstractPiglin piglinEntity){
        dropItemsAccountingForNearbyPlayer(piglinEntity, getBlockBarteringLoot(piglinEntity));
    }
    public static void dropNuggetBarteringLoot(AbstractPiglin piglinEntity){
        dropItemsAccountingForNearbyPlayer(piglinEntity, getNuggetBarteringLoot(piglinEntity));
    }

    private static void dropItemsAccountingForNearbyPlayer(AbstractPiglin piglinEntity, List<ItemStack> itemStacks) {
        Optional<Player> optionalPlayerEntity = getNearestVisiblePlayer(piglinEntity);
        if (optionalPlayerEntity.isPresent()) {
            dropItemsNearPlayer(piglinEntity, optionalPlayerEntity.get(), itemStacks);
        } else {
            dropItemsNearSelf(piglinEntity, itemStacks);
        }

    }

    private static Optional<Player> getNearestVisiblePlayer(AbstractPiglin piglinEntity) {
        return piglinEntity.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_PLAYER);
    }


    private static void dropItemsNearPlayer(AbstractPiglin piglinEntity, Player playerEntity, List<ItemStack> itemStacks) {
        dropItems(piglinEntity, itemStacks, playerEntity.position());
    }

    public static boolean hasIdle(AbstractPiglin piglinEntity) {
        return piglinEntity.getBrain().isActive(Activity.IDLE);
    }

    public static boolean piglinsAvoid(EntityType<?> entityType) {
        return entityType.is(PIGLINS_AVOID);
    }

    public static boolean piglinsHate(EntityType<?> entityType) {
        return entityType.is(PIGLINS_HATE);
    }

    public static void additionalSensorLogic(LivingEntity entityIn) {
        Brain<?> brain = entityIn.getBrain();

        Optional<Mob> optionalNemesis = Optional.empty();
        Optional<Hoglin> optionalHuntableHoglin = Optional.empty();
        Optional<Player> optionalPlayerNotGilded = Optional.empty();
        Optional<LivingEntity> optionalZombified = Optional.empty();

        Optional<Hoglin> optionalNearestVisibleAdultHoglin = Optional.empty();

        List<AbstractPiglin> visibleAdultPiglins = Lists.newArrayList();

        NearestVisibleLivingEntities nearestvisiblelivingentities = brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).orElse(NearestVisibleLivingEntities.empty());

        for(LivingEntity nearby : nearestvisiblelivingentities.findAll((le) -> true)){
            if (nearby instanceof Hoglin hoglin) {
                if (hoglin.isAdult() && optionalNearestVisibleAdultHoglin.isEmpty()) {
                    optionalNearestVisibleAdultHoglin = Optional.of(hoglin);
                }
                if (optionalHuntableHoglin.isEmpty()
                        && hoglin.canBeHunted()
                        && GeneralHelper.isNotOnSameTeam(entityIn, hoglin)) {
                    optionalHuntableHoglin = Optional.of(hoglin);
                }
            } else if (nearby instanceof AbstractPiglin piglin) {
                if (piglin.isAdult()) {
                    visibleAdultPiglins.add(piglin);
                }
            } else if (nearby instanceof Player player) {
                if (optionalPlayerNotGilded.isEmpty()
                        && entityIn.canAttack(nearby)
                        && !ReputationHelper.hasAcceptableAttire(nearby, entityIn)
                        && GeneralHelper.isNotOnSameTeam(entityIn, nearby)) {
                    optionalPlayerNotGilded = Optional.of(player);
                }
            }  else if (optionalNemesis.isPresent() || !piglinsHate(nearby.getType())) {
                if (optionalZombified.isEmpty()
                        && piglinsAvoid(nearby.getType())
                        && GeneralHelper.isNotOnSameTeam(entityIn, nearby)) {
                    optionalZombified = Optional.of(nearby);
                }
            } else if(GeneralHelper.isNotOnSameTeam(entityIn, nearby)){
                optionalNemesis = Optional.of((Mob)nearby);
            }
        }
        brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_NEMESIS, optionalNemesis);
        brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_HUNTABLE_HOGLIN, optionalHuntableHoglin);
        brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED, optionalZombified);
        brain.setMemory(MemoryModuleType.NEAREST_TARGETABLE_PLAYER_NOT_WEARING_GOLD, optionalPlayerNotGilded);
        brain.setMemory(ModMemoryModuleTypes.NEAREST_VISIBLE_ADULT_HOGLIN.get(), optionalNearestVisibleAdultHoglin);
        brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLINS, visibleAdultPiglins);
        brain.setMemory(MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT, visibleAdultPiglins.size());
    }

    public static void addAdditionalIdleMovementBehaviors(GateBehavior<Piglin> gateBehavior) {
        BrainHelper.addToGateBehavior(gateBehavior,
                Pair.of(new InteractWith<>(
                        EntityType.PIGLIN, 8,
                        AgeableHelper::canBreed,
                        AgeableHelper::canBreed,
                        ModMemoryModuleTypes.BREEDING_TARGET.get(), 0.5F, 2),
                1),
                Pair.of(new CreateBabyTask<>(), 3),
                Pair.of(new ShareGoldTask<>(), 2),
                Pair.of(new CraftWithGoldTask<>(), 2),
                Pair.of(new FeedHoglinsTask<>(), 2)
        );
    }
}
