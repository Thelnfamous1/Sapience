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

    byte getFoodLevel();

    void setFoodLevel(byte foodLevel);

    void depleteFoodValue();

    boolean canBreed();

    boolean canSelfAge();

    default void decreaseFoodLevel(int qty) {
        this.setFoodLevel((byte)(this.getFoodLevel() - qty));
    }

    default void increaseFoodLevel(int qty){
        this.setFoodLevel((byte)(this.getFoodLevel() + qty));
    }

}