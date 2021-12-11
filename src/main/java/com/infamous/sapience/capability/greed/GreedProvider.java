package com.infamous.sapience.capability.greed;

import net.minecraft.nbt.Tag;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class GreedProvider implements ICapabilitySerializable<Tag> {

    @CapabilityInject(IGreed.class)
    public static final Capability<IGreed> GREED_CAPABILITY = null;

    private LazyOptional<IGreed> instance = LazyOptional.of(GREED_CAPABILITY::getDefaultInstance);

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return cap == GREED_CAPABILITY ? instance.cast() : LazyOptional.empty();    }

    @Override
    public Tag serializeNBT() {
        return GREED_CAPABILITY.getStorage().writeNBT(GREED_CAPABILITY, this.instance.orElseThrow(() -> new IllegalArgumentException("LazyOptional must not be empty!")), null);
    }

    @Override
    public void deserializeNBT(Tag nbt) {
        GREED_CAPABILITY.getStorage().readNBT(GREED_CAPABILITY, this.instance.orElseThrow(() -> new IllegalArgumentException("LazyOptional must not be empty!")), null, nbt);
    }
}