package com.infamous.sapience.capability.reputation;

import com.infamous.sapience.capability.ageable.IAgeable;
import com.mojang.serialization.Dynamic;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ReputationProvider implements ICapabilitySerializable<CompoundTag> {

    public static final Capability<IReputation> REPUTATION_CAPABILITY =
            CapabilityManager.get(new CapabilityToken<>() {
            });

    private LazyOptional<IReputation> instance = LazyOptional.of(Reputation::new);

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return cap == REPUTATION_CAPABILITY ? instance.cast() : LazyOptional.empty();    }

    @Override
    public CompoundTag serializeNBT() {
        IReputation instance = this.instance.orElseThrow(() -> new IllegalArgumentException("LazyOptional must not be empty!"));
        CompoundTag tag = new CompoundTag();

        tag.put("Gossips", instance.getGossipManager().store(NbtOps.INSTANCE).getValue());
        tag.putLong("LastGossipDecay", instance.getLastGossipDecay());
        if(instance.getPreviousInteractor() != null){
            tag.putUUID("PreviousInteractor", instance.getPreviousInteractor());
        }

        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        IReputation instance = this.instance.orElseThrow(() -> new IllegalArgumentException("LazyOptional must not be empty!"));
        ListTag listnbt = nbt.getList("Gossips", Tag.TAG_COMPOUND);
        instance.getGossipManager().update(new Dynamic<>(NbtOps.INSTANCE, listnbt));

        instance.setLastGossipDecay(nbt.getLong("LastGossipDecay"));
        if(nbt.hasUUID("PreviousInteractor")){
            instance.setPreviousInteractor(nbt.getUUID("PreviousInteractor"));
        }
    }
}