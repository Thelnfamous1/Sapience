package com.infamous.sapience.util;

import com.infamous.sapience.Sapience;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.monster.Zoglin;
import net.minecraftforge.common.Tags;

import java.util.Optional;

public class ZoglinTasksHelper {

    public static final TagKey<EntityType<?>> ZOGLINS_IGNORE = GeneralHelper.createEntityTag(new ResourceLocation(Sapience.MODID, "zoglins_ignore"));

    public static Optional<? extends LivingEntity> findNearestValidAttackTarget(Zoglin zoglin) {
        return zoglin.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).orElse(NearestVisibleLivingEntities.empty()).findClosest(le -> isTargetable(zoglin, le));
    }

    public static boolean isTargetable(Zoglin zoglin, LivingEntity target) {
        EntityType<?> entitytype = target.getType();
        return !entitytype.m_204039_(ZOGLINS_IGNORE) && Sensor.isEntityAttackable(zoglin, target);
    }

    public static void setAttackTarget(Zoglin zoglin, LivingEntity target) {
        zoglin.getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, target);
        zoglin.getBrain().eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
    }
}
