package gg.norisk.heroes.ironman.mixin.client;

import gg.norisk.heroes.events.MouseEvents;
import gg.norisk.heroes.ironman.abilities.MissileAbility;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(Mouse.class)
public abstract class MouseMixin {
    @ModifyArgs(method = "onMouseButton", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/KeyBinding;setKeyPressed(Lnet/minecraft/client/util/InputUtil$Key;Z)V"))
    private void injected(Args args) {
        MouseEvents.INSTANCE.getMouseClickEvent().invoke(new MouseEvents.MouseClickEvent(args.get(0), args.get(1)));
    }

    @Inject(method = "onMouseScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;getOverlay()Lnet/minecraft/client/gui/screen/Overlay;", shift = At.Shift.BEFORE))
    private void hookMouseScroll(long window, double horizontal, double vertical, CallbackInfo callbackInfo) {
        MouseEvents.INSTANCE.getMouseScrollEvent().invoke(new MouseEvents.MouseScrollEvent(window, horizontal, vertical));
    }

    @Inject(
            method = "onMouseScroll(JDD)V",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/Mouse;eventDeltaVerticalWheel:D",
                    ordinal = 6
            ),
            cancellable = true
    )
    private void updateZoom(CallbackInfo info) {
        MissileAbility.INSTANCE.handleHotBarScrolling(info);
    }
}
