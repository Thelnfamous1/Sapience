package com.infamous.sapience.capability.reputation;

import net.minecraft.world.entity.ai.gossip.GossipContainer;

import javax.annotation.Nullable;
import java.util.UUID;

public class Reputation implements IReputation{
    private GossipContainer gossipManager;
    private long lastGossipTime;
    private long lastGossipDecay;
    private UUID previousInteractor;

    public Reputation(){
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
