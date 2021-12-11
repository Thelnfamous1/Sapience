package com.infamous.sapience.capability.greed;

import com.infamous.sapience.capability.ageable.IAgeable;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class GreedProvider implements ICapabilitySerializable<CompoundTag> {

    public static final Capability<IGreed> GREED_CAPABILITY =
            CapabilityManager.get(new CapabilityToken<>() {
            });

    private LazyOptional<IGreed> instance = LazyOptional.of(Greed::new);

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return cap == GREED_CAPABILITY ? instance.cast() : LazyOptional.empty();    }

    @Override
    public CompoundTag serializeNBT() {
        IGreed instance = this.instance.orElseThrow(() -> new IllegalArgumentException("LazyOptional must not be empty!"));
        CompoundTag tag = new CompoundTag();
        tag.put("GreedInventory", instance.getGreedInventory().createTag());
        tag.putBoolean("SharingGold", instance.isSharingGold());
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        IGreed instance = this.instance.orElseThrow(() -> new IllegalArgumentException("LazyOptional must not be empty!"));
        instance.getGreedInventory().fromTag(nbt.getList("GreedInventory", Tag.TAG_COMPOUND));
        instance.setSharingGold(nbt.getBoolean("SharingGold"));
    }
}