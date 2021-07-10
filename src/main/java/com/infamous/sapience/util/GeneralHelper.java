package com.infamous.sapience.util;

import net.minecraft.entity.LivingEntity;
import net.minecraft.particles.IParticleData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class GeneralHelper {
    public static final int ANGER_ID = 16;
    public static final int DECLINE_ID = 6;
    public static final int ACCEPT_ID = 8;

    @OnlyIn(Dist.CLIENT)
    public static void spawnParticles(LivingEntity livingEntity, IParticleData particleData) {
        for(int i = 0; i < 5; ++i) {
            double randomXSpeed = livingEntity.getRNG().nextGaussian() * 0.02D;
            double randomYSpeed = livingEntity.getRNG().nextGaussian() * 0.02D;
            double randomZSpeed = livingEntity.getRNG().nextGaussian() * 0.02D;
            livingEntity.world.addParticle(particleData, livingEntity.getPosXRandom(1.0D), livingEntity.getPosYRandom() + 1.0D, livingEntity.getPosZRandom(1.0D), randomXSpeed, randomYSpeed, randomZSpeed);
        }
    }

    public static boolean isNotOnSameTeam(LivingEntity entityIn, LivingEntity livingentity) {
        return !isOnSameTeam(entityIn, livingentity);
    }

    public static boolean isOnSameTeam(LivingEntity entityIn, LivingEntity livingentity) {
        return entityIn.isOnSameTeam(livingentity);
    }
}
