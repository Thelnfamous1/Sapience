package com.infamous.sapience.util;

import com.infamous.sapience.capability.ageable.AgeableProvider;
import com.infamous.sapience.Sapience;
import com.infamous.sapience.capability.ageable.IAgeable;
import net.minecraft.entity.*;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nullable;

public class AgeableHelper {

    public static final int BIRTH_AGE = -24000;
    public static final int BREEDING_COOLDOWN = 6000;
    public static final int BREEDING_ID = 12;
    //public static final int FINISHED_BREEDING_ID = 13;
    public static final int GROWING_ID = 14;
    //public static final int FINISHED_GROWING_ID = 15;
    public static final int ANGER_ID = 16;
    public static final int DECLINE_ID = 6;
    public static final int ACCEPT_ID = 8;

    @Nullable
    public static IAgeable getAgeableCapability(Entity entity)
    {
        LazyOptional<IAgeable> lazyCap = entity.getCapability(AgeableProvider.AGEABLE_CAPABILITY);
        if (lazyCap.isPresent()) {
            return lazyCap.orElseThrow(() -> new IllegalStateException("Couldn't get the ageable capability from the Entity!"));
        }
        Sapience.LOGGER.error("Couldn't get the ageable capability from the Entity in AgeableHelper#getAgeableCapability!");
        return null;
    }

    public static boolean canAddItemStackToFoodInventory(MobEntity mobEntity, ItemStack itemStack) {
        IAgeable ageable = getAgeableCapability(mobEntity);
        if(ageable != null){
            return ageable.getFoodInventory().func_233541_b_(itemStack);
        }
        Sapience.LOGGER.error("Couldn't get the ageable capability from the MobEntity in AgeableHelper#canAddItemStackToFoodInventory!");
        return false;
    }

    public static void depleteParentsFoodReserves(MobEntity parent, MobEntity partner) {
        depleteFoodReserves(parent);
        depleteFoodReserves(partner);
    }

    public static void depleteFoodReserves(MobEntity parent) {
        IAgeable parentAging = AgeableHelper.getAgeableCapability(parent);
        if (parentAging != null) {
            parentAging.depleteFoodReserves();
        }
    }

    public static boolean canBreed(MobEntity parent){
        IAgeable parentAging = AgeableHelper.getAgeableCapability(parent);
        return parentAging != null && parentAging.canBreed();
    }

    public static boolean canPartnersBreed(MobEntity parent, MobEntity partner) {
        return canBreed(parent) && canBreed(partner);
    }

    public static void setParentsOnBreedCooldown(MobEntity parent, MobEntity partner) {
        setParentOnBreedCooldown(parent);
        setParentOnBreedCooldown(partner);
    }

    public static void setParentOnBreedCooldown(MobEntity parent) {
        IAgeable parentAging = AgeableHelper.getAgeableCapability(parent);
        if (parentAging != null) {
            parentAging.setGrowingAge(BREEDING_COOLDOWN);
        }
    }

    public static void updateGrowingAge(MobEntity mobEntity) {
        IAgeable ageable = getAgeableCapability(mobEntity);
        if(ageable != null){
            int i = ageable.getGrowingAge();
            if (i < 0) {
                ++i;
                ageable.setGrowingAge(i);
            } else if (i > 0) {
                --i;
                ageable.setGrowingAge(i);
            } else{
                // Manually setting the piglin to an adult
                // Normally, isChild would automatically return false if the growing age was not less than 0
                // But since Piglins don't extend from AgeableEntity, we have to do it manually here
                if(mobEntity.isChild()){
                    mobEntity.setChild(false);
                }
            }
        }
    }

    public static void updateForcedAge(MobEntity mobEntity) {
        IAgeable ageable = getAgeableCapability(mobEntity);
        if(ageable != null){
            if (ageable.getForcedAgeTimer() > 0) {
                if (ageable.getForcedAgeTimer() % 4 == 0) {
                    mobEntity.world.setEntityState(mobEntity, (byte) AgeableHelper.GROWING_ID);
                }
                ageable.setForcedAgeTimer(ageable.getForcedAgeTimer() - 1);
            }
        }
    }

    public static void initializeChild(MobEntity child) {
        IAgeable childAgeable = AgeableHelper.getAgeableCapability(child);
        if(childAgeable != null && !childAgeable.wasBorn()){
            childAgeable.setGrowingAge(BIRTH_AGE);
            childAgeable.setBorn(true);
        }
    }

    @Nullable
    public static MobEntity createChild(ServerWorld serverWorld, MobEntity parent, MobEntity partner){
        EntityType parentType = parent.getType();
        EntityType partnerType = partner.getType();
        if(parentType == partnerType){
            MobEntity child = (MobEntity) parentType.create(serverWorld);
            if(child != null){
                child.setChild(true);
                AgeableHelper.initializeChild(child);
                child.onInitialSpawn(serverWorld, serverWorld.getDifficultyForLocation(child.getPosition()), SpawnReason.BREEDING, (ILivingEntityData)null, (CompoundNBT)null);
                return child;
            }
        }
        return (MobEntity)null;
    }

    public static void updateSelfAge(MobEntity mobEntity) {
        IAgeable ageable = getAgeableCapability(mobEntity);
        if (ageable != null && ageable.canSelfAge()) {
            ageable.depleteFoodReserves();
            //mobEntity.world.setEntityState(mobEntity, (byte) CapabilityHelper.FINISHED_GROWING_ID);
            ageable.ageUp((int) ((float) (-ageable.getGrowingAge() / 20) * 0.1F), true);
        }
    }

    public static void dropAllFoodItems(MobEntity mobEntity) {
        IAgeable ageable = getAgeableCapability(mobEntity);
        if (ageable != null) {
            Inventory foodInventory = ageable.getFoodInventory();
            foodInventory.func_233543_f_().forEach(mobEntity::entityDropItem);
        }
    }
}
