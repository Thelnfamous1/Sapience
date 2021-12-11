package com.infamous.sapience.mixin;

import com.infamous.sapience.mod.IShakesHead;
import net.minecraft.client.model.PiglinModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PiglinModel.class)
public abstract class PiglinModelMixin extends PlayerModel {

    public PiglinModelMixin(float modelSize, boolean smallArmsIn) {
        super(modelSize, smallArmsIn);
    }

    @Shadow
    @Final
    private ModelPart headDefault;
    @Shadow
    @Final
    private ModelPart leftArmDefault;
    @Shadow
    @Final
    private ModelPart rightArmDefault;

    @Inject(at = @At("RETURN"), method = "setRotationAngles")
    private void setRotationAngles(Mob entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo callbackInfo){
        boolean isShakingHead = false;
        if (entityIn instanceof IShakesHead) {
            isShakingHead = ((IShakesHead)entityIn).getShakeHeadTicks() > 0;
        }

        if (isShakingHead) {
            this.head.copyFrom(this.headDefault); // reset bipedHead
            this.head.zRot = 0.3F * Mth.sin(0.45F * ageInTicks);
            this.head.xRot = 0.4F;
        }

        // Eating animation, from tallestred's / seymourimadeit's Player Eating Animation mod
        if (entityIn.getMainArm() == HumanoidArm.RIGHT)
        {
            this.eatingAnimationRightHand(InteractionHand.MAIN_HAND, entityIn, ageInTicks);
            this.eatingAnimationLeftHand(InteractionHand.OFF_HAND, entityIn, ageInTicks);
        } else {
            this.eatingAnimationRightHand(InteractionHand.OFF_HAND, entityIn, ageInTicks);
            this.eatingAnimationLeftHand(InteractionHand.MAIN_HAND, entityIn, ageInTicks);
        }
    }

    private void eatingAnimationRightHand(InteractionHand hand, LivingEntity entity, float ageInTicks)
    {
        ItemStack itemstack = entity.getItemInHand(hand);
        boolean drinkingoreating = itemstack.getUseAnimation() == UseAnim.EAT || itemstack.getUseAnimation() == UseAnim.DRINK;
        if (entity.getUseItemRemainingTicks() > 0 && drinkingoreating && entity.getUsedItemHand() == hand)
        {
            this.leftArm.copyFrom(this.leftArmDefault); // reset bipedRightArm
            this.rightArm.yRot = -0.5F;
            this.rightArm.xRot = -1.3F;
            this.rightArm.zRot = Mth.cos(ageInTicks) * 0.1F;
            this.rightSleeve.copyFrom(rightArm);
        }
    }

    private void eatingAnimationLeftHand(InteractionHand hand, LivingEntity entity, float ageInTicks)
    {
        ItemStack itemstack = entity.getItemInHand(hand);
        boolean drinkingoreating = itemstack.getUseAnimation() == UseAnim.EAT || itemstack.getUseAnimation() == UseAnim.DRINK;
        if (entity.getUseItemRemainingTicks() > 0 && drinkingoreating && entity.getUsedItemHand() == hand)
        {
            this.rightArm.copyFrom(this.rightArmDefault); // reset bipedLeftArm
            this.leftArm.yRot = 0.5F;
            this.leftArm.xRot = -1.3F;
            this.leftArm.zRot = Mth.cos(ageInTicks) * 0.1F;
            this.leftSleeve.copyFrom(leftArm);
        }
    }
}
