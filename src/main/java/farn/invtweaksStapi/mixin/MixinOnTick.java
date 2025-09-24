package farn.invtweaksStapi.mixin;

import farn.invtweaksStapi.InvTweaksStapi;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class MixinOnTick {
    @Shadow
    private Minecraft client;

    @Inject(method="onFrameUpdate", at=@At("TAIL"))
    public void frameUpdate(float par1, CallbackInfo ci) {
        InvTweaksStapi.onTick(client);
    }
}
