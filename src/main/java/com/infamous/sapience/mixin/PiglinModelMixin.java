package com.infamous.sapience.mixin;

import com.infamous.sapience.mod.IShakesHead;
import net.minecraft.client.renderer.entity.model.PiglinModel;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.MathHelper;
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
    private ModelRenderer field_241661_z_;
    @Shadow
    @Final
    private ModelRenderer field_241658_A_;
    @Shadow
    @Final
    private ModelRenderer field_241659_B_;

    @Inject(at = @At("RETURN"), method = "setRotationAngles")
    private void setRotationAngles(MobEntity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo callbackInfo){
        boolean isShakingHead = false;
        if (entityIn instanceof IShakesHead) {
            isShakingHead = ((IShakesHead)entityIn).getShakeHeadTicks() > 0;
        }

        if (isShakingHead) {
            this.bipedHead.copyModelAngles(this.field_241661_z_); // reset bipedHead
            this.bipedHead.rotateAngleZ = 0.3F * MathHelper.sin(0.45F * ageInTicks);
            this.bipedHead.rotateAngleX = 0.4F;
        }

        // Eating animation, from tallestred's / seymourimadeit's Player Eating Animation mod
        if (entityIn.getPrimaryHand() == HandSide.RIGHT)
        {
            this.eatingAnimationRightHand(Hand.MAIN_HAND, entityIn, ageInTicks);
            this.eatingAnimationLeftHand(Hand.OFF_HAND, entityIn, ageInTicks);
        } else {
            this.eatingAnimationRightHand(Hand.OFF_HAND, entityIn, ageInTicks);
            this.eatingAnimationLeftHand(Hand.MAIN_HAND, entityIn, ageInTicks);
        }
    }

    private void eatingAnimationRightHand(Hand hand, LivingEntity entity, float ageInTicks)
    {
        ItemStack itemstack = entity.getHeldItem(hand);
        boolean drinkingoreating = itemstack.getUseAction() == UseAction.EAT || itemstack.getUseAction() == UseAction.DRINK;
        if (entity.getItemInUseCount() > 0 && drinkingoreating && entity.getActiveHand() == hand)
        {
            this.bipedLeftArm.copyModelAngles(this.field_241658_A_); // reset bipedRightArm
            this.bipedRightArm.rotateAngleY = -0.5F;
            this.bipedRightArm.rotateAngleX = -1.3F;
            this.bipedRightArm.rotateAngleZ = MathHelper.cos(ageInTicks) * 0.1F;
            this.bipedRightArmwear.copyModelAngles(bipedRightArm);
        }
    }

    private void eatingAnimationLeftHand(Hand hand, LivingEntity entity, float ageInTicks)
    {
        ItemStack itemstack = entity.getHeldItem(hand);
        boolean drinkingoreating = itemstack.getUseAction() == UseAction.EAT || itemstack.getUseAction() == UseAction.DRINK;
        if (entity.getItemInUseCount() > 0 && drinkingoreating && entity.getActiveHand() == hand)
        {
            this.bipedRightArm.copyModelAngles(this.field_241659_B_); // reset bipedLeftArm
            this.bipedLeftArm.rotateAngleY = 0.5F;
            this.bipedLeftArm.rotateAngleX = -1.3F;
            this.bipedLeftArm.rotateAngleZ = MathHelper.cos(ageInTicks) * 0.1F;
            this.bipedLeftArmwear.copyModelAngles(bipedLeftArm);
        }
    }
}
