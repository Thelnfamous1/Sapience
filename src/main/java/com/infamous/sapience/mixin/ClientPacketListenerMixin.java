package com.infamous.sapience.mixin;

import com.infamous.sapience.util.AgeableHelper;
import com.infamous.sapience.util.GeneralHelper;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.piglin.Piglin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {

    @Shadow private ClientLevel level;

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;handleEntityEvent(B)V"), method = "handleEntityEvent")
    private void handleHandleEntityEvent(ClientboundEntityEventPacket packet, CallbackInfo ci){
        Entity entity = packet.getEntity(this.level);
        byte eventId = packet.getEventId();
        if(entity instanceof Piglin piglin){
            if(eventId == AgeableHelper.BREEDING_ID){
                GeneralHelper.spawnParticles(piglin, ParticleTypes.HEART);
            }
            else if(eventId == AgeableHelper.GROWING_ID){
                GeneralHelper.spawnParticles(piglin, ParticleTypes.COMPOSTER);
            }
            else if(eventId == GeneralHelper.ANGER_ID){
                GeneralHelper.spawnParticles(piglin, ParticleTypes.ANGRY_VILLAGER);
            }
            else if(eventId == GeneralHelper.DECLINE_ID){
                GeneralHelper.spawnParticles(piglin, ParticleTypes.SMOKE);
            }
            else if(eventId == GeneralHelper.ACCEPT_ID){
                GeneralHelper.spawnParticles(piglin, ParticleTypes.HAPPY_VILLAGER);
            }
        }
    }
}
