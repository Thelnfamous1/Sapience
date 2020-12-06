package com.infamous.sapience.util;

import com.infamous.sapience.Sapience;
import com.infamous.sapience.SapienceConfig;
import com.infamous.sapience.capability.greed.GreedProvider;
import com.infamous.sapience.capability.greed.IGreed;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Hand;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.util.LazyOptional;

import java.util.*;

public class GreedHelper {

    public static final String BARTERED = "Bartered";

    public static IGreed getGreedCapability(Entity entity){
        LazyOptional<IGreed> lazyCap = entity.getCapability(GreedProvider.GREED_CAPABILITY);
        if (lazyCap.isPresent()) {
            return lazyCap.orElseThrow(() -> new IllegalStateException("Couldn't get the greed capability from the Entity!"));
        }
        Sapience.LOGGER.error("Couldn't get the greed capability from the Entity in GreedHelper#getAgeableCapability!");
        return null;
    }

    public static boolean canAddItemStackToGreedInventory(MobEntity mobEntity, ItemStack itemStack) {
        IGreed ageable = getGreedCapability(mobEntity);
        if(ageable != null){
            return ageable.getGreedInventory().func_233541_b_(itemStack);
        }
        Sapience.LOGGER.error("Couldn't get the greed capability from the MobEntity in GreedHelper#canAddItemStackToFoodInventory!");
        return false;
    }

    public static boolean doesGreedInventoryHaveGold(MobEntity mobEntity){
        IGreed greed = getGreedCapability(mobEntity);
        if(greed != null){
            List<Item> forgeIngotsGold = Tags.Items.INGOTS_GOLD.getAllElements();
            Set<Item> goldIngots = new HashSet<>(forgeIngotsGold);
            return greed.getGreedInventory().hasAny(goldIngots);
        }
        return false;
    }

    public static void dropGreedItems(MobEntity mobEntity){
        IGreed greed = getGreedCapability(mobEntity);
        if(greed != null){
            greed.getGreedInventory().func_233543_f_().stream()
                    .filter(GreedHelper::wasNotTraded) // Filter out traded items
                    .forEach(mobEntity::entityDropItem);
        }
    }

    private static boolean wasNotTraded(ItemStack stack) {
        CompoundNBT compoundNBT = stack.getTag();
        return compoundNBT != null && !compoundNBT.getBoolean(GreedHelper.BARTERED);
    }

    public static boolean isSharingGold(MobEntity mobEntity){
        IGreed greed = getGreedCapability(mobEntity);
        if(greed != null){
            return greed.isSharingGold();
        }
        return false;
    }


    public static int getIngotRequirement(EquipmentSlotType slotType){
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

    public static EquipmentSlotType getSlotForItem(Item item){
        if(item == Items.GOLDEN_HELMET){
            return EquipmentSlotType.HEAD;
        }
        else if(item == Items.GOLDEN_CHESTPLATE){
            return EquipmentSlotType.CHEST;
        }
        else if(item == Items.GOLDEN_LEGGINGS){
            return EquipmentSlotType.LEGS;
        }
        else if(item == Items.GOLDEN_BOOTS){
            return EquipmentSlotType.FEET;
        }
        else if(item == Items.GOLDEN_SWORD){
            return EquipmentSlotType.MAINHAND;
        }
        else{
            return EquipmentSlotType.OFFHAND;
        }
    }

    public static ItemStack getEquipmentForIngotCount(int ingotCount){
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

    public static ItemStack craftArmorFromGreedInventory(MobEntity mobEntity, EquipmentSlotType slotType){
        IGreed greed = getGreedCapability(mobEntity);
        if(greed != null){
            int ingotRequirement = getIngotRequirement(slotType);
            if(ingotRequirement == 0){
                return ItemStack.EMPTY;
            }
            int ingotsFound = 0;
            List<ItemStack> usedStacks = new ArrayList<>();
            for(int slotIndex = 0; slotIndex < greed.getGreedInventory().getSizeInventory(); slotIndex++){
                ItemStack stackFromSlot = greed.getGreedInventory().getStackInSlot(slotIndex);
                // If the found stack is a gold ingot
                if(stackFromSlot.getItem().isIn(Tags.Items.INGOTS_GOLD) && !stackFromSlot.isEmpty()){
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

    public static boolean checkCraftEquipment(MobEntity mobEntity){
        IGreed greed = getGreedCapability(mobEntity);
        boolean crafted = false;
        if(greed != null){
            if (!greed.getGreedInventory().isEmpty()) {
                int heldEquipmentAmount = 0;

                // Check each equipment slot for an item
                for (EquipmentSlotType slot : EquipmentSlotType.values()) {
                    // for now, skip offhand equipment check
                    if(slot == EquipmentSlotType.OFFHAND){
                        continue;
                    }

                    ItemStack stackFromSlot = mobEntity.getItemStackFromSlot(slot);

                    boolean dropCurrentEquipment = false;
                    // Skip to the next slot if an item was found in the slot
                    if (!stackFromSlot.isEmpty()) {
                        // If the slot is occupied by armor
                        if(SapienceConfig.COMMON.PIGLINS_PREFER_CRAFTED_EQUIPMENT.get()){
                            boolean isGoldArmorOrWeapon = isGoldArmorOrWeapon(stackFromSlot);
                            if (isGoldArmorOrWeapon || stackFromSlot.getItem() instanceof CrossbowItem){
                                heldEquipmentAmount++;
                                continue;
                            }
                            else{
                                dropCurrentEquipment = true;
                            }
                        }
                        else{
                            boolean isArmorOrWeapon = isArmorOrWeapon(stackFromSlot);
                            if(isArmorOrWeapon || stackFromSlot.getItem() instanceof CrossbowItem){
                                heldEquipmentAmount++;
                                continue;
                            }
                            else{
                                dropCurrentEquipment = true;
                            }
                        }
                    }


                    ItemStack craftedEquipment = craftArmorFromGreedInventory(mobEntity, slot);
                    if (!craftedEquipment.isEmpty()){
                        crafted = true;
                        if(dropCurrentEquipment){
                            mobEntity.setItemStackToSlot(slot, ItemStack.EMPTY);
                            mobEntity.entityDropItem(stackFromSlot);
                        }
                        mobEntity.setItemStackToSlot(slot, craftedEquipment);
                    }
                }

                // Attempt to give gold to a different Piglin if fully equipped
                greed.setSharingGold(heldEquipmentAmount >= 5);
                return crafted;
            }
            return false;
        }
        return false;
    }



    public static boolean isArmorOrWeapon(ItemStack stack){
        return stack.getItem() instanceof ArmorItem || stack.getItem() instanceof TieredItem;
    }

    public static boolean isGoldArmorOrWeapon(ItemStack stack){
        if(stack.getItem() instanceof ArmorItem){
            return ((ArmorItem) stack.getItem()).getArmorMaterial() == ArmorMaterial.GOLD;
        }
        else if(stack.getItem() instanceof TieredItem){
            return ((TieredItem) stack.getItem()).getTier() == ItemTier.GOLD;
        }
        else return false;
    }

    public static ItemStack addGreedItemToGreedInventory(MobEntity mobEntity, ItemStack stackToAdd, boolean didBarter) {
        IGreed greed = getGreedCapability(mobEntity);
        if(greed != null){
            if(!stackToAdd.isEmpty()){
                Inventory greedInventory = greed.getGreedInventory();
                if (stackToAdd.getItem().isIn(Tags.Items.INGOTS_GOLD)){
                    return addStackToGreedInventoryCheckTraded(mobEntity, stackToAdd, didBarter);
                }
                else if (stackToAdd.getItem().isIn(Tags.Items.STORAGE_BLOCKS_GOLD)) {
                    int blockCount = stackToAdd.getCount();
                    ItemStack blocksToIngotsStack = new ItemStack(Items.GOLD_INGOT, blockCount * 9);
                    blocksToIngotsStack.setTag(stackToAdd.getTag());
                    return addStackToGreedInventoryCheckTraded(mobEntity, blocksToIngotsStack, didBarter);
                } else if (stackToAdd.getItem().isIn(Tags.Items.NUGGETS_GOLD)){
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

    private static ItemStack addGoldNuggetsToGreedInventory(MobEntity mobEntity, ItemStack stackToAdd, Inventory greedInventory, boolean didBarter) {
        int nuggetCount = stackToAdd.getCount();
        boolean canAutoMerge = nuggetCount % 9 == 0;
        if(canAutoMerge){
            ItemStack goldIngotStack = new ItemStack(Items.GOLD_INGOT, nuggetCount / 9);
            goldIngotStack.setTag(stackToAdd.getTag());
            return addStackToGreedInventoryCheckTraded(mobEntity, goldIngotStack, didBarter);
        }
        else{
            for(int slotIndex = 0; slotIndex < greedInventory.getSizeInventory(); slotIndex++){
                ItemStack stackInSlot = greedInventory.getStackInSlot(slotIndex);
                if(stackInSlot.getItem().isIn(Tags.Items.NUGGETS_GOLD) && stackInSlot.getCount() + nuggetCount <= stackInSlot.getMaxStackSize()){
                    stackToAdd.shrink(nuggetCount);
                    stackInSlot.grow(nuggetCount);
                    if(stackInSlot.getCount() % 9 == 0){
                        ItemStack goldIngotStack = new ItemStack(Items.GOLD_INGOT, nuggetCount / 9);
                        goldIngotStack.setTag(stackToAdd.getTag());
                        greedInventory.setInventorySlotContents(slotIndex, goldIngotStack);
                        break;
                    }
                    if(stackToAdd.isEmpty()){
                        return stackToAdd;
                    }
                }
            }
            return addStackToGreedInventoryCheckTraded(mobEntity, stackToAdd, didBarter);
        }
    }

    public static ItemStack addStackToGreedInventoryCheckTraded(MobEntity mobEntity, ItemStack stack, boolean didBarter){
        IGreed greed = getGreedCapability(mobEntity);
        if(greed != null){
            if (didBarter) {
                CompoundNBT compoundNBT = stack.getOrCreateTag();
                compoundNBT.putBoolean(GreedHelper.BARTERED, true);
            }
            return greed.getGreedInventory().addItem(stack);
        }
        return stack;
    }

    public static Set<Item> getDesiredItems(LivingEntity ally) {
        Set<Item> desiredItems = new HashSet<>();
        for(EquipmentSlotType slotType : EquipmentSlotType.values()){
            if(slotType == EquipmentSlotType.OFFHAND){
                continue;
            }
            int ingotRequirement = getIngotRequirement(slotType);
            ItemStack desiredItemStack = getEquipmentForIngotCount(ingotRequirement);
            Item desiredItem = desiredItemStack.getItem();
            ItemStack stackFromSlot = ally.getItemStackFromSlot(slotType);

            if(!stackFromSlot.isEmpty()){
                boolean isCrossbow =  stackFromSlot.getItem() instanceof CrossbowItem;
                if(SapienceConfig.COMMON.PIGLINS_PREFER_CRAFTED_EQUIPMENT.get()){
                    boolean isGoldArmorOrWeapon = isGoldArmorOrWeapon(stackFromSlot);
                    if(!isGoldArmorOrWeapon && !isCrossbow){
                        if(!desiredItemStack.isEmpty()){
                            desiredItems.add(desiredItem);
                        }
                    }
                }
                else{
                    boolean isArmorOrWeapon = isArmorOrWeapon(stackFromSlot);
                    if(!isArmorOrWeapon && !isCrossbow){
                        if(!desiredItemStack.isEmpty()){
                            desiredItems.add(desiredItem);
                        }
                    }
                }
            }
            else if(!desiredItemStack.isEmpty()){
                desiredItems.add(desiredItem);
            }
        }
        return desiredItems;
    }

    public static void giveAllyDesiredItem(Set<Item> allyDesiredItems, MobEntity owner, LivingEntity ally) {
        if(!allyDesiredItems.isEmpty()){
            Item desiredItem = null;
            for(Item item: allyDesiredItems) {
                desiredItem = item;
                break;
            }
            EquipmentSlotType slotType = getSlotForItem(desiredItem);
            if(slotType != EquipmentSlotType.OFFHAND){
                ItemStack craftedItem = craftArmorFromGreedInventory(owner, slotType);
                if(!craftedItem.isEmpty()){
                    IGreed greed = getGreedCapability(owner);
                    if(greed != null){
                        greed.setSharingGold(false);
                    }
                    if(!ally.getItemStackFromSlot(slotType).isEmpty()){
                        ItemStack dropStack = ally.getItemStackFromSlot(slotType);
                        ally.entityDropItem(dropStack);
                    }
                    owner.swingArm(Hand.OFF_HAND);
                    ally.swingArm(Hand.OFF_HAND);
                    ally.setItemStackToSlot(slotType, craftedItem);
                    ally.world.setEntityState(ally, (byte) AgeableHelper.ACCEPT_ID);
                    allyDesiredItems.remove(desiredItem);
                }
            }
        }
    }
}
