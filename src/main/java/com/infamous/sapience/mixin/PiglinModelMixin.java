package com.infamous.sapience.mixin;

import com.infamous.sapience.mod.IShakesHead;
import net.minecraft.client.renderer.entity.model.PiglinModel;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.MobEntity;
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

    @Inject(at = @At("RETURN"), method = "setRotationAngles")
    private void setRotationAngles(MobEntity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo callbackInfo){
        boolean isShakingHead = false;
        if (entityIn instanceof IShakesHead) {
            isShakingHead = ((IShakesHead)entityIn).getShakeHeadTicks() > 0;
        }

        if (isShakingHead) {
            this.bipedHead.copyModelAngles(this.field_241661_z_);
            this.bipedHead.rotateAngleZ = 0.3F * MathHelper.sin(0.45F * ageInTicks);
            this.bipedHead.rotateAngleX = 0.4F;
        }
    }
}
