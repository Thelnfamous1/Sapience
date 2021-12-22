package com.infamous.sapience.util;

import com.infamous.sapience.Sapience;
import com.infamous.sapience.capability.ageable.AgeableProvider;
import com.infamous.sapience.capability.ageable.IAgeable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nullable;

public class AgeableHelper {

    private static final int BIRTH_AGE = -24000;
    private static final int BREEDING_COOLDOWN = 6000;
    public static final int BREEDING_ID = 12;
    //public static final int FINISHED_BREEDING_ID = 13;
    public static final int GROWING_ID = 14;
    //public static final int FINISHED_GROWING_ID = 15;

    @Nullable
    public static IAgeable getAgeableCapability(Entity entity)
    {
        LazyOptional<IAgeable> lazyCap = entity.getCapability(AgeableProvider.AGEABLE_CAPABILITY);
        if (lazyCap.isPresent()) {
            return lazyCap.orElseThrow(() -> new IllegalStateException("Couldn't get the ageable capability from the Entity!"));
        }
        Sapience.LOGGER.error("Couldn't get the ageable capability from " + entity.toString() + "in AgeableHelper#getAgeableCapability!");
        return null;
    }

    public static void depleteParentsFoodValue(Mob parent, Mob partner) {
        depleteFoodValue(parent);
        depleteFoodValue(partner);
    }

    private static void depleteFoodValue(Mob parent) {
        IAgeable parentAging = AgeableHelper.getAgeableCapability(parent);
        if (parentAging != null) {
            parentAging.depleteFoodValue();
        }
    }

    public static boolean canBreed(Mob parent){
        IAgeable parentAging = AgeableHelper.getAgeableCapability(parent);
        return parentAging != null && parentAging.canBreed();
    }

    public static boolean canPartnersBreed(Mob parent, Mob partner) {
        return canBreed(parent) && canBreed(partner);
    }

    public static void setParentsOnBreedCooldown(Mob parent, Mob partner) {
        setParentOnBreedCooldown(parent);
        setParentOnBreedCooldown(partner);
    }

    private static void setParentOnBreedCooldown(Mob parent) {
        IAgeable parentAging = AgeableHelper.getAgeableCapability(parent);
        if (parentAging != null) {
            parentAging.setGrowingAge(BREEDING_COOLDOWN);
        }
    }

    public static void increaseFoodLevel(Mob mobEntity, int foodValueIn) {
        IAgeable ageable = AgeableHelper.getAgeableCapability(mobEntity);
        if (ageable != null) {
            ageable.increaseFoodLevel(foodValueIn);
        }
    }

    public static void updateGrowingAge(Mob mobEntity) {
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
                if(mobEntity.isBaby()){
                    mobEntity.setBaby(false);
                }
            }
        }
    }

    public static void updateForcedAge(Mob mobEntity) {
        IAgeable ageable = getAgeableCapability(mobEntity);
        if(ageable != null){
            if (ageable.getForcedAgeTimer() > 0) {
                if (ageable.getForcedAgeTimer() % 4 == 0) {
                    mobEntity.level.broadcastEntityEvent(mobEntity, (byte) AgeableHelper.GROWING_ID);
                }
                ageable.setForcedAgeTimer(ageable.getForcedAgeTimer() - 1);
            }
        }
    }

    public static void initializeChild(Mob child) {
        //Sapience.LOGGER.info("Initializing child entity: " + child.toString());
        IAgeable childAgeable = AgeableHelper.getAgeableCapability(child);
        if(childAgeable != null && !childAgeable.wasBorn()){
            childAgeable.setGrowingAge(BIRTH_AGE);
            childAgeable.setBorn(true);
        }
    }

    @Nullable
    public static Mob createChild(ServerLevel serverWorld, Mob parent, Mob partner){
        EntityType<?> parentType = parent.getType();
        EntityType<?> partnerType = partner.getType();
        if(parentType == partnerType){
            Mob child = (Mob) parentType.create(serverWorld);
            if(child != null){
                child.setBaby(true);
                AgeableHelper.initializeChild(child);
                child.finalizeSpawn(serverWorld, serverWorld.getCurrentDifficultyAt(child.blockPosition()), MobSpawnType.BREEDING, (SpawnGroupData)null, (CompoundTag)null);
                return child;
            }
        }
        return (Mob)null;
    }

    public static void updateSelfAge(Mob mobEntity) {
        IAgeable ageable = getAgeableCapability(mobEntity);
        if (ageable != null && ageable.canSelfAge()) {
            ageable.depleteFoodValue();
            //mobEntity.world.setEntityState(mobEntity, (byte) CapabilityHelper.FINISHED_GROWING_ID);
            ageable.ageUp((int) ((float) (-ageable.getGrowingAge() / 20) * 0.1F), true);
        }
    }
}
