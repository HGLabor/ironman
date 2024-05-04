package gg.norisk.heroes.ironman.mixin.client.render.entity;

import gg.norisk.heroes.ironman.client.render.entity.feature.FlightParticleRenderer;
import gg.norisk.heroes.ironman.player.IronManPlayer;
import gg.norisk.heroes.ironman.player.IronManPlayerKt;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.ArmorEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityRenderer.class)
public abstract class PlayerEntityRendererMixin extends LivingEntityRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {
    public PlayerEntityRendererMixin(EntityRendererFactory.Context context, PlayerEntityModel<AbstractClientPlayerEntity> entityModel, float f) {
        super(context, entityModel, f);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void injected(EntityRendererFactory.Context context, boolean bl, CallbackInfo ci) {
        this.addFeature(
                new FlightParticleRenderer<>(
                        this,
                        new ArmorEntityModel(context.getPart(bl ? EntityModelLayers.PLAYER_SLIM_INNER_ARMOR : EntityModelLayers.PLAYER_INNER_ARMOR)),
                        new ArmorEntityModel(context.getPart(bl ? EntityModelLayers.PLAYER_SLIM_OUTER_ARMOR : EntityModelLayers.PLAYER_OUTER_ARMOR)),
                        context.getModelManager()
                )
        );
    }

    @Inject(method = "setupTransforms(Lnet/minecraft/client/network/AbstractClientPlayerEntity;Lnet/minecraft/client/util/math/MatrixStack;FFF)V", at = @At(value = "HEAD"), cancellable = true)
    private void setupTransformsInjection(AbstractClientPlayerEntity abstractClientPlayerEntity, MatrixStack matrixStack, float f, float g, float h, CallbackInfo ci) {
        if (!IronManPlayerKt.isIronManFlying(abstractClientPlayerEntity)) return;
        var i = ((IronManPlayer) abstractClientPlayerEntity).getFlyingLeaningPitch(h);
        if (i > 0.0F) {
            float speed = (float) abstractClientPlayerEntity.getVelocity().lengthSquared() * 2;
            super.setupTransforms(abstractClientPlayerEntity, matrixStack, f, g, h);
            var j = MathHelper.clamp( -45f * speed - abstractClientPlayerEntity.getPitch(),-70f,45f);
            var k = MathHelper.lerp(i, 0.0F, j);
            var size = abstractClientPlayerEntity.getHeight() / 2;
            matrixStack.translate(0f, size, 0f);
            matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(k));
            matrixStack.translate(0f, -size, 0f);
            ci.cancel();
        }
    }
}
