package gg.norisk.heroes.ironman.mixin.entity.player;

import gg.norisk.heroes.ironman.abilities.FlyAbility;
import gg.norisk.heroes.ironman.player.IronManPlayer;
import gg.norisk.heroes.ironman.player.IronManPlayerKt;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity implements IronManPlayer {
    @Shadow
    public abstract PlayerAbilities getAbilities();

    @Shadow public abstract void sendMessage(Text text, boolean bl);

    @Unique
    private float lastFlyingLeaningPitch;
    @Unique
    private float flyingLeaningPitch;
    @Unique
    private long startFlightTimestamp;

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "initDataTracker", at = @At("TAIL"))
    private void initDataTrackerInjecetion(CallbackInfo ci) {
        this.dataTracker.startTracking(IronManPlayerKt.getFlyTracker(), false);
        this.dataTracker.startTracking(IronManPlayerKt.getIronManTracker(), true);
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

    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    private void injected(Vec3d vec3d, CallbackInfo ci) {
        if (isOnGround()) {
            FlyAbility.INSTANCE.onLanding((PlayerEntity) ((Object) this));
        }
        if (this.getAbilities().flying && IronManPlayerKt.isIronManFlying((PlayerEntity) ((Object) this)) && !this.hasVehicle()) {
            super.travel(vec3d);
            double d = this.getVelocity().y;
            Vec3d vec3d3 = this.getVelocity();
            this.setVelocity(vec3d3.x, d * 0.5, vec3d3.z);
            this.onLanding();
            this.setFlag(7, false);
            ci.cancel();
        }
    }

    @Unique
    @Override
    public float getFlyingLeaningPitch(float tickDelta) {
        return MathHelper.lerp(tickDelta, this.lastFlyingLeaningPitch, this.flyingLeaningPitch);
    }

    @Unique
    @Override
    public long getStartFlightTimestamp() {
        return startFlightTimestamp;
    }

    @Unique
    @Override
    public void setStartFlightTimestamp(long startFlightTimestamp) {
        this.startFlightTimestamp = startFlightTimestamp;
    }
}
