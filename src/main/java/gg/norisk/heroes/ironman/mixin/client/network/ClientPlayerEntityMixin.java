package gg.norisk.heroes.ironman.mixin.client.network;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import gg.norisk.heroes.ironman.player.IronManPlayerKt;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin {

    @ModifyReturnValue(
            method = "isUsingItem",
            at = @At("RETURN")
    )
    private boolean halveSpeed(boolean original) {
        return original || IronManPlayerKt.isRepulsorCharging((PlayerEntity) (Object) this);
    }
}
