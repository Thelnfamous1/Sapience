package com.infamous.sapience.capability.reputation;

import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ReputationProvider implements ICapabilitySerializable<INBT> {

    @CapabilityInject(IReputation.class)
    public static final Capability<IReputation> REPUTATION_CAPABILITY = null;

    private LazyOptional<IReputation> instance = LazyOptional.of(REPUTATION_CAPABILITY::getDefaultInstance);

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return cap == REPUTATION_CAPABILITY ? instance.cast() : LazyOptional.empty();    }

    @Override
    public INBT serializeNBT() {
        return REPUTATION_CAPABILITY.getStorage().writeNBT(REPUTATION_CAPABILITY, this.instance.orElseThrow(() -> new IllegalArgumentException("LazyOptional must not be empty!")), null);
    }

    @Override
    public void deserializeNBT(INBT nbt) {
        REPUTATION_CAPABILITY.getStorage().readNBT(REPUTATION_CAPABILITY, this.instance.orElseThrow(() -> new IllegalArgumentException("LazyOptional must not be empty!")), null, nbt);
    }
}
