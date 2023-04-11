package com.infamous.sapience.mixin;

import com.infamous.sapience.capability.emotive.EmotiveProvider;
import com.infamous.sapience.util.PiglinTasksHelper;
import net.minecraft.client.model.PiglinModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinArmPose;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PiglinModel.class)
public class PiglinModelMixin<T extends Mob> extends PlayerModel<T> {

    public PiglinModelMixin(ModelPart bakedLayer, boolean smallArms) {
        super(bakedLayer, smallArms);
    }

    @Inject(at = @At(value = "TAIL"), method = "setupAnim(Lnet/minecraft/world/entity/Mob;FFFFF)V")
    private void postSetupAnim(T piglinLike, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci){
        if(piglinLike instanceof Piglin piglin){
            this.handleCustomAdmiringAnimation(piglin);
            this.handleEmotiveAnimation(ageInTicks, piglin);
            this.handleEatingAnimation(piglin, ageInTicks);

            this.hat.copyFrom(this.head);
            this.leftSleeve.copyFrom(this.leftArm);
            this.rightSleeve.copyFrom(this.rightArm);
        }
    }

    private void handleEatingAnimation(LivingEntity entityIn, float ageInTicks) {
        if (entityIn.getMainArm() == HumanoidArm.RIGHT)
        {
            eatingAnimationRightHand(InteractionHand.MAIN_HAND, entityIn, ageInTicks);
            eatingAnimationLeftHand(InteractionHand.OFF_HAND, entityIn, ageInTicks);
        } else {
            eatingAnimationRightHand(InteractionHand.OFF_HAND, entityIn, ageInTicks);
            eatingAnimationLeftHand(InteractionHand.MAIN_HAND, entityIn, ageInTicks);
        }
    }

    private void eatingAnimationRightHand(InteractionHand hand, LivingEntity entity, float ageInTicks)
    {
        ItemStack itemstack = entity.getItemInHand(hand);
        boolean drinkingoreating = itemstack.getUseAnimation() == UseAnim.EAT || itemstack.getUseAnimation() == UseAnim.DRINK;
        if (entity.getUseItemRemainingTicks() > 0 && drinkingoreating && entity.getUsedItemHand() == hand)
        {
            this.rightArm.yRot = -0.5F;
            this.rightArm.xRot = -1.3F;
            this.rightArm.zRot = Mth.cos(ageInTicks) * 0.1F;
            this.rightSleeve.copyFrom(this.rightArm);
            this.head.xRot = Mth.cos(ageInTicks) * 0.2F;
            this.head.yRot = 0.0F;
        }
    }

    private void eatingAnimationLeftHand(InteractionHand hand, LivingEntity entity, float ageInTicks)
    {
        ItemStack itemstack = entity.getItemInHand(hand);
        boolean drinkingoreating = itemstack.getUseAnimation() == UseAnim.EAT || itemstack.getUseAnimation() == UseAnim.DRINK;
        if (entity.getUseItemRemainingTicks() > 0 && drinkingoreating && entity.getUsedItemHand() == hand)
        {
            this.leftArm.yRot = 0.5F;
            this.leftArm.xRot = -1.3F;
            this.leftArm.zRot = Mth.cos(ageInTicks) * 0.1F;
            this.leftSleeve.copyFrom(this.leftArm);
            this.head.xRot = Mth.cos(ageInTicks) * 0.2F;
            this.head.yRot = 0.0F;
        }
    }

    private void handleCustomAdmiringAnimation(Piglin piglin) {
        if (piglin.getArmPose() == PiglinArmPose.DEFAULT && PiglinTasksHelper.shouldAdmire(piglin.getOffhandItem())) {
            this.head.xRot = 0.5F;
            this.head.yRot = 0.0F;
            if (piglin.isLeftHanded()) {
                this.rightArm.yRot = -0.5F;
                this.rightArm.xRot = -0.9F;
            } else {
                this.leftArm.yRot = 0.5F;
                this.leftArm.xRot = -0.9F;
            }
        }
    }

    private void handleEmotiveAnimation(float ageInTicks, Piglin piglin) {
        piglin.getCapability(EmotiveProvider.EMOTIVE_CAPABILITY).ifPresent(
                e -> {
                    if (e.getShakeHeadTicks() > 0) {
                        this.head.zRot = 0.3F * Mth.sin(0.45F * ageInTicks);
                        this.head.xRot = 0.4F;
                    }
                }
        );
    }
}
