package gg.norisk.heroes.ironman.mixin.client.render;

import gg.norisk.heroes.ironman.abilities.RepulsorBlastAbility;
import gg.norisk.heroes.ironman.client.render.CameraShaker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Shadow
    @Final
    MinecraftClient client;

    @Inject(method = "renderHand", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;push()V", shift = At.Shift.AFTER))
    private void injected(MatrixStack matrixStack, Camera camera, float f, CallbackInfo ci) {
        RepulsorBlastAbility.INSTANCE.rotateHand(matrixStack,camera,f);
    }


    @Inject(
            method = "render",
            at = @At("HEAD")
    )
    private void camerashake$onRender(float tickDelta, long startTime, boolean tick, CallbackInfo ci) {
        if (!client.skipGameRender && tick && client.world != null) {
            CameraShaker.INSTANCE.newFrame();
        }
    }

    @Inject(
            method = "renderHand",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/GameRenderer;tiltViewWhenHurt(Lnet/minecraft/client/util/math/MatrixStack;F)V"
            )
    )
    private void camerashake$shakeHand(MatrixStack matrices, Camera camera, float tickDelta, CallbackInfo ci) {
        double x = CameraShaker.INSTANCE.getAvgX();
        double y = CameraShaker.INSTANCE.getAvgY();

        matrices.translate(x, -y, .0); // opposite of camera
    }
}
