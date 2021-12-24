package com.infamous.sapience.util;

import com.infamous.sapience.Sapience;
import com.infamous.sapience.SapienceConfig;
import com.infamous.sapience.capability.greed.GreedProvider;
import com.infamous.sapience.capability.greed.IGreed;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.item.*;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GreedHelper {

    public static final String BARTERED = "Bartered";

    private static IGreed getGreedCapability(Entity entity){
        LazyOptional<IGreed> lazyCap = entity.getCapability(GreedProvider.GREED_CAPABILITY);
        if (lazyCap.isPresent()) {
            return lazyCap.orElseThrow(() -> new IllegalStateException("Couldn't get the greed capability from the Entity!"));
        }
        Sapience.LOGGER.error("Couldn't get the greed capability from the Entity in GreedHelper#getAgeableCapability!");
        return null;
    }

    public static boolean doesGreedInventoryHaveGold(Mob mobEntity){
        IGreed greed = getGreedCapability(mobEntity);
        if(greed != null){
            List<Item> forgeIngotsGold = Tags.Items.INGOTS_GOLD.getValues();
            Set<Item> goldIngots = new HashSet<>(forgeIngotsGold);
            return greed.getGreedInventory().hasAnyOf(goldIngots);
        }
        return false;
    }

    public static void dropGreedItems(LivingEntity mobEntity){
        IGreed greed = getGreedCapability(mobEntity);
        if(greed != null){
            getGreedItemsForDrop(mobEntity) // Filter out traded items
                    .forEach(mobEntity::spawnAtLocation);
        }
    }

    public static Collection<ItemStack> getGreedItemsForDrop(LivingEntity mobEntity) {
        IGreed greed = getGreedCapability(mobEntity);
        if(greed != null){
            return greed.getGreedInventory().removeAllItems().stream()
                    .filter(GreedHelper::wasNotTraded)
                    .collect(Collectors.toList());
        } else{
            return Collections.emptyList();
        }
    }

    private static boolean wasNotTraded(ItemStack stack) {
        CompoundTag compoundNBT = stack.getTag();
        return compoundNBT != null && !compoundNBT.getBoolean(GreedHelper.BARTERED);
    }

    public static boolean isSharingGold(Mob mobEntity){
        IGreed greed = getGreedCapability(mobEntity);
        if(greed != null){
            return greed.isSharingGold();
        }
        return false;
    }


    private static int getIngotRequirement(EquipmentSlot slotType){
        switch (slotType){
            case HEAD:
                return 5;
            case CHEST:
                return 8;
            case LEGS:
                return 7;
            case FEET:
                return 4;
            case MAINHAND:
                return 2;
            default:
                return 0;
        }
    }

    private static EquipmentSlot getSlotForItem(Item item){
        if(item == Items.GOLDEN_HELMET){
            return EquipmentSlot.HEAD;
        }
        else if(item == Items.GOLDEN_CHESTPLATE){
            return EquipmentSlot.CHEST;
        }
        else if(item == Items.GOLDEN_LEGGINGS){
            return EquipmentSlot.LEGS;
        }
        else if(item == Items.GOLDEN_BOOTS){
            return EquipmentSlot.FEET;
        }
        else if(item == Items.GOLDEN_SWORD){
            return EquipmentSlot.MAINHAND;
        }
        else{
            return EquipmentSlot.OFFHAND;
        }
    }

    private static ItemStack getEquipmentForIngotCount(int ingotCount){
        switch (ingotCount){
            case 5:
                return new ItemStack(Items.GOLDEN_HELMET);
            case 8:
                return new ItemStack(Items.GOLDEN_CHESTPLATE);
            case 7:
                return new ItemStack(Items.GOLDEN_LEGGINGS);
            case 4:
                return new ItemStack(Items.GOLDEN_BOOTS);
            case 2:
                return new ItemStack(Items.GOLDEN_SWORD);
            default:
                return ItemStack.EMPTY;
        }
    }

    private static ItemStack craftArmorFromGreedInventory(Mob mobEntity, EquipmentSlot slotType){
        IGreed greed = getGreedCapability(mobEntity);
        if(greed != null){
            int ingotRequirement = getIngotRequirement(slotType);
            if(ingotRequirement == 0){
                return ItemStack.EMPTY;
            }
            int ingotsFound = 0;
            List<ItemStack> usedStacks = new ArrayList<>();
            for(int slotIndex = 0; slotIndex < greed.getGreedInventory().getContainerSize(); slotIndex++){
                ItemStack stackFromSlot = greed.getGreedInventory().getItem(slotIndex);
                // If the found stack is a gold ingot
                if(stackFromSlot.is(Tags.Items.INGOTS_GOLD) && !stackFromSlot.isEmpty()){
                    for(int i = 1; i <= stackFromSlot.getCount(); i++){
                        // split 1 off from the stack and add it to usedStacks
                        usedStacks.add(stackFromSlot.split(1));
                        ingotsFound++;
                        // if we can craft the equipment, attempt do so
                        if(ingotsFound == ingotRequirement){
                            ItemStack craftedEquipment = getEquipmentForIngotCount(ingotsFound);
                            // if unsuccessful, return all stacks in usedStacks to the greed inventory
                            if(craftedEquipment.isEmpty()){
                                for(ItemStack itemStack : usedStacks){
                                    greed.getGreedInventory().addItem(itemStack);
                                }
                            }
                            // return the result, whether successful or not
                            return craftedEquipment;
                        }
                    }
                }
            }
            // if unsuccessful, return all stacks in usedStacks to the greed inventory
            for(ItemStack itemStack : usedStacks){
                greed.getGreedInventory().addItem(itemStack);
            }
            return ItemStack.EMPTY;
        }
        return ItemStack.EMPTY;
    }

    public static void checkCraftEquipment(Piglin piglinEntity){
        IGreed greed = getGreedCapability(piglinEntity);
        if(greed != null){
            if (!greed.getGreedInventory().isEmpty()) {
                // track the amount of equipped items the piglin has currently
                int heldEquipmentAmount = 0;

                // verify that the piglin can craft, cannot after crafting one item
                boolean didCraft = false;

                // Check each equipment slot for an item
                for (EquipmentSlot slot : EquipmentSlot.values()) {
                    // for now, skip offhand equipment check
                    if(slot == EquipmentSlot.OFFHAND){
                        continue;
                    }

                    ItemStack stackFromSlot = piglinEntity.getItemBySlot(slot);
                    // Skip to the next slot if an item was found in the slot
                    if (!stackFromSlot.isEmpty()) {
                        // If the slot is occupied by armor
                        boolean isValidEquipment = SapienceConfig.COMMON.PIGLINS_PREFER_CRAFTED_EQUIPMENT.get() ?
                                isGoldArmorOrWeapon(stackFromSlot) : isArmorOrWeapon(stackFromSlot);
                        boolean isCrossbow = stackFromSlot.useOnRelease();
                        if (isValidEquipment || isCrossbow){
                            heldEquipmentAmount++;
                        }
                        else if(!didCraft){
                            ItemStack craftedEquipment = craftArmorFromGreedInventory(piglinEntity, slot);
                            // If the equipment was crafted
                            if (!craftedEquipment.isEmpty()){
                                PiglinTasksHelper.dropOffhandItemAndSetItemStackToOffhand(piglinEntity, craftedEquipment);
                                PiglinTasksHelper.setAdmiringItem(piglinEntity);
                                PiglinTasksHelper.clearWalkPath(piglinEntity);
                                didCraft = true;
                                heldEquipmentAmount++;
                            }
                        }
                    }
                    else if(!didCraft){
                        ItemStack craftedEquipment = craftArmorFromGreedInventory(piglinEntity, slot);
                        // If the equipment was crafted
                        if (!craftedEquipment.isEmpty()){
                            PiglinTasksHelper.dropOffhandItemAndSetItemStackToOffhand(piglinEntity, craftedEquipment);
                            PiglinTasksHelper.setAdmiringItem(piglinEntity);
                            PiglinTasksHelper.clearWalkPath(piglinEntity);
                            didCraft = true;
                            heldEquipmentAmount++;
                        }
                    }
                }

                // Attempt to give gold to a different Piglin if fully equipped
                greed.setSharingGold(heldEquipmentAmount >= 5);
            }
        }
    }


    private static boolean isArmorOrWeapon(ItemStack stack){
        return stack.getItem() instanceof ArmorItem || stack.getItem() instanceof TieredItem;
    }

    private static boolean isGoldArmorOrWeapon(ItemStack stack){
        if(stack.getItem() instanceof ArmorItem){
            return ((ArmorItem) stack.getItem()).getMaterial() == ArmorMaterials.GOLD;
        }
        else if(stack.getItem() instanceof TieredItem){
            return ((TieredItem) stack.getItem()).getTier() == Tiers.GOLD;
        }
        else return false;
    }

    public static ItemStack addGreedItemToGreedInventory(Mob mobEntity, ItemStack stackToAdd, boolean didBarter) {
        IGreed greed = getGreedCapability(mobEntity);
        if(greed != null){
            if(!stackToAdd.isEmpty()){
                SimpleContainer greedInventory = greed.getGreedInventory();
                if (stackToAdd.is(Tags.Items.INGOTS_GOLD)){
                    return addStackToGreedInventoryCheckBartered(mobEntity, stackToAdd, didBarter);
                }
                else if (stackToAdd.is(Tags.Items.STORAGE_BLOCKS_GOLD)) {
                    int blockCount = stackToAdd.getCount();
                    ItemStack blocksToIngotsStack = new ItemStack(Items.GOLD_INGOT, blockCount * 9);
                    blocksToIngotsStack.setTag(stackToAdd.getTag());
                    return addStackToGreedInventoryCheckBartered(mobEntity, blocksToIngotsStack, didBarter);
                } else if (stackToAdd.is(Tags.Items.NUGGETS_GOLD)){
                    return addGoldNuggetsToGreedInventory(mobEntity, stackToAdd, greedInventory, didBarter);
                }
                else{
                    return stackToAdd;
                }
            }
            else{
                return stackToAdd;
            }
        }
        return ItemStack.EMPTY;
    }

    private static ItemStack addGoldNuggetsToGreedInventory(Mob mobEntity, ItemStack stackToAdd, SimpleContainer greedInventory, boolean didBarter) {
        int nuggetCount = stackToAdd.getCount();
        boolean canAutoMerge = nuggetCount % 9 == 0;
        if(canAutoMerge){
            ItemStack goldIngotStack = new ItemStack(Items.GOLD_INGOT, nuggetCount / 9);
            goldIngotStack.setTag(stackToAdd.getTag());
            return addStackToGreedInventoryCheckBartered(mobEntity, goldIngotStack, didBarter);
        }
        else{
            for(int slotIndex = 0; slotIndex < greedInventory.getContainerSize(); slotIndex++){
                ItemStack stackInSlot = greedInventory.getItem(slotIndex);
                if(stackInSlot.is(Tags.Items.NUGGETS_GOLD) && stackInSlot.getCount() + nuggetCount <= stackInSlot.getMaxStackSize()){
                    stackToAdd.shrink(nuggetCount);
                    stackInSlot.grow(nuggetCount);
                    if(stackInSlot.getCount() % 9 == 0){
                        ItemStack goldIngotStack = new ItemStack(Items.GOLD_INGOT, nuggetCount / 9);
                        goldIngotStack.setTag(stackToAdd.getTag());
                        greedInventory.setItem(slotIndex, goldIngotStack);
                        break;
                    }
                    if(stackToAdd.isEmpty()){
                        return stackToAdd;
                    }
                }
            }
            return addStackToGreedInventoryCheckBartered(mobEntity, stackToAdd, didBarter);
        }
    }

    public static ItemStack addStackToGreedInventoryCheckBartered(Mob mobEntity, ItemStack stack, boolean didBarter){
        IGreed greed = getGreedCapability(mobEntity);
        if(greed != null){
            if (didBarter) {
                CompoundTag compoundNBT = stack.getOrCreateTag();
                compoundNBT.putBoolean(GreedHelper.BARTERED, true);
            }
            return greed.getGreedInventory().addItem(stack);
        }
        return stack;
    }

    public static Set<Item> getDesiredItems(LivingEntity ally) {
        Set<Item> desiredItems = new HashSet<>();
        for(EquipmentSlot slotType : EquipmentSlot.values()){
            if(slotType == EquipmentSlot.OFFHAND){
                continue;
            }
            int ingotRequirement = getIngotRequirement(slotType);
            ItemStack desiredItemStack = getEquipmentForIngotCount(ingotRequirement);
            Item desiredItem = desiredItemStack.getItem();
            ItemStack stackFromSlot = ally.getItemBySlot(slotType);

            if(!stackFromSlot.isEmpty()){
                boolean isCrossbow =  stackFromSlot.useOnRelease();
                boolean isValidEquipment = SapienceConfig.COMMON.PIGLINS_PREFER_CRAFTED_EQUIPMENT.get() ?
                        isGoldArmorOrWeapon(stackFromSlot) : isArmorOrWeapon(stackFromSlot);

                if(!isValidEquipment && !isCrossbow){
                    if(!desiredItemStack.isEmpty()){
                        desiredItems.add(desiredItem);
                    }
                }
            }
            else if(!desiredItemStack.isEmpty()){
                desiredItems.add(desiredItem);
            }
        }
        return desiredItems;
    }

    public static void giveAllyDesiredItem(Set<Item> allyDesiredItems, Piglin owner, Piglin ally) {
        if(!allyDesiredItems.isEmpty()){
            Item desiredItem = null;
            for(Item item: allyDesiredItems) {
                desiredItem = item;
                break;
            }
            EquipmentSlot slotType = getSlotForItem(desiredItem);
            if(slotType != EquipmentSlot.OFFHAND){
                ItemStack craftedItem = craftArmorFromGreedInventory(owner, slotType);
                if(!craftedItem.isEmpty()){
                    GreedHelper.checkCraftEquipment(owner);
                    /*
                    if(!ally.getItemStackFromSlot(slotType).isEmpty()){
                        ItemStack dropStack = ally.getItemStackFromSlot(slotType);
                        ally.entityDropItem(dropStack);
                    }
                     */
                    owner.swing(InteractionHand.OFF_HAND);
                    //ally.swingArm(Hand.OFF_HAND);
                    //ally.setItemStackToSlot(slotType, craftedItem);
                    PiglinTasksHelper.dropOffhandItemAndSetItemStackToOffhand(ally, craftedItem);
                    PiglinTasksHelper.setAdmiringItem(ally);
                    PiglinTasksHelper.clearWalkPath(ally);
                    ally.level.broadcastEntityEvent(ally, (byte) GeneralHelper.ACCEPT_ID);
                    allyDesiredItems.remove(desiredItem);
                }
            }
        }
    }
}
