package com.infamous.sapience.mixin;

import com.infamous.sapience.util.ZoglinTasksHelper;
import com.mojang.datafixers.kinds.Const;
import com.mojang.datafixers.kinds.OptionalBox;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.StartAttacking;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.monster.Zoglin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

@Mixin(StartAttacking.class)
public class StartAttackingMixin {

    @ModifyVariable(method = "lambda$create$1", at = @At(value = "STORE"))
    private static Optional<? extends LivingEntity> modifyFoundTarget(Optional<? extends LivingEntity> original, Predicate<LivingEntity> predicate, Function<LivingEntity, Optional<? extends LivingEntity>> function, MemoryAccessor<Const.Mu<Unit>, LivingEntity> at, MemoryAccessor<OptionalBox.Mu, Long> crwts, ServerLevel level, Mob mob){
        if(mob instanceof Zoglin zoglin){
            return ZoglinTasksHelper.findNearestValidAttackTarget(zoglin);
        }
        return original;
    }
}
