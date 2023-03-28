package com.infamous.sapience.util;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;

@FunctionalInterface
public interface MobInteraction<M extends Mob> {

    static <M extends Mob> InteractionResult pass(M ignoredMob, Player ignoredPlayer, InteractionHand ignoredHand){
        return InteractionResult.PASS;
    }

    InteractionResult apply(M mob, Player player, InteractionHand hand);
}
