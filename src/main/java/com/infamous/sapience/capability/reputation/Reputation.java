package com.infamous.sapience.capability.reputation;

import net.minecraft.world.entity.ai.gossip.GossipContainer;

import javax.annotation.Nullable;
import java.util.UUID;

public interface Reputation {

    GossipContainer getGossipManager();

    long getLastGossipTime();

    void setLastGossipTime(long lastGossipTime);

    long getLastGossipDecay();

    void setLastGossipDecay(long lastGossipDecay);

    @Nullable
    UUID getPreviousInteractor();

    void setPreviousInteractor(@Nullable UUID previousCustomerUUID);

    class Impl implements Reputation {
        private final GossipContainer gossipManager;
        private long lastGossipTime;
        private long lastGossipDecay;
        private UUID previousInteractor;

        public Impl(){
            this.gossipManager = new GossipContainer();
        }

        @Override
        public GossipContainer getGossipManager() {
            return this.gossipManager;
        }

        @Override
        public long getLastGossipTime() {
            return this.lastGossipTime;
        }

        @Override
        public void setLastGossipTime(long lastGossipTime) {
            this.lastGossipTime = lastGossipTime;
        }

        @Override
        public long getLastGossipDecay() {
            return this.lastGossipDecay;
        }

        @Override
        public void setLastGossipDecay(long lastGossipDecay) {
            this.lastGossipDecay = lastGossipDecay;
        }

        @Nullable
        @Override
        public UUID getPreviousInteractor() {
            return this.previousInteractor;
        }

        @Override
        public void setPreviousInteractor(@Nullable UUID previousCustomerUUID) {
            this.previousInteractor = previousCustomerUUID;
        }
    }
}
