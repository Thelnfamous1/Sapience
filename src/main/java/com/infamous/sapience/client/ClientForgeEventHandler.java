package com.infamous.sapience.client;

import com.infamous.sapience.Sapience;
import com.infamous.sapience.capability.emotive.EmotiveProvider;
import com.infamous.sapience.util.PiglinTasksHelper;
import com.infamous.sapience.util.ReflectionHelper;
import net.minecraft.client.model.PiglinModel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinArmPose;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Sapience.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientForgeEventHandler {

    @SubscribeEvent
    public static void onRenderLivingPost(RenderLivingEvent.Post<?, ?> event){
        if(event.getEntity() instanceof Piglin piglin && event.getRenderer().getModel() instanceof PiglinModel<?> piglinModel){
            float ageInTicks = piglin.tickCount + event.getPartialTick();
            if (piglin.getArmPose() == PiglinArmPose.DEFAULT && PiglinTasksHelper.shouldAdmire(piglin.getOffhandItem())) {
                piglinModel.head.xRot = 0.5F;
                piglinModel.head.yRot = 0.0F;
                if (piglin.isLeftHanded()) {
                    piglinModel.rightArm.yRot = -0.5F;
                    piglinModel.rightArm.xRot = -0.9F;
                } else {
                    piglinModel.leftArm.yRot = 0.5F;
                    piglinModel.leftArm.xRot = -0.9F;
                }
            }
            piglin.getCapability(EmotiveProvider.EMOTIVE_CAPABILITY).ifPresent(
                    e -> {
                        if (e.getShakeHeadTicks() > 0) {
                            piglinModel.head.loadPose(ReflectionHelper.getHeadDefault(piglinModel)); // reset bipedHead
                            piglinModel.head.zRot = 0.3F * Mth.sin(0.45F * ageInTicks);
                            piglinModel.head.xRot = 0.4F;
                        }
                    }
            );
            handleEatingAnimation(piglinModel, piglin, ageInTicks);
        }
    }

    public static void handleEatingAnimation(PiglinModel<?> piglinModel, LivingEntity entityIn, float ageInTicks) {
        if (entityIn.getMainArm() == HumanoidArm.RIGHT)
        {
            eatingAnimationRightHand(piglinModel, InteractionHand.MAIN_HAND, entityIn, ageInTicks);
            eatingAnimationLeftHand(piglinModel, InteractionHand.OFF_HAND, entityIn, ageInTicks);
        } else {
            eatingAnimationRightHand(piglinModel, InteractionHand.OFF_HAND, entityIn, ageInTicks);
            eatingAnimationLeftHand(piglinModel, InteractionHand.MAIN_HAND, entityIn, ageInTicks);
        }
    }

    private static void eatingAnimationRightHand(PiglinModel<?> piglinModel, InteractionHand hand, LivingEntity entity, float ageInTicks)
    {
        ItemStack itemstack = entity.getItemInHand(hand);
        boolean drinkingoreating = itemstack.getUseAnimation() == UseAnim.EAT || itemstack.getUseAnimation() == UseAnim.DRINK;
        if (entity.getUseItemRemainingTicks() > 0 && drinkingoreating && entity.getUsedItemHand() == hand)
        {
            piglinModel.leftArm.loadPose(ReflectionHelper.getLeftArmDefault(piglinModel)); // reset bipedRightArm
            piglinModel.rightArm.yRot = -0.5F;
            piglinModel.rightArm.xRot = -1.3F;
            piglinModel.rightArm.zRot = Mth.cos(ageInTicks) * 0.1F;
            piglinModel.rightSleeve.copyFrom(piglinModel.rightArm);
        }
    }

    private static void eatingAnimationLeftHand(PiglinModel<?> piglinModel, InteractionHand hand, LivingEntity entity, float ageInTicks)
    {
        ItemStack itemstack = entity.getItemInHand(hand);
        boolean drinkingoreating = itemstack.getUseAnimation() == UseAnim.EAT || itemstack.getUseAnimation() == UseAnim.DRINK;
        if (entity.getUseItemRemainingTicks() > 0 && drinkingoreating && entity.getUsedItemHand() == hand)
        {
            piglinModel.rightArm.loadPose(ReflectionHelper.getRightArmDefault(piglinModel)); // reset bipedLeftArm
            piglinModel.leftArm.yRot = 0.5F;
            piglinModel.leftArm.xRot = -1.3F;
            piglinModel.leftArm.zRot = Mth.cos(ageInTicks) * 0.1F;
            piglinModel.leftSleeve.copyFrom(piglinModel.leftArm);
        }
    }
}
