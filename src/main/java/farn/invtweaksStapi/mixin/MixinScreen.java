package farn.invtweaksStapi.mixin;

import farn.invtweaksStapi.InvTweaksStapi;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public class MixinScreen {

    @Inject(method="tickInput", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;onKeyboardEvent()V"))
    public void invTweaks_keyPressed(CallbackInfo ci) {
        InvTweaksStapi.pressKey();
    }
}
