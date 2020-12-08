package com.infamous.sapience.capability.reputation;

import net.minecraft.village.GossipManager;

import javax.annotation.Nullable;
import java.util.UUID;

public interface IReputation {

    GossipManager getGossipManager();

    long getLastGossipTime();

    void setLastGossipTime(long lastGossipTime);

    long getLastGossipDecay();

    void setLastGossipDecay(long lastGossipDecay);

    @Nullable
    UUID getPreviousInteractor();

    void setPreviousInteractor(@Nullable UUID previousCustomerUUID);
}
