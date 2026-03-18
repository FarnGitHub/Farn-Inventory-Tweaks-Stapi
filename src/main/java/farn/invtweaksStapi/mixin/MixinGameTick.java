package farn.invtweaksStapi.mixin;

import farn.invtweaksStapi.InvTweaksStapi;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class MixinGameTick {

    @Inject(method="onFrameUpdate", at = @At("TAIL"))
    private void invtweak_onFrameUpdate(float tickDelta, CallbackInfo ci) {
        if(Minecraft.INSTANCE.currentScreen != null)
            InvTweaksStapi.instance.onTickInGUI(Minecraft.INSTANCE.currentScreen);

        if(Minecraft.INSTANCE.world != null)
            InvTweaksStapi.instance.onTickInGame();
    }
}
