package com.infamous.sapience.capability.ageable;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;

public class AgeableStorage implements Capability.IStorage<IAgeable> {

    @Nullable
    @Override
    public INBT writeNBT(Capability<IAgeable> capability, IAgeable instance, Direction side) {
        CompoundNBT tag = new CompoundNBT();
        tag.putInt("Age", instance.getGrowingAge());
        tag.putInt("ForcedAge", instance.getForcedAge());
        tag.putBoolean("WasBorn", instance.wasBorn());
        //tag.put("FoodInventory", instance.getFoodInventory().write());
        tag.putByte("FoodLevel", instance.getFoodLevel());
        //tag.putInt("InLove", instance.getInLove());
        return tag;
    }

    @Override
    public void readNBT(Capability<IAgeable> capability, IAgeable instance, Direction side, INBT nbt) {
        CompoundNBT tag = (CompoundNBT) nbt;
        instance.setGrowingAge(tag.getInt("Age"));
        instance.setForcedAge(tag.getInt("ForcedAge"));
        instance.setBorn(tag.getBoolean("WasBorn"));
        //instance.getFoodInventory().read(tag.getList("FoodInventory", 10));
        instance.setFoodLevel(tag.getByte("FoodLevel"));
        //instance.setInLove(tag.getInt("InLove"));
    }
}