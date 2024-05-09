package gg.norisk.heroes.ironman.mixin.entity;

import gg.norisk.heroes.ironman.abilities.FlyAbility;
import gg.norisk.heroes.ironman.abilities.RepulsorBlastAbility;
import gg.norisk.heroes.ironman.abilities.TransformAbility;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.mob.FlyingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Inject(method = "onTrackedDataSet", at = @At("RETURN"))
    private void injected(TrackedData<?> trackedData, CallbackInfo ci) {
        FlyAbility.INSTANCE.handleTrackedDataSet((LivingEntity) (Object) this, trackedData);
        TransformAbility.INSTANCE.handleTrackedDataSet((LivingEntity) (Object) this, trackedData);
        RepulsorBlastAbility.INSTANCE.handleTrackedDataSet((LivingEntity) (Object) this, trackedData);
    }
}
