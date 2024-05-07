package gg.norisk.heroes.ironman.mixin.client.render.entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import gg.norisk.heroes.ironman.IronManManager;
import gg.norisk.heroes.ironman.client.render.entity.feature.FlightParticleRenderer;
import gg.norisk.heroes.ironman.client.render.entity.feature.IronManArmorOverlayRenderer;
import gg.norisk.heroes.ironman.player.IronManPlayer;
import gg.norisk.heroes.ironman.player.IronManPlayerKt;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.ArmorEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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
        this.addFeature(new IronManArmorOverlayRenderer(this));
    }

    @WrapOperation(
            method = "renderArm",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/SkinTextures;comp_1626()Lnet/minecraft/util/Identifier;")
    )
    private Identifier handleFakeSkin(SkinTextures instance, Operation<Identifier> original, MatrixStack matrixStack,
                                      VertexConsumerProvider vertexConsumerProvider,
                                      int i,
                                      AbstractClientPlayerEntity player,
                                      ModelPart modelPart,
                                      ModelPart modelPart2) {
        if (IronManArmorOverlayRenderer.Companion.shouldRenderIronManSkin(player)) {
            return IronManManager.INSTANCE.getSkin();
        } else {
            return original.call(instance);
        }
    }

    @Inject(method = "getTexture(Lnet/minecraft/client/network/AbstractClientPlayerEntity;)Lnet/minecraft/util/Identifier;", at = @At("RETURN"), cancellable = true)
    private void injected(AbstractClientPlayerEntity abstractClientPlayerEntity, CallbackInfoReturnable<Identifier> cir) {
        if (IronManArmorOverlayRenderer.Companion.shouldRenderIronManSkin(abstractClientPlayerEntity)) {
            cir.setReturnValue(IronManManager.INSTANCE.getSkin());
        }
    }

    @Inject(method = "setupTransforms(Lnet/minecraft/client/network/AbstractClientPlayerEntity;Lnet/minecraft/client/util/math/MatrixStack;FFF)V", at = @At(value = "HEAD"), cancellable = true)
    private void setupTransformsInjection(AbstractClientPlayerEntity abstractClientPlayerEntity, MatrixStack matrixStack, float f, float g, float h, CallbackInfo ci) {
        if (!IronManPlayerKt.isIronManFlying(abstractClientPlayerEntity)) return;
        var i = ((IronManPlayer) abstractClientPlayerEntity).getFlyingLeaningPitch(h);
        if (i > 0.0F) {
            float speed = (float) abstractClientPlayerEntity.getVelocity().lengthSquared() * 2;
            super.setupTransforms(abstractClientPlayerEntity, matrixStack, f, g, h);
            var j = MathHelper.clamp(-45f * speed - abstractClientPlayerEntity.getPitch(), -70f, 45f);
            var k = MathHelper.lerp(i, 0.0F, j);
            var size = abstractClientPlayerEntity.getHeight() / 2;
            matrixStack.translate(0f, size, 0f);
            matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(k));
            matrixStack.translate(0f, -size, 0f);
            ci.cancel();
        }
    }
}
