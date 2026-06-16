package farn.invtweaksStapi.mixin;

import farn.invtweaksStapi.InvTweaksStapi;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MixinMinecraft {

    @Inject(
            method = "tick()V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/Minecraft;isWorldRemote()Z",
                    shift = At.Shift.BEFORE,
                    ordinal = 0
            )
    )
    private void invTweak_keyPressInGame(CallbackInfo ci) {
        InvTweaksStapi.pressKey();
    }
}
