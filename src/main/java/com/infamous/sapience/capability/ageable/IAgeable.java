package com.infamous.sapience.capability.ageable;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.Map;

public interface IAgeable {

    boolean wasBorn();

    void setBorn(boolean wasBorn);

    int getGrowingAge();

    int getForcedAge();

    int getForcedAgeTimer();

    void ageUp(int growthSeconds, boolean updateForcedAge);

    void addGrowth(int growth);

    void setGrowingAge(int age);

    void setForcedAge(int forcedAge);

    void setForcedAgeTimer(int forcedAgeTimer);

    void onGrowingAdult();

    // BREEDING METHODS //

    Inventory getFoodInventory();

    Map<Item, Integer> getFoodValues();

    byte getFoodLevel();

    void setFoodLevel(byte foodLevel);

    boolean isHungry();

    void depleteFoodReserves();

    /**
     * @return calculated food value from item stacks in this villager's inventory
     */
    default int getFoodValueFromInventory() {
        Inventory inventory = this.getFoodInventory();
        return this.getFoodValues().entrySet().stream().mapToInt(
                (foodValueEntry) -> inventory.count(foodValueEntry.getKey()) * foodValueEntry.getValue()).sum();
    }

    default void eat() {
        if (this.isHungry() && this.getFoodValueFromInventory() != 0) {
            for(int i = 0; i < this.getFoodInventory().getSizeInventory(); ++i) {
                ItemStack itemstack = this.getFoodInventory().getStackInSlot(i);
                if (!itemstack.isEmpty()) {
                    Integer integer = this.getFoodValues().get(itemstack.getItem());
                    if (integer != null) {
                        int j = itemstack.getCount();

                        for(int k = j; k > 0; --k) {
                            this.setFoodLevel((byte)(this.getFoodLevel() + integer));
                            this.getFoodInventory().decrStackSize(i, 1);
                            if (!this.isHungry()) {
                                return;
                            }
                        }
                    }
                }
            }

        }
    }

    default boolean replaceItemInFoodInventory(int inventorySlot, ItemStack itemStackIn) {
        int i = inventorySlot - 300;
        if (i >= 0 && i < this.getFoodInventory().getSizeInventory()) {
            this.getFoodInventory().setInventorySlotContents(i, itemStackIn);
            return true;
        } else {
            return false;
        }
    }

    boolean canBreed();

    boolean canSelfAge();

    default void decreaseFoodLevel(int qty) {
        this.setFoodLevel((byte)(this.getFoodLevel() - qty));
    }

}