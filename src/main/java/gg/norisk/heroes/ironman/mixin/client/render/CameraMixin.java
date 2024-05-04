package gg.norisk.heroes.ironman.mixin.client.render;

import gg.norisk.heroes.ironman.player.IronManPlayerKt;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow
    protected abstract double clipToSpace(double d);

    @Unique
    private double lerpedLength;

    @ModifyArgs(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;moveBy(DDD)V", ordinal = 0))
    private void setPosInjection2(Args args, BlockView blockView, Entity entity, boolean bl, boolean bl2, float delta) {
        var velocity = entity.getVelocity().horizontalLengthSquared() / 2;
        var length = 4.0 + velocity * 3;
        lerpedLength = MathHelper.lerp((delta * 0.2), lerpedLength, length);
        if (entity instanceof PlayerEntity player && IronManPlayerKt.isIronManFlying(player)) {
            args.set(0, -this.clipToSpace(lerpedLength));
        }
    }
}
