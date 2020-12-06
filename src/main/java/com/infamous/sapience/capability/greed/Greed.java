package com.infamous.sapience.capability.greed;

import net.minecraft.inventory.Inventory;

public class Greed implements IGreed {

    private Inventory greedInventory = new Inventory(8);
    private boolean sharingGold;

    public Greed(){
    }

    @Override
    public Inventory getGreedInventory() {
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
