package com.infamous.sapience.mixin;

import com.infamous.sapience.util.GeneralHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.sensing.PlayerSensor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Mixin(PlayerSensor.class)
public class NearestPlayersSensorMixin {

    @Redirect(at = @At(value = "INVOKE",
            target = "Ljava/util/stream/Stream;findFirst()Ljava/util/Optional;"),
            method = "doTick")
    private Optional<Player> checkTeam(Stream<Player> stream, ServerLevel serverWorld, LivingEntity sensorMob){
        return stream
                .filter((player) -> GeneralHelper.isNotOnSameTeam(sensorMob, player))
                .findFirst();
    }
}
