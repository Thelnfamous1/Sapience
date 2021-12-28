package com.infamous.sapience.capability.greed;

import net.minecraft.world.SimpleContainer;

public interface Greed {

    SimpleContainer getGreedInventory();

    boolean isSharingGold();

    void setSharingGold(boolean shareGold);


    class Impl implements Greed {

        private final SimpleContainer greedInventory = new SimpleContainer(8);
        private boolean sharingGold;

        public Impl(){
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
}
