package com.infamous.sapience.mixin;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Animal.class)
public interface AnimalAccessor {

    @Invoker
    void callUsePlayerItem(Player player, InteractionHand hand, ItemStack stack);
}
