package com.infamous.sapience.util;

import com.infamous.sapience.Sapience;
import com.infamous.sapience.SapienceConfig;
import com.infamous.sapience.capability.greed.GreedProvider;
import com.infamous.sapience.capability.greed.IGreed;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.monster.piglin.PiglinEntity;
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

    private static IGreed getGreedCapability(Entity entity){
        LazyOptional<IGreed> lazyCap = entity.getCapability(GreedProvider.GREED_CAPABILITY);
        if (lazyCap.isPresent()) {
            return lazyCap.orElseThrow(() -> new IllegalStateException("Couldn't get the greed capability from the Entity!"));
        }
        Sapience.LOGGER.error("Couldn't get the greed capability from the Entity in GreedHelper#getAgeableCapability!");
        return null;
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

    public static void dropGreedItems(LivingEntity mobEntity){
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


    private static int getIngotRequirement(EquipmentSlotType slotType){
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

    private static EquipmentSlotType getSlotForItem(Item item){
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

    private static ItemStack craftArmorFromGreedInventory(MobEntity mobEntity, EquipmentSlotType slotType){
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

    public static void checkCraftEquipment(PiglinEntity piglinEntity){
        IGreed greed = getGreedCapability(piglinEntity);
        if(greed != null){
            if (!greed.getGreedInventory().isEmpty()) {
                // track the amount of equipped items the piglin has currently
                int heldEquipmentAmount = 0;

                // verify that the piglin can craft, cannot after crafting one item
                boolean didCraft = false;

                // Check each equipment slot for an item
                for (EquipmentSlotType slot : EquipmentSlotType.values()) {
                    // for now, skip offhand equipment check
                    if(slot == EquipmentSlotType.OFFHAND){
                        continue;
                    }

                    ItemStack stackFromSlot = piglinEntity.getItemStackFromSlot(slot);
                    // Skip to the next slot if an item was found in the slot
                    if (!stackFromSlot.isEmpty()) {
                        // If the slot is occupied by armor
                        boolean isValidEquipment = SapienceConfig.COMMON.PIGLINS_PREFER_CRAFTED_EQUIPMENT.get() ?
                                isGoldArmorOrWeapon(stackFromSlot) : isArmorOrWeapon(stackFromSlot);
                        boolean isCrossbow = stackFromSlot.isCrossbowStack();
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
                    return addStackToGreedInventoryCheckBartered(mobEntity, stackToAdd, didBarter);
                }
                else if (stackToAdd.getItem().isIn(Tags.Items.STORAGE_BLOCKS_GOLD)) {
                    int blockCount = stackToAdd.getCount();
                    ItemStack blocksToIngotsStack = new ItemStack(Items.GOLD_INGOT, blockCount * 9);
                    blocksToIngotsStack.setTag(stackToAdd.getTag());
                    return addStackToGreedInventoryCheckBartered(mobEntity, blocksToIngotsStack, didBarter);
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
            return addStackToGreedInventoryCheckBartered(mobEntity, goldIngotStack, didBarter);
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
            return addStackToGreedInventoryCheckBartered(mobEntity, stackToAdd, didBarter);
        }
    }

    public static ItemStack addStackToGreedInventoryCheckBartered(MobEntity mobEntity, ItemStack stack, boolean didBarter){
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
                boolean isCrossbow =  stackFromSlot.isCrossbowStack();
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

    public static void giveAllyDesiredItem(Set<Item> allyDesiredItems, PiglinEntity owner, PiglinEntity ally) {
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
                    GreedHelper.checkCraftEquipment(owner);
                    /*
                    if(!ally.getItemStackFromSlot(slotType).isEmpty()){
                        ItemStack dropStack = ally.getItemStackFromSlot(slotType);
                        ally.entityDropItem(dropStack);
                    }
                     */
                    owner.swingArm(Hand.OFF_HAND);
                    //ally.swingArm(Hand.OFF_HAND);
                    //ally.setItemStackToSlot(slotType, craftedItem);
                    PiglinTasksHelper.dropOffhandItemAndSetItemStackToOffhand(ally, craftedItem);
                    PiglinTasksHelper.setAdmiringItem(ally);
                    PiglinTasksHelper.clearWalkPath(ally);
                    ally.world.setEntityState(ally, (byte) GeneralHelper.ACCEPT_ID);
                    allyDesiredItems.remove(desiredItem);
                }
            }
        }
    }
}
