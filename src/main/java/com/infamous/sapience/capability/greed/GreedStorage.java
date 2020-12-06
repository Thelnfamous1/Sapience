package com.infamous.sapience.capability.greed;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;

public class GreedStorage implements Capability.IStorage<IGreed> {

    @Nullable
    @Override
    public INBT writeNBT(Capability<IGreed> capability, IGreed instance, Direction side) {
        CompoundNBT tag = new CompoundNBT();
        tag.put("GreedInventory", instance.getGreedInventory().write());
        tag.putBoolean("SharingGold", instance.isSharingGold());
        return tag;
    }

    @Override
    public void readNBT(Capability<IGreed> capability, IGreed instance, Direction side, INBT nbt) {
        CompoundNBT tag = (CompoundNBT) nbt;
        instance.getGreedInventory().read(tag.getList("GreedInventory", 10));
        instance.setSharingGold(tag.getBoolean("SharingGold"));
    }
}