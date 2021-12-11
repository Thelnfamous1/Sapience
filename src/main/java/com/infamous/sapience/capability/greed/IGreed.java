package com.infamous.sapience.capability.greed;

import net.minecraft.world.SimpleContainer;

public interface IGreed {

    SimpleContainer getGreedInventory();

    boolean isSharingGold();

    void setSharingGold(boolean shareGold);


}
