package com.infamous.sapience.util;

import com.infamous.sapience.Sapience;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.event.ForgeEventFactory;

import java.util.Optional;
import java.util.Set;

public class GeneralHelper {
    public static final int ANGER_ID = 16;
    public static final int DECLINE_ID = 6;
    public static final int ACCEPT_ID = 8;

    public static final TagKey<EntityType<?>> BOSSES = createEntityTag(new ResourceLocation(Sapience.MODID, "bosses"));

    public static void spawnParticles(LivingEntity livingEntity, ParticleOptions particleData) {
        for(int i = 0; i < 5; ++i) {
            double randomXSpeed = livingEntity.getRandom().nextGaussian() * 0.02D;
            double randomYSpeed = livingEntity.getRandom().nextGaussian() * 0.02D;
            double randomZSpeed = livingEntity.getRandom().nextGaussian() * 0.02D;
            livingEntity.level.addParticle(particleData, livingEntity.getRandomX(1.0D), livingEntity.getRandomY() + 1.0D, livingEntity.getRandomZ(1.0D), randomXSpeed, randomYSpeed, randomZSpeed);
        }
    }

    public static boolean isNotOnSameTeam(LivingEntity entityIn, LivingEntity livingentity) {
        return !isOnSameTeam(entityIn, livingentity);
    }

    public static boolean isOnSameTeam(LivingEntity entityIn, LivingEntity livingentity) {
        return entityIn.isAlliedTo(livingentity);
    }

    public static EntityType<?> maybeSpoofHoglin(Entity entity) {
        return entity instanceof Hoglin ? EntityType.HOGLIN : entity.getType();
    }

    public static EntityType<?> maybeSpoofHoglinOrPiglin(Entity entity) {
        return entity instanceof Hoglin ? EntityType.HOGLIN : entity instanceof Piglin ? EntityType.PIGLIN : entity.getType();
    }

    public static EntityType<?> maybeSpoofPiglinsHunt(Entity entity) {
        return entity.getType().m_204039_(PiglinTasksHelper.PIGLINS_HUNT) ? EntityType.HOGLIN : entity.getType();
    }

    public static EntityType<?> maybeSpoofPiglin(Entity entity) {
        return entity instanceof Piglin ? EntityType.PIGLIN : entity.getType();
    }

    public static InteractionResult handleGiveAnimalFood(Animal animal, Player player, InteractionHand hand){
        ItemStack stack = player.getItemInHand(hand);
        int age = animal.getAge();
        if (!animal.level.isClientSide && age == 0 && animal.canFallInLove()) {
            ReflectionHelper.callUsePlayerItem(animal, player, hand, stack);
            animal.setInLove(player);
            animal.gameEvent(GameEvent.MOB_INTERACT, animal.eyeBlockPosition());
            return InteractionResult.SUCCESS;
        }

        if (animal.isBaby()) {
            ReflectionHelper.callUsePlayerItem(animal, player, hand, stack);
            animal.ageUp((int)((float)(-age / 20) * 0.1F), true);
            animal.gameEvent(GameEvent.MOB_INTERACT, animal.eyeBlockPosition());
            return InteractionResult.sidedSuccess(animal.level.isClientSide);
        }

        if (animal.level.isClientSide) {
            return InteractionResult.CONSUME;
        }

        return InteractionResult.PASS;
    }

    public static Optional<? extends Animal> findValidBreedPartner(Animal parent, EntityType<? extends Animal> partnerType) {
        return parent.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).flatMap(memory -> memory.findClosest((le) -> {
            if (le.getType() == partnerType && le instanceof Animal potentialPartner) {
                return parent.canMate(potentialPartner);
            }
            return false;
        })).map(Animal.class::cast);
    }

    public static Optional<Animal> getBreedTarget(Animal animal) {
        return animal.getBrain().getMemory(MemoryModuleType.BREED_TARGET)
                .filter(Animal.class::isInstance)
                .map(Animal.class::cast);
    }

    public static Optional<LivingEntity> getAttackTarget(LivingEntity attacker) {
        return attacker.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET);
    }

    public static void customLooting(Mob mob) {
        if (!mob.level.isClientSide && mob.canPickUpLoot() && mob.isAlive() && !ReflectionHelper.getDead(mob) && ForgeEventFactory.getMobGriefingEvent(mob.level, mob)) {
            for(ItemEntity itemEntity : mob.level.getEntitiesOfClass(ItemEntity.class, mob.getBoundingBox().inflate(1.0D, 0.0D, 1.0D))) {
                ItemStack stack = itemEntity.getItem();
                if (!itemEntity.isRemoved()
                        && !stack.isEmpty()
                        && !itemEntity.hasPickUpDelay()
                        && customWantsToPickUp(mob, stack)) {
                    customPickUpItem(mob, itemEntity);
                }
            }
        }
    }

    private static void customPickUpItem(Mob mob, ItemEntity itemEntity) {
        if(mob instanceof Hoglin hoglin){
            mob.onItemPickup(itemEntity);
            HoglinTasksHelper.pickUpHoglinItem(hoglin, itemEntity);
        } else if(mob instanceof Piglin piglin){
            mob.onItemPickup(itemEntity);
            PiglinTasksHelper.pickUpPiglinItem(piglin, itemEntity);
            if(itemEntity.getThrower() != null && mob.level instanceof ServerLevel){
                Entity throwerEntity = ((ServerLevel) mob.level).getEntity(itemEntity.getThrower());
                ReputationHelper.setPreviousInteractor(piglin, throwerEntity);
            }
        }
    }

    public static boolean customWantsToPickUp(Mob mob, ItemStack stack) {
        return mob instanceof Hoglin hoglin ?
                HoglinTasksHelper.wantsToPickUp(hoglin, stack) :
                mob instanceof Piglin piglin && PiglinTasksHelper.wantsToPickUp(piglin, stack);
    }

    public static TagKey<EntityType<?>> createEntityTag(ResourceLocation location) {
        return TagKey.m_203882_(Registry.ENTITY_TYPE_REGISTRY, location);
    }

    public static boolean hasAnyOf(Container container, TagKey<Item> tagKey) {
        for(int i = 0; i < container.getContainerSize(); ++i) {
            ItemStack itemstack = container.getItem(i);
            if (itemstack.m_204117_(tagKey) && itemstack.getCount() > 0) {
                return true;
            }
        }

        return false;
    }
}
