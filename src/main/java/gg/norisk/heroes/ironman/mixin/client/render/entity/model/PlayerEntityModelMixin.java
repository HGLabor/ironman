package gg.norisk.heroes.ironman.mixin.client.render.entity.model;

import gg.norisk.heroes.ironman.player.IronManPlayerKt;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityModel.class)
public abstract class PlayerEntityModelMixin<T extends LivingEntity> extends BipedEntityModel<T> {
    public PlayerEntityModelMixin(ModelPart modelPart) {
        super(modelPart);
    }

    @Inject(method = "setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/model/BipedEntityModel;setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V", shift = At.Shift.AFTER))
    private void injected(T livingEntity, float f, float g, float h, float i, float j, CallbackInfo ci) {
        if (!IronManPlayerKt.isIronManFlying((PlayerEntity) livingEntity)) return;
        float o = 0.3F;
        float p = 0.33333334F;
        this.head.pitch = 0f;
        this.hat.pitch = 0f;
        this.leftArm.pitch = p;
        this.rightArm.pitch = p;
        this.leftLeg.pitch = p;
        this.rightLeg.pitch = p;
    }
}
