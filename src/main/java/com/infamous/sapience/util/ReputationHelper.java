package com.infamous.sapience.util;

import com.infamous.sapience.Sapience;
import com.infamous.sapience.SapienceConfig;
import com.infamous.sapience.capability.reputation.Reputation;
import com.infamous.sapience.capability.reputation.ReputationProvider;
import com.mojang.serialization.Dynamic;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ReputationEventHandler;
import net.minecraft.world.entity.ai.gossip.GossipContainer;
import net.minecraft.world.entity.ai.gossip.GossipType;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.village.ReputationEventType;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nullable;
import java.util.Optional;

public class ReputationHelper {

    private static final int NEUTRAL_REPUTATION = 0;

    @Nullable
    public static Reputation getReputationCapability(Entity entity)
    {
        LazyOptional<Reputation> lazyCap = entity.getCapability(ReputationProvider.REPUTATION_CAPABILITY);
        if (lazyCap.isPresent()) {
            return lazyCap.orElseThrow(() -> new IllegalStateException("Couldn't get the reputation capability from the Entity!"));
        }
        Sapience.LOGGER.error("Couldn't get the reputation capability from the Entity in ReputationHelper#getReputationCapability!");
        return null;
    }

    public static void updatePiglinReputation(LivingEntity host, ReputationEventType type, Entity target){
        Reputation reputation = getReputationCapability(host);
        if(reputation != null){
            GossipContainer gossipManager = reputation.getGossipManager();

            // Extremely major positive events - slaying the Wither
            if (type == PiglinReputationType.WITHER_KILLED) {
                gossipManager.add(target.getUUID(), GossipType.MAJOR_POSITIVE, SapienceConfig.COMMON.WITHER_KILLED_GOSSIP_VALUE.get());
                gossipManager.add(target.getUUID(), GossipType.MINOR_POSITIVE, SapienceConfig.COMMON.WITHER_KILLED_BONUS_GOSSIP_VALUE.get());
            }

            // Major positive events - slaying wither skeletons
            else if (type == PiglinReputationType.WITHER_SKELETON_KILLED) {
                gossipManager.add(target.getUUID(), GossipType.MAJOR_POSITIVE, SapienceConfig.COMMON.WITHER_SKELETON_KILLED_GOSSIP_VALUE.get());
                gossipManager.add(target.getUUID(), GossipType.MINOR_POSITIVE, SapienceConfig.COMMON.WITHER_SKELETON_KILLED_BONUS_GOSSIP_VALUE.get());
            }

            // Minor positive events - giving gifts, bartering
            else if(type == PiglinReputationType.GOLD_GIFT){
                gossipManager.add(target.getUUID(), GossipType.MINOR_POSITIVE, SapienceConfig.COMMON.GOLD_GIFT_GOSSIP_VALUE.get());
            }
            else if (type == PiglinReputationType.FOOD_GIFT) {
                gossipManager.add(target.getUUID(), GossipType.MINOR_POSITIVE, SapienceConfig.COMMON.FOOD_GIFT_GOSSIP_VALUE.get());
            }
            else if (type == PiglinReputationType.BARTER) {
                gossipManager.add(target.getUUID(), GossipType.TRADING, SapienceConfig.COMMON.BARTER_GOSSIP_VALUE.get());
            }

            // Minor negative events - hurting piglins, stealing gold
            else if (type == PiglinReputationType.GOLD_STOLEN) {
                gossipManager.add(target.getUUID(), GossipType.MINOR_NEGATIVE, SapienceConfig.COMMON.GOLD_STOLEN_GOSSIP_VALUE.get());
            }
            else if (type == PiglinReputationType.ADULT_PIGLIN_HURT) {
                gossipManager.add(target.getUUID(), GossipType.MINOR_NEGATIVE, SapienceConfig.COMMON.ADULT_PIGLIN_HURT_GOSSIP_VALUE.get());
            }
            else if (type == PiglinReputationType.BABY_PIGLIN_HURT) {
                gossipManager.add(target.getUUID(), GossipType.MINOR_NEGATIVE, SapienceConfig.COMMON.BABY_PIGLIN_HURT_GOSSIP_VALUE.get());
            }

            // Major negative events - killing piglins
            else if (type == PiglinReputationType.ADULT_PIGLIN_KILLED) {
                gossipManager.add(target.getUUID(), GossipType.MAJOR_NEGATIVE, SapienceConfig.COMMON.ADULT_PIGLIN_KILLED_GOSSIP_VALUE.get());
                gossipManager.add(target.getUUID(), GossipType.MINOR_NEGATIVE, SapienceConfig.COMMON.ADULT_PIGLIN_KILLED_BONUS_GOSSIP_VALUE.get());
            }
            else if (type == PiglinReputationType.BABY_PIGLIN_KILLED) {
                gossipManager.add(target.getUUID(), GossipType.MAJOR_NEGATIVE, SapienceConfig.COMMON.BABY_PIGLIN_KILLED_GOSSIP_VALUE.get());
                gossipManager.add(target.getUUID(), GossipType.MINOR_NEGATIVE, SapienceConfig.COMMON.BABY_PIGLIN_KILLED_BONUS_GOSSIP_VALUE.get());
            }

            // Extremely major negative events - killing piglin brutes
            else if (type == PiglinReputationType.BRUTE_KILLED) {
                gossipManager.add(target.getUUID(), GossipType.MAJOR_NEGATIVE, SapienceConfig.COMMON.BRUTE_KILLED_GOSSIP_VALUE.get());
                gossipManager.add(target.getUUID(), GossipType.MINOR_NEGATIVE, SapienceConfig.COMMON.BRUTE_KILLED_BONUS_GOSSIP_VALUE.get());
            }
        }
    }

    // used in ShareGoldTask#updateTask
    public static void spreadGossip(LivingEntity host, LivingEntity ally, long gameTime) {
        Reputation hostReputation = getReputationCapability(host);
        Reputation allyReputation = getReputationCapability(ally);

        if(hostReputation != null && allyReputation != null){
            if ((gameTime < hostReputation.getLastGossipTime() || gameTime >= hostReputation.getLastGossipTime() + 1200L)
                    && (gameTime < allyReputation.getLastGossipTime() || gameTime >= allyReputation.getLastGossipTime() + 1200L)) {
                hostReputation.getGossipManager().transferFrom(allyReputation.getGossipManager(), host.getRandom(), 10);
                hostReputation.setLastGossipTime(gameTime);
                allyReputation.setLastGossipTime(gameTime);
            }
        }
    }

    // used in CreateBabyTask#updateTask
    public static void spreadGossipDirect(LivingEntity host, LivingEntity ally) {
        Reputation hostReputation = getReputationCapability(host);
        Reputation allyReputation = getReputationCapability(ally);

        if(hostReputation != null && allyReputation != null){
            hostReputation.getGossipManager().transferFrom(allyReputation.getGossipManager(), host.getRandom(), 10);
        }
    }

    public static void makeWitnessesOfMurder(LivingEntity victim, Entity murderer, ReputationEventType killedReputationType){
        if (victim.level instanceof ServerLevel serverworld) {
            if(victim.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)){
                Optional<NearestVisibleLivingEntities> optional = victim.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);
                optional.ifPresent(nvle -> nvle
                        .findAll(ReputationHelper::hasModdedReputationHandling)
                        .forEach(le -> ReputationHelper.updatePiglinReputation(le, killedReputationType, murderer)));
            } else{ // substitute for mobs that don't have the NEAREST_VISIBLE_LIVING_ENTITIES module
                final double scale = 16.0D;
                AABB aabb = victim.getBoundingBox().inflate(scale);
                serverworld.getEntitiesOfClass(LivingEntity.class, aabb, (le) -> le != victim && le.isAlive() && hasModdedReputationHandling(le))
                        .forEach(le -> ReputationHelper.updatePiglinReputation(le, killedReputationType, murderer));
            }
        }
    }

    // called by the mob's tick method - use LivingUpdateEvent instead
    public static void updateGossip(Mob mobEntity) {
        Reputation reputation = getReputationCapability(mobEntity);
        if(reputation != null && mobEntity.level instanceof ServerLevel){
            long gameTime = mobEntity.level.getGameTime();
            if (reputation.getLastGossipDecay() == 0L) {
                reputation.setLastGossipDecay(gameTime);
            } else if (gameTime >= reputation.getLastGossipDecay() + 24000L) {
                reputation.getGossipManager().decay();
                reputation.setLastGossipDecay(gameTime);
            }
        }
    }

    // call this when the zombified version of the mob gets cured
    // current plan is no curing zombified piglins, but will keep this here just in case
    public static void setGossip(Mob mobEntity, Tag gossip) {
        Reputation reputation = getReputationCapability(mobEntity);
        if(reputation != null){
            reputation.getGossipManager().update(new Dynamic<>(NbtOps.INSTANCE, gossip));
        }
    }

    // call this whenever a reputation of a player must be considered
    public static int getEntityReputation(Entity reputationEntity, Entity entityToCheck) {
        Reputation reputation = getReputationCapability(reputationEntity);
        if(reputation != null){
            // A negative value means bad reputation, a positive value means good reputation
            /*
            if(entityToCheck instanceof PlayerEntity){
                Sapience.LOGGER.info("Reputation check for " + entityToCheck.toString() + ": " + checkedReputation);
            }

             */
            return reputation.getGossipManager().getReputation(entityToCheck.getUUID(), (gossipType) -> true);
        }
        else return NEUTRAL_REPUTATION; // Default value
    }

    @Nullable
    public static Entity getPreviousInteractor(Mob mobEntity){
        Reputation reputation = getReputationCapability(mobEntity);
        if(reputation != null && mobEntity.level instanceof ServerLevel){
            if(reputation.getPreviousInteractor() != null){
                return ((ServerLevel) mobEntity.level).getEntity(reputation.getPreviousInteractor());
            }
            else return null;
        }
        else return null;
    }

    public static void setPreviousInteractor(Mob mobEntity, @Nullable Entity interactor){
        Reputation reputation = getReputationCapability(mobEntity);
        if(reputation != null){
            if(interactor != null){
                reputation.setPreviousInteractor(interactor.getUUID());
            }
            else reputation.setPreviousInteractor(null);
        }
    }

    public static void updatePreviousInteractorReputation(Mob mobEntity, ReputationEventType reputationType) {
        Entity previousInteractor = getPreviousInteractor(mobEntity);
        if(previousInteractor != null && mobEntity.level instanceof ServerLevel serverWorld){
            updatePiglinReputation(mobEntity, reputationType, previousInteractor);
            setPreviousInteractor(mobEntity, null);
        }
    }

    public static boolean isAllowedToTouchGold(Player playerEntity, Piglin nearbyPiglin) {
        return getEntityReputation(nearbyPiglin, playerEntity) >= SapienceConfig.COMMON.ALLY_GOSSIP_REQUIREMENT.get()
                || GeneralHelper.isOnSameTeam(nearbyPiglin, playerEntity);
    }

    public static boolean hasAcceptableAttire(LivingEntity livingEntity, LivingEntity sensorEntity) {
        return (PiglinAi.isWearingGold(livingEntity) &&
                getEntityReputation(sensorEntity, livingEntity) > SapienceConfig.COMMON.ENEMY_GOSSIP_REQUIREMENT.get())
                || getEntityReputation(sensorEntity, livingEntity) >= SapienceConfig.COMMON.FRIENDLY_GOSSIP_REQUIREMENT.get();
    }

    public static boolean isAllowedToBarter(Piglin piglinEntity, LivingEntity interactorEntity) {
        return getEntityReputation(piglinEntity, interactorEntity) > SapienceConfig.COMMON.UNFRIENDLY_GOSSIP_REQUIREMENT.get();
    }

    public static boolean hasVanillaOrModdedReputationHandling(Entity target) {
        return target instanceof ReputationEventHandler || hasModdedReputationHandling(target);
    }

    private static boolean hasModdedReputationHandling(Entity target) {
        return target.getCapability(ReputationProvider.REPUTATION_CAPABILITY).isPresent();
    }
}
