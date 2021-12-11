package com.infamous.sapience.capability.reputation;

import com.mojang.serialization.Dynamic;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;

public class ReputationStorage implements Capability.IStorage<IReputation> {

    @Nullable
    @Override
    public Tag writeNBT(Capability<IReputation> capability, IReputation instance, Direction side) {
        CompoundTag tag = new CompoundTag();

        tag.put("Gossips", instance.getGossipManager().store(NbtOps.INSTANCE).getValue());
        tag.putLong("LastGossipDecay", instance.getLastGossipDecay());
        if(instance.getPreviousInteractor() != null){
            tag.putUUID("PreviousInteractor", instance.getPreviousInteractor());
        }

        return tag;
    }

    @Override
    public void readNBT(Capability<IReputation> capability, IReputation instance, Direction side, Tag nbt) {
        CompoundTag tag = (CompoundTag) nbt;

        ListTag listnbt = tag.getList("Gossips", 10);
        instance.getGossipManager().update(new Dynamic<>(NbtOps.INSTANCE, listnbt));

        instance.setLastGossipDecay(tag.getLong("LastGossipDecay"));
        if(tag.hasUUID("PreviousInteractor")){
            instance.setPreviousInteractor(tag.getUUID("PreviousInteractor"));
        }
    }
}
