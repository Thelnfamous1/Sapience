package com.infamous.sapience.capability.reputation;

import net.minecraft.world.entity.ai.gossip.GossipContainer;

import javax.annotation.Nullable;
import java.util.UUID;

public interface IReputation {

    GossipContainer getGossipManager();

    long getLastGossipTime();

    void setLastGossipTime(long lastGossipTime);

    long getLastGossipDecay();

    void setLastGossipDecay(long lastGossipDecay);

    @Nullable
    UUID getPreviousInteractor();

    void setPreviousInteractor(@Nullable UUID previousCustomerUUID);
}
