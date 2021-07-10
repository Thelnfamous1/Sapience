package com.infamous.sapience.util;

import com.infamous.sapience.Sapience;
import com.infamous.sapience.SapienceConfig;
import com.infamous.sapience.capability.reputation.IReputation;
import com.infamous.sapience.capability.reputation.ReputationProvider;
import com.mojang.serialization.Dynamic;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.merchant.IReputationTracking;
import net.minecraft.entity.merchant.IReputationType;
import net.minecraft.entity.monster.piglin.AbstractPiglinEntity;
import net.minecraft.entity.monster.piglin.PiglinEntity;
import net.minecraft.entity.monster.piglin.PiglinTasks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.village.GossipManager;
import net.minecraft.village.GossipType;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nullable;
import java.util.List;

public class ReputationHelper {

    private static final int ALLY_REPUTATION = SapienceConfig.COMMON.ALLY_GOSSIP_REQUIREMENT.get();
    private static final int FRIENDLY_REPUTATION = SapienceConfig.COMMON.FRIENDLY_GOSSIP_REQUIREMENT.get();
    private static final int NEUTRAL_REPUTATION = 0;
    private static final int UNFRIENDLY_REPUTATION = SapienceConfig.COMMON.UNFRIENDLY_GOSSIP_REQUIREMENT.get();
    private static final int ENEMY_REPUTATION = SapienceConfig.COMMON.ENEMY_GOSSIP_REQUIREMENT.get();


    @Nullable
    private static IReputation getReputationCapability(Entity entity)
    {
        LazyOptional<IReputation> lazyCap = entity.getCapability(ReputationProvider.REPUTATION_CAPABILITY);
        if (lazyCap.isPresent()) {
            return lazyCap.orElseThrow(() -> new IllegalStateException("Couldn't get the reputation capability from the Entity!"));
        }
        Sapience.LOGGER.error("Couldn't get the reputation capability from the Entity in ReputationHelper#getReputationCapability!");
        return null;
    }

    public static void updatePiglinReputation(AbstractPiglinEntity piglinEntity, IReputationType type, Entity target){
        IReputation reputation = getReputationCapability(piglinEntity);
        if(reputation != null){
            GossipManager gossipManager = reputation.getGossipManager();

            // Extremely major positive events - slaying the Wither
            if (type == PiglinReputationType.WITHER_KILLED) {
                gossipManager.add(target.getUniqueID(), GossipType.MAJOR_POSITIVE, SapienceConfig.COMMON.WITHER_KILLED_GOSSIP_VALUE.get());
                gossipManager.add(target.getUniqueID(), GossipType.MINOR_POSITIVE, SapienceConfig.COMMON.WITHER_KILLED_BONUS_GOSSIP_VALUE.get());
            }

            // Major positive events - slaying wither skeletons
            else if (type == PiglinReputationType.WITHER_SKELETON_KILLED) {
                gossipManager.add(target.getUniqueID(), GossipType.MAJOR_POSITIVE, SapienceConfig.COMMON.WITHER_SKELETON_KILLED_GOSSIP_VALUE.get());
                gossipManager.add(target.getUniqueID(), GossipType.MINOR_POSITIVE, SapienceConfig.COMMON.WITHER_SKELETON_KILLED_BONUS_GOSSIP_VALUE.get());
            }

            // Minor positive events - giving gifts, bartering
            else if(type == PiglinReputationType.GOLD_GIFT){
                gossipManager.add(target.getUniqueID(), GossipType.MINOR_POSITIVE, SapienceConfig.COMMON.GOLD_GIFT_GOSSIP_VALUE.get());
            }
            else if (type == PiglinReputationType.FOOD_GIFT) {
                gossipManager.add(target.getUniqueID(), GossipType.MINOR_POSITIVE, SapienceConfig.COMMON.FOOD_GIFT_GOSSIP_VALUE.get());
            }
            else if (type == PiglinReputationType.BARTER) {
                gossipManager.add(target.getUniqueID(), GossipType.TRADING, SapienceConfig.COMMON.BARTER_GOSSIP_VALUE.get());
            }

            // Minor negative events - hurting piglins, stealing gold
            else if (type == PiglinReputationType.GOLD_STOLEN) {
                gossipManager.add(target.getUniqueID(), GossipType.MINOR_NEGATIVE, SapienceConfig.COMMON.GOLD_STOLEN_GOSSIP_VALUE.get());
            }
            else if (type == PiglinReputationType.ADULT_PIGLIN_HURT) {
                gossipManager.add(target.getUniqueID(), GossipType.MINOR_NEGATIVE, SapienceConfig.COMMON.ADULT_PIGLIN_HURT_GOSSIP_VALUE.get());
            }
            else if (type == PiglinReputationType.BABY_PIGLIN_HURT) {
                gossipManager.add(target.getUniqueID(), GossipType.MINOR_NEGATIVE, SapienceConfig.COMMON.BABY_PIGLIN_HURT_GOSSIP_VALUE.get());
            }

            // Major negative events - killing piglins
            else if (type == PiglinReputationType.ADULT_PIGLIN_KILLED) {
                gossipManager.add(target.getUniqueID(), GossipType.MAJOR_NEGATIVE, SapienceConfig.COMMON.ADULT_PIGLIN_KILLED_GOSSIP_VALUE.get());
                gossipManager.add(target.getUniqueID(), GossipType.MINOR_NEGATIVE, SapienceConfig.COMMON.ADULT_PIGLIN_KILLED_BONUS_GOSSIP_VALUE.get());
            }
            else if (type == PiglinReputationType.BABY_PIGLIN_KILLED) {
                gossipManager.add(target.getUniqueID(), GossipType.MAJOR_NEGATIVE, SapienceConfig.COMMON.BABY_PIGLIN_KILLED_GOSSIP_VALUE.get());
                gossipManager.add(target.getUniqueID(), GossipType.MINOR_NEGATIVE, SapienceConfig.COMMON.BABY_PIGLIN_KILLED_BONUS_GOSSIP_VALUE.get());
            }

            // Extremely major negative events - killing piglin brutes
            else if (type == PiglinReputationType.BRUTE_KILLED) {
                gossipManager.add(target.getUniqueID(), GossipType.MAJOR_NEGATIVE, SapienceConfig.COMMON.BRUTE_KILLED_GOSSIP_VALUE.get());
                gossipManager.add(target.getUniqueID(), GossipType.MINOR_NEGATIVE, SapienceConfig.COMMON.BRUTE_KILLED_BONUS_GOSSIP_VALUE.get());
            }
        }
    }

    // used in ShareGoldTask#updateTask
    public static void spreadGossip(AbstractPiglinEntity host, LivingEntity ally, long gameTime) {
        IReputation hostReputation = getReputationCapability(host);
        IReputation allyReputation = getReputationCapability(ally);

        if(hostReputation != null && allyReputation != null){
            if ((gameTime < hostReputation.getLastGossipTime() || gameTime >= hostReputation.getLastGossipTime() + 1200L)
                    && (gameTime < allyReputation.getLastGossipTime() || gameTime >= allyReputation.getLastGossipTime() + 1200L)) {
                hostReputation.getGossipManager().transferFrom(allyReputation.getGossipManager(), host.getRNG(), 10);
                hostReputation.setLastGossipTime(gameTime);
                allyReputation.setLastGossipTime(gameTime);
            }
        }
    }

    public static void makeWitnessesOfMurder(LivingEntity murderedEntity, Entity murderer, IReputationType killedReputationType){
        if (murderedEntity.world instanceof ServerWorld) {
            AxisAlignedBB axisalignedbb = murderedEntity.getBoundingBox().grow(16.0D, 16.0D, 16.0D);
            List<LivingEntity> nearbyWitnesses = murderedEntity.world.getEntitiesWithinAABB(LivingEntity.class, axisalignedbb,
                    (nearbyEntity) -> nearbyEntity != murderedEntity && nearbyEntity.isAlive() && nearbyEntity.canEntityBeSeen(murderedEntity));

            ServerWorld serverworld = (ServerWorld)murderedEntity.world;
            nearbyWitnesses.stream().filter((gossipTarget) -> gossipTarget instanceof IReputationTracking).forEach((gossipTarget) -> {
                serverworld.updateReputation(killedReputationType, murderer, (IReputationTracking)gossipTarget);
            });
        }
    }

    // called by the mob's tick method - use LivingUpdateEvent instead
    public static void updateGossip(MobEntity mobEntity) {
        IReputation reputation = getReputationCapability(mobEntity);
        if(reputation != null && mobEntity.world instanceof ServerWorld){
            long gameTime = mobEntity.world.getGameTime();
            if (reputation.getLastGossipDecay() == 0L) {
                reputation.setLastGossipDecay(gameTime);
            } else if (gameTime >= reputation.getLastGossipDecay() + 24000L) {
                reputation.getGossipManager().tick();
                reputation.setLastGossipDecay(gameTime);
            }
        }
    }

    // call this when the zombified version of the mob gets cured
    // current plan is no curing zombified piglins, but will keep this here just in case
    public static void setGossip(MobEntity mobEntity, INBT gossip) {
        IReputation reputation = getReputationCapability(mobEntity);
        if(reputation != null){
            reputation.getGossipManager().read(new Dynamic<>(NBTDynamicOps.INSTANCE, gossip));
        }
    }

    // call this whenever a reputation of a player must be considered
    private static int getEntityReputation(LivingEntity reputationEntity, LivingEntity entityToCheck) {
        IReputation reputation = getReputationCapability(reputationEntity);
        if(reputation != null){
            // A negative value means bad reputation, a positive value means good reputation
            /*
            if(entityToCheck instanceof PlayerEntity){
                Sapience.LOGGER.info("Reputation check for " + entityToCheck.toString() + ": " + checkedReputation);
            }

             */
            return reputation.getGossipManager().getReputation(entityToCheck.getUniqueID(), (gossipType) -> true);
        }
        else return NEUTRAL_REPUTATION; // Default value
    }

    @Nullable
    public static Entity getPreviousInteractor(MobEntity mobEntity){
        IReputation reputation = getReputationCapability(mobEntity);
        if(reputation != null && mobEntity.world instanceof ServerWorld){
            if(reputation.getPreviousInteractor() != null){
                return ((ServerWorld) mobEntity.world).getEntityByUuid(reputation.getPreviousInteractor());
            }
            else return null;
        }
        else return null;
    }

    public static void setPreviousInteractor(MobEntity mobEntity, @Nullable Entity interactor){
        IReputation reputation = getReputationCapability(mobEntity);
        if(reputation != null){
            if(interactor != null){
                reputation.setPreviousInteractor(interactor.getUniqueID());
            }
            else reputation.setPreviousInteractor(null);
        }
    }

    public static void updatePreviousInteractorReputation(MobEntity mobEntity, IReputationType reputationType) {
        Entity previousInteractor = getPreviousInteractor(mobEntity);
        if(previousInteractor != null && mobEntity instanceof IReputationTracking && mobEntity.world instanceof ServerWorld){
            ServerWorld serverWorld = (ServerWorld) mobEntity.world;
            serverWorld.updateReputation(reputationType, previousInteractor, (IReputationTracking) mobEntity);
            setPreviousInteractor(mobEntity, null);
        }
    }

    public static boolean isAllowedToTouchGold(PlayerEntity playerEntity, PiglinEntity nearbyPiglin) {
        return getEntityReputation(nearbyPiglin, playerEntity) >= ALLY_REPUTATION
                || GeneralHelper.isOnSameTeam(nearbyPiglin, playerEntity);
    }

    public static boolean hasAcceptableAttire(LivingEntity livingEntity, LivingEntity sensorEntity) {
        return (PiglinTasks.func_234460_a_(livingEntity) &&
                getEntityReputation(sensorEntity, livingEntity) > ENEMY_REPUTATION)
                || getEntityReputation(sensorEntity, livingEntity) >= FRIENDLY_REPUTATION;
    }

    public static boolean isAllowedToBarter(PiglinEntity piglinEntity, LivingEntity interactorEntity) {
        return getEntityReputation(piglinEntity, interactorEntity) > UNFRIENDLY_REPUTATION;
    }
}
