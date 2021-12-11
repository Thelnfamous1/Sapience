package com.infamous.sapience.capability.greed;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;

public class GreedStorage implements Capability.IStorage<IGreed> {

    @Nullable
    @Override
    public Tag writeNBT(Capability<IGreed> capability, IGreed instance, Direction side) {
        CompoundTag tag = new CompoundTag();
        tag.put("GreedInventory", instance.getGreedInventory().createTag());
        tag.putBoolean("SharingGold", instance.isSharingGold());
        return tag;
    }

    @Override
    public void readNBT(Capability<IGreed> capability, IGreed instance, Direction side, Tag nbt) {
        CompoundTag tag = (CompoundTag) nbt;
        instance.getGreedInventory().fromTag(tag.getList("GreedInventory", 10));
        instance.setSharingGold(tag.getBoolean("SharingGold"));
    }
}