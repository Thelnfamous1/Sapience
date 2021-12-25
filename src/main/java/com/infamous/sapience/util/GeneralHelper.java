package com.infamous.sapience.util;

import com.infamous.sapience.Sapience;
import com.infamous.sapience.mixin.AnimalMakeLoveAccessor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.entity.ai.behavior.AnimalMakeLove;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.StartAdmiringItemIfSeen;
import net.minecraft.world.entity.monster.piglin.StopHoldingItemIfNoLongerAdmiring;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.Tags;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public class GeneralHelper {
    public static final int ANGER_ID = 16;
    public static final int DECLINE_ID = 6;
    public static final int ACCEPT_ID = 8;

    public static final Tags.IOptionalNamedTag<EntityType<?>> BOSSES = EntityTypeTags.createOptional(new ResourceLocation(Sapience.MODID, "bosses"));

    @OnlyIn(Dist.CLIENT)
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
}
