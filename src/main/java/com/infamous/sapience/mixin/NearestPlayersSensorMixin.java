package com.infamous.sapience.mixin;

import com.infamous.sapience.util.GeneralHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.sensor.NearestPlayersSensor;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Mixin(NearestPlayersSensor.class)
public class NearestPlayersSensorMixin {

    @Redirect(at = @At(value = "INVOKE",
            target = "Ljava/util/stream/Stream;findFirst()Ljava/util/Optional;"),
            method = "update")
    private Optional<PlayerEntity> checkTeam(Stream<PlayerEntity> stream, ServerWorld serverWorld, LivingEntity sensorMob){
        return stream
                .filter((player) -> GeneralHelper.isNotOnSameTeam(sensorMob, player))
                .findFirst();
    }
}
