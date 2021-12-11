package com.infamous.sapience.capability.greed;

import net.minecraft.world.SimpleContainer;

public class Greed implements IGreed {

    private SimpleContainer greedInventory = new SimpleContainer(8);
    private boolean sharingGold;

    public Greed(){
    }

    @Override
    public SimpleContainer getGreedInventory() {
        return this.greedInventory;
    }

    @Override
    public boolean isSharingGold() {
        return this.sharingGold;
    }

    @Override
    public void setSharingGold(boolean shareGold) {
        this.sharingGold = shareGold;
    }

}
