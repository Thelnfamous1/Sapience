package com.infamous.sapience.util;

import com.infamous.sapience.mixin.AnimalAccessor;
import com.infamous.sapience.mixin.MobAccessor;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.Registries;
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

public class GeneralHelper {
    public static final int ANGER_ID = 16;
    public static final int DECLINE_ID = 6;
    public static final int ACCEPT_ID = 8;

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
        return entity.getType().is(PiglinTasksHelper.PIGLINS_HUNT) ? EntityType.HOGLIN : entity.getType();
    }

    public static EntityType<?> maybeSpoofPiglin(Entity entity) {
        return entity instanceof Piglin ? EntityType.PIGLIN : entity.getType();
    }

    public static InteractionResult handleGiveAnimalFood(Animal animal, Player player, InteractionHand hand){
        ItemStack stack = player.getItemInHand(hand);
        int age = animal.getAge();
        if (!animal.level.isClientSide && age == 0 && animal.canFallInLove()) {
            ((AnimalAccessor)animal).callUsePlayerItem(player, hand, stack);
            animal.setInLove(player);
            return InteractionResult.SUCCESS;
        }

        if (animal.isBaby()) {
            ((AnimalAccessor)animal).callUsePlayerItem(player, hand, stack);
            animal.ageUp(Animal.getSpeedUpSecondsWhenFeeding(-age), true);
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
        return TagKey.create(Registries.ENTITY_TYPE, location);
    }

    public static boolean hasAnyOf(Container container, TagKey<Item> tagKey) {
        for(int i = 0; i < container.getContainerSize(); ++i) {
            ItemStack itemstack = container.getItem(i);
            if (itemstack.is(tagKey) && itemstack.getCount() > 0) {
                return true;
            }
        }

        return false;
    }

    public static <M extends Mob> InteractionResult interactOn(Player player, M target, InteractionHand hand, MobInteraction<M> mobInteraction){
        ItemStack itemInHand = player.getItemInHand(hand);
        ItemStack copy = itemInHand.copy();
        InteractionResult interactionresult = interact(target, player, hand, mobInteraction);
        if (interactionresult.consumesAction()) {
            if (player.getAbilities().instabuild && itemInHand == player.getItemInHand(hand) && itemInHand.getCount() < copy.getCount()) {
                itemInHand.setCount(copy.getCount());
            }

            if (!player.getAbilities().instabuild && itemInHand.isEmpty()) {
                net.minecraftforge.event.ForgeEventFactory.onPlayerDestroyItem(player, copy, hand);
            }
            return interactionresult;
        } else {
            if (!itemInHand.isEmpty()) {
                if (player.getAbilities().instabuild) {
                    itemInHand = copy;
                }

                InteractionResult interactionResult = itemInHand.interactLivingEntity(player, target, hand);
                if (interactionResult.consumesAction()) {
                    if (itemInHand.isEmpty() && !player.getAbilities().instabuild) {
                        net.minecraftforge.event.ForgeEventFactory.onPlayerDestroyItem(player, copy, hand);
                        player.setItemInHand(hand, ItemStack.EMPTY);
                    }

                    return interactionResult;
                }
            }

            return InteractionResult.PASS;
        }
    }

    public static <M extends Mob> InteractionResult interact(M mob, Player player, InteractionHand hand, MobInteraction<M> mobInteraction) {
        if (!mob.isAlive()) {
            return InteractionResult.PASS;
        } else if (mob.getLeashHolder() == player) {
            mob.dropLeash(true, !player.getAbilities().instabuild);
            return InteractionResult.sidedSuccess(mob.level.isClientSide);
        } else {
            InteractionResult interactionResult = ((MobAccessor)mob).callCheckAndHandleImportantInteractions(player, hand);
            if (interactionResult.consumesAction()) {
                return interactionResult;
            } else {
                interactionResult = mobInteraction.apply(mob, player, hand);
                if (interactionResult.consumesAction()) {
                    mob.gameEvent(GameEvent.ENTITY_INTERACT);
                    return interactionResult;
                } else {
                    return InteractionResult.PASS;
                }
            }
        }
    }
}
