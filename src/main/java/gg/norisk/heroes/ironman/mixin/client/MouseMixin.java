package gg.norisk.heroes.ironman.mixin.client;

import gg.norisk.heroes.events.MouseEvents;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(Mouse.class)
public abstract class MouseMixin {
    @ModifyArgs(method = "onMouseButton", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/KeyBinding;setKeyPressed(Lnet/minecraft/client/util/InputUtil$Key;Z)V"))
    private void injected(Args args) {
        MouseEvents.INSTANCE.getMouseClickEvent().invoke(new MouseEvents.MouseClickEvent(args.get(0), args.get(1)));
    }
}
