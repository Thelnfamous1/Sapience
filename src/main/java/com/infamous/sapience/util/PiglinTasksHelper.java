package com.infamous.sapience.util;

import com.google.common.collect.ImmutableList;
import com.infamous.sapience.Sapience;
import com.infamous.sapience.mod.ModMemoryModuleTypes;
import com.infamous.sapience.tasks.CraftWithGoldTask;
import com.infamous.sapience.tasks.CreateBabyTask;
import com.infamous.sapience.tasks.FeedHoglinsTask;
import com.infamous.sapience.tasks.ShareGoldTask;
import com.mojang.datafixers.util.Pair;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.ai.brain.BrainUtil;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.schedule.Activity;
import net.minecraft.entity.ai.brain.task.*;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.monster.ZoglinEntity;
import net.minecraft.entity.monster.ZombifiedPiglinEntity;
import net.minecraft.entity.monster.piglin.AbstractPiglinEntity;
import net.minecraft.entity.monster.piglin.PiglinAction;
import net.minecraft.entity.monster.piglin.PiglinEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameterSets;
import net.minecraft.loot.LootParameters;
import net.minecraft.loot.LootTable;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.*;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.Tags;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class PiglinTasksHelper {

    private static final Tags.IOptionalNamedTag<Item> PIGLIN_FOOD_ITEMS = ItemTags.createOptional(new ResourceLocation(Sapience.MODID, "piglin_food_items"));
    private static final ResourceLocation PIGLIN_BARTERING_CHEAP = new ResourceLocation(Sapience.MODID, "gameplay/piglin_bartering_cheap");
    private static final ResourceLocation PIGLIN_BARTERING_EXPENSIVE = new ResourceLocation(Sapience.MODID, "gameplay/piglin_bartering_expensive");

    private static final RangedInteger RANGED_FEEDING_TIMER = TickRangeConverter.convertRange(30, 120);

    public static boolean canPickUpFoodStack(PiglinEntity piglinEntity, ItemStack itemStack) {
        Item item = itemStack.getItem();
        if (item.isIn(ItemTags.PIGLIN_REPELLENTS)) {
            return false;
        } else if (PiglinTasksHelper.hasAdmiringDisabled(piglinEntity) && piglinEntity.getBrain().hasMemory(MemoryModuleType.ATTACK_TARGET)) {
            return false;
        } else if (itemStack.isPiglinCurrency()) {
            return PiglinTasksHelper.hasOpenOffhandSlot(piglinEntity);
        } else {
            //boolean canAddItemStackToInventory = AgeableHelper.canAddItemStackToFoodInventory(piglinEntity, itemStack);
            if (PiglinTasksHelper.isPiglinFoodItem(item)) {
                return !PiglinTasksHelper.hasAteRecently(piglinEntity) && hasOpenOffhandSlot(piglinEntity);
            }
            else{
                return false;
            }
        }
    }

    private static void dropItems(AbstractPiglinEntity piglinEntity, List<ItemStack> itemStacks, Vector3d vector3d) {
        if (!itemStacks.isEmpty()) {
            piglinEntity.swingArm(Hand.OFF_HAND);

            for(ItemStack itemstack : itemStacks) {
                BrainUtil.spawnItemNearEntity(piglinEntity, itemstack, vector3d.add(0.0D, 1.0D, 0.0D));
            }
        }
    }

    private static Vector3d getNearbyVectorOrPositionVector(AbstractPiglinEntity piglinEntity) {
        Vector3d vector3d = RandomPositionGenerator.getLandPos(piglinEntity, 4, 2);
        return vector3d == null ? piglinEntity.getPositionVec() : vector3d;
    }

    private static void dropItemsNearSelf(AbstractPiglinEntity piglinEntity, List<ItemStack> itemStacks) {
        dropItems(piglinEntity, itemStacks, getNearbyVectorOrPositionVector(piglinEntity));
    }

    public static void setAteRecently(AbstractPiglinEntity piglinEntity) {
        piglinEntity.getBrain().replaceMemory(MemoryModuleType.ATE_RECENTLY, true, 200L);
    }

    public static ItemStack extractSingletonFromItemEntity(ItemEntity itemEntity) {
        ItemStack itemstack = itemEntity.getItem();
        ItemStack itemstack1 = itemstack.split(1);
        if (itemstack.isEmpty()) {
            itemEntity.remove();
        } else {
            itemEntity.setItem(itemstack);
        }

        return itemstack1;
    }



    public static boolean isPiglinFoodItem(Item item) {
        return item.isIn(PIGLIN_FOOD_ITEMS) && item.isFood();
    }

    public static boolean hasAteRecently(AbstractPiglinEntity piglinEntity) {
        return piglinEntity.getBrain().hasMemory(MemoryModuleType.ATE_RECENTLY);
    }

    public static boolean hasOpenOffhandSlot(PiglinEntity piglinEntity) {
        return piglinEntity.getHeldItemOffhand().isEmpty() || !isPiglinLoved(piglinEntity.getHeldItemOffhand().getItem());
    }

    public static boolean isPiglinLoved(Item item) {
        return item.isIn(ItemTags.PIGLIN_LOVED);
    }

    private static boolean hasAdmiringDisabled(AbstractPiglinEntity piglinEntity) {
        return piglinEntity.getBrain().hasMemory(MemoryModuleType.ADMIRING_DISABLED);
    }


    public static ImmutableList getInteractionTasks(){
        return ImmutableList.of(
                // Originals
                Pair.of(new WalkRandomlyTask(0.6F),
                        2),
                Pair.of(InteractWithEntityTask.func_220445_a(
                        EntityType.PIGLIN, 8,
                        MemoryModuleType.INTERACTION_TARGET, 0.6F, 2),
                        2),
                Pair.of(new SupplementedTask<>(
                        PiglinTasksHelper::doesNotHaveNearestPlayerHoldingWantedItem,
                        new WalkTowardsLookTargetTask(0.6F, 3)),
                        2),
                Pair.of(new DummyTask(30, 60),
                        1),
                // Additions
                Pair.of(new InteractWithEntityTask<>(
                        EntityType.PIGLIN, 8,
                        AgeableHelper::canBreed,
                        AgeableHelper::canBreed,
                        ModMemoryModuleTypes.BREEDING_TARGET.get(), 0.5F, 2),
                        1),
                Pair.of(new CreateBabyTask(), 3),
                Pair.of(new ShareGoldTask<>(), 2),
                Pair.of(new CraftWithGoldTask<>(), 2),
                Pair.of(new FeedHoglinsTask<>(), 2)
                );

    }

    public static boolean hasNearbyBabyHoglin(PiglinEntity piglinEntity){
        return piglinEntity.getBrain().hasMemory(MemoryModuleType.NEAREST_VISIBLE_BABY_HOGLIN);
    }

    public static boolean hasHuntedRecently(PiglinEntity piglinEntity){
        return piglinEntity.getBrain().hasMemory(MemoryModuleType.HUNTED_RECENTLY);
    }

    public static boolean hasNotFedRecently(PiglinEntity piglinEntity){
        return !hasFedRecently(piglinEntity);
    }

    public static void setFedRecently(PiglinEntity piglinEntity) {
        piglinEntity.getBrain().replaceMemory(ModMemoryModuleTypes.FED_RECENTLY.get(), true, (long) RANGED_FEEDING_TIMER.getRandomWithinRange(piglinEntity.world.rand));
    }

    private static boolean hasFedRecently(PiglinEntity piglinEntity){
        return piglinEntity.getBrain().hasMemory(ModMemoryModuleTypes.FED_RECENTLY.get());
    }

    public static boolean outnumbersHoglins(PiglinEntity piglinEntity){
        return !isOutnumberedByHoglins(piglinEntity);
    }

    private static boolean isOutnumberedByHoglins(PiglinEntity piglinEntity) {
        int piglinCount = piglinEntity.getBrain().getMemory(MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT).orElse(0) + 1;
        int hoglinCount = piglinEntity.getBrain().getMemory(MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT).orElse(0);
        return hoglinCount > piglinCount;
    }

    private static boolean hasNearestPlayerHoldingWantedItem(LivingEntity livingEntity) {
        return livingEntity.getBrain().hasMemory(MemoryModuleType.NEAREST_PLAYER_HOLDING_WANTED_ITEM);
    }

    private static boolean doesNotHaveNearestPlayerHoldingWantedItem(LivingEntity livingEntity) {
        return !hasNearestPlayerHoldingWantedItem(livingEntity);
    }

    public static void removeTimeTryingToReachAdmireItem(PiglinEntity piglinEntity){
        piglinEntity.getBrain().removeMemory(MemoryModuleType.TIME_TRYING_TO_REACH_ADMIRE_ITEM);
    }

    public static void dropOffhandItemAndSetItemStackToOffhand(AbstractPiglinEntity piglinEntity, ItemStack itemStack) {
        if (hasOffhandItem(piglinEntity)) {
            piglinEntity.entityDropItem(piglinEntity.getHeldItem(Hand.OFF_HAND));
        }
        setItemStackToOffhandAndPersist(piglinEntity, itemStack);
    }

    private static boolean hasOffhandItem(AbstractPiglinEntity piglinEntity) {
        return !piglinEntity.getHeldItemOffhand().isEmpty();
    }

    private static void setItemStackToOffhandAndPersist(AbstractPiglinEntity piglinEntity, ItemStack itemStack) {
        /*
        if (itemStack.isPiglinCurrency()) {
            piglinEntity.setItemStackToSlot(EquipmentSlotType.OFFHAND, itemStack);
            piglinEntity.func_233663_d_(EquipmentSlotType.OFFHAND);
        } else {

         */
            piglinEntity.setItemStackToSlot(EquipmentSlotType.OFFHAND, itemStack);
            piglinEntity.func_233663_d_(EquipmentSlotType.OFFHAND);
            piglinEntity.enablePersistence();
        //}

    }

    public static void clearWalkPath(AbstractPiglinEntity piglinEntity) {
        piglinEntity.getBrain().removeMemory(MemoryModuleType.WALK_TARGET);
        piglinEntity.getNavigator().clearPath();
    }

    public static boolean hasConsumableOffhandItem(AbstractPiglinEntity piglinEntity) {
        ItemStack offhandStack = piglinEntity.getHeldItemOffhand();
        return isPiglinFoodItem(offhandStack.getItem()) // this accounts for piglin foods
                || offhandStack.getUseAction() == UseAction.DRINK; // this accounts for honey bottles, milk buckets, and potions
    }

    public static void setAdmiringItem(LivingEntity livingEntity) {
        livingEntity.getBrain().replaceMemory(MemoryModuleType.ADMIRING_ITEM, true, 120L);
    }

    private static boolean hasAdmiringItem(AbstractPiglinEntity piglinEntity) {
        return piglinEntity.getBrain().hasMemory(MemoryModuleType.ADMIRING_ITEM);
    }

    public static boolean isNormalBarterItem(Item item){
        return item.isIn(Tags.Items.INGOTS_GOLD);
    }

    public static boolean isBarterItem(Item item) {
        return isNormalBarterItem(item)|| isAlternativeGreedItem(item);
    }

    private static boolean isAlternativeGreedItem(Item item) {
        return isExpensiveBarterItem(item) || isCheapBarterItem(item);
    }

    private static ActionResultType processInteractionForFoodItem(AbstractPiglinEntity piglinEntity, PlayerEntity playerEntity, Hand hand) {
        ItemStack itemstack = playerEntity.getHeldItem(hand);
        if (canAcceptFoodItem(piglinEntity, itemstack)) {
            ItemStack foodStack = itemstack.split(1);
            //addToFoodInventoryThenDropRemainder(piglinEntity, foodStack);
            //setAteRecently(piglinEntity);
            PiglinTasksHelper.dropOffhandItemAndSetItemStackToOffhand(piglinEntity, foodStack);
            PiglinTasksHelper.setAdmiringItem(piglinEntity);
            PiglinTasksHelper.clearWalkPath(piglinEntity);

            // REPUTATION
            ReputationHelper.setPreviousInteractor(piglinEntity, playerEntity);
            return ActionResultType.CONSUME;
        } else {
            return ActionResultType.PASS;
        }
    }


    private static ActionResultType processInteractionForPiglinGreedItem(AbstractPiglinEntity piglinEntity, PlayerEntity playerEntity, Hand handIn) {
        ItemStack itemstack = playerEntity.getHeldItem(handIn);
        if (canAcceptPiglinGreedItem(piglinEntity, itemstack)) {
            ItemStack greedStack = itemstack.split(1);
            PiglinTasksHelper.dropOffhandItemAndSetItemStackToOffhand(piglinEntity, greedStack);
            PiglinTasksHelper.setAdmiringItem(piglinEntity);
            PiglinTasksHelper.clearWalkPath(piglinEntity);

            // REPUTATION
            ReputationHelper.setPreviousInteractor(piglinEntity, playerEntity);
            return ActionResultType.CONSUME;
        } else {
            return ActionResultType.PASS;
        }
    }


    private static ActionResultType processInteractionForPiglinLovedItem(AbstractPiglinEntity piglinEntity, PlayerEntity playerEntity, Hand handIn) {
        ItemStack itemstack = playerEntity.getHeldItem(handIn);
        if (canAcceptPiglinLovedItem(piglinEntity, itemstack)) {
            ItemStack greedStack = itemstack.split(1);
            PiglinTasksHelper.dropOffhandItemAndSetItemStackToOffhand(piglinEntity, greedStack);
            PiglinTasksHelper.setAdmiringItem(piglinEntity);
            PiglinTasksHelper.clearWalkPath(piglinEntity);

            // REPUTATION
            ReputationHelper.setPreviousInteractor(piglinEntity, playerEntity);
            return ActionResultType.CONSUME;
        } else {
            return ActionResultType.PASS;
        }
    }

    private static boolean canAcceptPiglinGreedItem(AbstractPiglinEntity piglinEntity, ItemStack itemStack) {
        return !hasAdmiringDisabled(piglinEntity)
                && !hasAdmiringItem(piglinEntity)

                && (isAlternativeGreedItem(itemStack.getItem()) // a specific check for baby piglins to be able to accept piglin currency
                || isNormalBarterItem(itemStack.getItem()) && piglinEntity.isChild())

                && !hasConsumableOffhandItem(piglinEntity);
    }

    private static boolean canAcceptPiglinLovedItem(AbstractPiglinEntity piglinEntity, ItemStack itemStack) {
        return !hasAdmiringDisabled(piglinEntity)
                && !hasAdmiringItem(piglinEntity)
                && isPiglinLoved(itemStack.getItem())
                && !hasConsumableOffhandItem(piglinEntity);
    }

    private static boolean canAcceptFoodItem(AbstractPiglinEntity piglinEntity, ItemStack itemStack) {
        return !hasAdmiringDisabled(piglinEntity)
                && !hasAdmiringItem(piglinEntity)
                && isPiglinFoodItem(itemStack.getItem())
                && !hasAteRecently(piglinEntity)
                && !piglinEntity.isAggressive()
                && !hasConsumableOffhandItem(piglinEntity);
    }

    public static ActionResultType getAgeableActionResultType(AbstractPiglinEntity piglinEntity, PlayerEntity playerEntity, Hand handIn, ActionResultType actionResultTypeIn) {
        ItemStack itemStack = playerEntity.getHeldItem(handIn);
        if(isPiglinFoodItem(itemStack.getItem())){
            if(!piglinEntity.world.isRemote){
                actionResultTypeIn = processInteractionForFoodItem(piglinEntity, playerEntity, handIn);
            }
            else{
                boolean canAcceptFoodItem = canAcceptFoodItem(piglinEntity, itemStack)  && piglinEntity.func_234424_eM_() != PiglinAction.ADMIRING_ITEM;
                actionResultTypeIn = canAcceptFoodItem ? ActionResultType.SUCCESS : ActionResultType.PASS;
            }
        }
        return actionResultTypeIn;
    }

    public static ActionResultType getGreedActionResultType(AbstractPiglinEntity piglinEntity, PlayerEntity playerEntity, Hand handIn, ActionResultType actionResultTypeIn) {
        ItemStack itemStack = playerEntity.getHeldItem(handIn);
        if(isBarterItem(itemStack.getItem())){
            if(!piglinEntity.world.isRemote){
                actionResultTypeIn = processInteractionForPiglinGreedItem(piglinEntity, playerEntity, handIn);
            }
            else{
                boolean canTakeGreedItem = canAcceptPiglinGreedItem(piglinEntity, itemStack)  && piglinEntity.func_234424_eM_() != PiglinAction.ADMIRING_ITEM;
                actionResultTypeIn = canTakeGreedItem ? ActionResultType.SUCCESS : ActionResultType.PASS;
            }
        }
        else if(isPiglinLoved(itemStack.getItem())){
            if(!piglinEntity.world.isRemote){
                actionResultTypeIn = processInteractionForPiglinLovedItem(piglinEntity, playerEntity, handIn);
            }
            else{
                boolean canTakeLovedItem = canAcceptPiglinLovedItem(piglinEntity, itemStack)  && piglinEntity.func_234424_eM_() != PiglinAction.ADMIRING_ITEM;
                actionResultTypeIn = canTakeLovedItem ? ActionResultType.SUCCESS : ActionResultType.PASS;
            }
        }
        return actionResultTypeIn;
    }

    private static List<ItemStack> getBlockBarteringLoot(AbstractPiglinEntity piglinEntity){
        if(piglinEntity.world.getServer() != null){
            LootTable loottable = piglinEntity.world.getServer().getLootTableManager().getLootTableFromLocation(PIGLIN_BARTERING_EXPENSIVE);
            return loottable.generate((new LootContext.Builder((ServerWorld)piglinEntity.world)).withParameter(LootParameters.THIS_ENTITY, piglinEntity).withRandom(piglinEntity.world.rand).build(LootParameterSets.field_237453_h_));
        }
        return Collections.emptyList();
    }

    private static List<ItemStack> getNuggetBarteringLoot(AbstractPiglinEntity piglinEntity){
        if(piglinEntity.world.getServer() != null){
            LootTable loottable = piglinEntity.world.getServer().getLootTableManager().getLootTableFromLocation(PIGLIN_BARTERING_CHEAP);
            return loottable.generate((new LootContext.Builder((ServerWorld)piglinEntity.world)).withParameter(LootParameters.THIS_ENTITY, piglinEntity).withRandom(piglinEntity.world.rand).build(LootParameterSets.field_237453_h_));

        }
        return Collections.emptyList();
    }

    public static boolean isExpensiveBarterItem(Item item) {
        return item.isIn(Tags.Items.STORAGE_BLOCKS_GOLD);
    }

    public static boolean isCheapBarterItem(Item item) {
        return item.isIn(Tags.Items.NUGGETS_GOLD);
    }

    public static void dropBlockBarteringLoot(AbstractPiglinEntity piglinEntity){
        dropItemsAccountingForNearbyPlayer(piglinEntity, getBlockBarteringLoot(piglinEntity));
    }
    public static void dropNuggetBarteringLoot(AbstractPiglinEntity piglinEntity){
        dropItemsAccountingForNearbyPlayer(piglinEntity, getNuggetBarteringLoot(piglinEntity));
    }

    private static void dropItemsAccountingForNearbyPlayer(AbstractPiglinEntity piglinEntity, List<ItemStack> itemStacks) {
        Optional<PlayerEntity> optionalPlayerEntity = getNearestVisiblePlayer(piglinEntity);
        if (optionalPlayerEntity.isPresent()) {
            dropItemsNearPlayer(piglinEntity, optionalPlayerEntity.get(), itemStacks);
        } else {
            dropItemsNearSelf(piglinEntity, itemStacks);
        }

    }

    private static Optional<PlayerEntity> getNearestVisiblePlayer(AbstractPiglinEntity piglinEntity) {
        return piglinEntity.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_PLAYER);
    }


    private static void dropItemsNearPlayer(AbstractPiglinEntity piglinEntity, PlayerEntity playerEntity, List<ItemStack> itemStacks) {
        dropItems(piglinEntity, itemStacks, playerEntity.getPositionVec());
    }

    public static boolean canTarget(LivingEntity livingEntity) {
        return EntityPredicates.CAN_HOSTILE_AI_TARGET.test(livingEntity);
    }

    private static Optional<LivingEntity> getAngerTargetFromMemory(AbstractPiglinEntity piglinEntity) {
        return BrainUtil.getTargetFromMemory(piglinEntity, MemoryModuleType.ANGRY_AT);
    }

    public static boolean hasIdle(AbstractPiglinEntity piglinEntity) {
        return piglinEntity.getBrain().hasActivity(Activity.IDLE);
    }

    public static boolean isZombified(LivingEntity livingEntity) {
        return livingEntity instanceof ZombifiedPiglinEntity || livingEntity instanceof ZoglinEntity;
    }
}
