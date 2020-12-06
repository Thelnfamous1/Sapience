package com.infamous.sapience.capability.ageable;

import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AgeableProvider implements ICapabilitySerializable<INBT> {

    @CapabilityInject(IAgeable.class)
    public static final Capability<IAgeable> AGEABLE_CAPABILITY = null;

    private LazyOptional<IAgeable> instance = LazyOptional.of(AGEABLE_CAPABILITY::getDefaultInstance);

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return cap == AGEABLE_CAPABILITY ? instance.cast() : LazyOptional.empty();    }

    @Override
    public INBT serializeNBT() {
        return AGEABLE_CAPABILITY.getStorage().writeNBT(AGEABLE_CAPABILITY, this.instance.orElseThrow(() -> new IllegalArgumentException("LazyOptional must not be empty!")), null);
    }

    @Override
    public void deserializeNBT(INBT nbt) {
        AGEABLE_CAPABILITY.getStorage().readNBT(AGEABLE_CAPABILITY, this.instance.orElseThrow(() -> new IllegalArgumentException("LazyOptional must not be empty!")), null, nbt);
    }
}