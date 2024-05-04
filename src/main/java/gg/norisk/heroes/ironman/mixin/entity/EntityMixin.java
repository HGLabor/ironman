package gg.norisk.heroes.ironman.mixin.entity;

import gg.norisk.heroes.ironman.abilities.FlyAbility;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Inject(method = "onLanding", at = @At("TAIL"))
    private void injected(CallbackInfo ci) {
        //FlyAbility.INSTANCE.onLanding((Entity) (Object) this);
    }
}
