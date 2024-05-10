package gg.norisk.heroes.ironman.mixin.client.render.entity.model;

import gg.norisk.heroes.ironman.player.IronManPlayerKt;
import net.minecraft.client.render.entity.model.AnimalModel;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.ModelWithArms;
import net.minecraft.client.render.entity.model.ModelWithHead;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BipedEntityModel.class)
public abstract class BipedEntityModelMixin<T extends LivingEntity> extends AnimalModel<T> implements ModelWithArms, ModelWithHead {
    @Shadow
    public BipedEntityModel.ArmPose leftArmPose;

    @Shadow
    public BipedEntityModel.ArmPose rightArmPose;

    @Inject(method = "animateModel(Lnet/minecraft/entity/LivingEntity;FFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/model/AnimalModel;animateModel(Lnet/minecraft/entity/Entity;FFF)V"))
    private void animateModelInjection(T livingEntity, float f, float g, float h, CallbackInfo ci) {
        if (livingEntity instanceof PlayerEntity player && (IronManPlayerKt.isRepulsorCharging(player) || IronManPlayerKt.isMissileSelecting(player))) {
            this.leftArmPose = this.rightArmPose = BipedEntityModel.ArmPose.BOW_AND_ARROW;
        }
    }
}