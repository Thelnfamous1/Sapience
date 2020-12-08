package com.infamous.sapience.capability.reputation;

import com.mojang.serialization.Dynamic;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;

public class ReputationStorage implements Capability.IStorage<IReputation> {

    @Nullable
    @Override
    public INBT writeNBT(Capability<IReputation> capability, IReputation instance, Direction side) {
        CompoundNBT tag = new CompoundNBT();

        tag.put("Gossips", instance.getGossipManager().write(NBTDynamicOps.INSTANCE).getValue());
        tag.putLong("LastGossipDecay", instance.getLastGossipDecay());
        if(instance.getPreviousInteractor() != null){
            tag.putUniqueId("PreviousInteractor", instance.getPreviousInteractor());
        }

        return tag;
    }

    @Override
    public void readNBT(Capability<IReputation> capability, IReputation instance, Direction side, INBT nbt) {
        CompoundNBT tag = (CompoundNBT) nbt;

        ListNBT listnbt = tag.getList("Gossips", 10);
        instance.getGossipManager().read(new Dynamic<>(NBTDynamicOps.INSTANCE, listnbt));

        instance.setLastGossipDecay(tag.getLong("LastGossipDecay"));
        if(tag.hasUniqueId("PreviousInteractor")){
            instance.setPreviousInteractor(tag.getUniqueId("PreviousInteractor"));
        }
    }
}
