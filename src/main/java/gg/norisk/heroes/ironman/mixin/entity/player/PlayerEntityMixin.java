package gg.norisk.heroes.ironman.mixin.entity.player;

import gg.norisk.heroes.ironman.player.IronManPlayer;
import gg.norisk.heroes.ironman.player.IronManPlayerKt;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity implements IronManPlayer {
    @Unique
    private float lastFlyingLeaningPitch;
    @Unique
    private float flyingLeaningPitch;

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "initDataTracker", at = @At("TAIL"))
    private void initDataTrackerInjecetion(CallbackInfo ci) {
        this.dataTracker.startTracking(IronManPlayerKt.getFlyTracker(), false);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void tickInjection(CallbackInfo ci) {
        updateFlyingLeaningPitch();
    }

    @Unique
    private void updateFlyingLeaningPitch() {
        this.lastFlyingLeaningPitch = this.flyingLeaningPitch;
        if (IronManPlayerKt.isIronManFlying((PlayerEntity) ((Object) this))) {
            this.flyingLeaningPitch = Math.min(1.0F, this.flyingLeaningPitch + 0.09F);
        } else {
            this.flyingLeaningPitch = Math.max(0.0F, this.flyingLeaningPitch - 0.09F);
        }
    }

    @Unique
    @Override
    public float getFlyingLeaningPitch(float tickDelta) {
        return MathHelper.lerp(tickDelta, this.lastFlyingLeaningPitch, this.flyingLeaningPitch);
    }
}
