package com.infamous.sapience.capability.emotive;

import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EmotiveProvider implements ICapabilityProvider {

    public static final Capability<Emotive> EMOTIVE_CAPABILITY =
            CapabilityManager.get(new CapabilityToken<>() {
            });

    private final LazyOptional<Emotive> instance = LazyOptional.of(Emotive.Impl::new);

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return cap == EMOTIVE_CAPABILITY ? instance.cast() : LazyOptional.empty();    }
}