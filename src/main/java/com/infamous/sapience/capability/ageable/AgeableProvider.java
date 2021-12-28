package com.infamous.sapience.capability.ageable;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AgeableProvider implements ICapabilitySerializable<CompoundTag> {

    public static final Capability<Ageable> AGEABLE_CAPABILITY =
            CapabilityManager.get(new CapabilityToken<>() {
            });

    private final LazyOptional<Ageable> instance = LazyOptional.of(Ageable.Impl::new);

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return cap == AGEABLE_CAPABILITY ? instance.cast() : LazyOptional.empty();    }

    @Override
    public CompoundTag serializeNBT() {
        Ageable instance = this.instance.orElseThrow(() -> new IllegalArgumentException("LazyOptional must not be empty!"));
        CompoundTag tag = new CompoundTag();
        tag.putInt("Age", instance.getGrowingAge());
        tag.putInt("ForcedAge", instance.getForcedAge());
        tag.putBoolean("WasBorn", instance.wasBorn());
        //tag.put("FoodInventory", instance.getFoodInventory().write());
        tag.putByte("FoodLevel", instance.getFoodLevel());
        //tag.putInt("InLove", instance.getInLove());
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        Ageable instance = this.instance.orElseThrow(() -> new IllegalArgumentException("LazyOptional must not be empty!"));
        instance.setGrowingAge(nbt.getInt("Age"));
        instance.setForcedAge(nbt.getInt("ForcedAge"));
        instance.setBorn(nbt.getBoolean("WasBorn"));
        //instance.getFoodInventory().read(tag.getList("FoodInventory", Tag.TAG_COMPOUND));
        instance.setFoodLevel(nbt.getByte("FoodLevel"));
        //instance.setInLove(nbt.getInt("InLove"));
    }
}