package farn.invtweaksStapi.mixin;

import net.invtweaks.InvTweaks;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public class MixinItemEntity {

    @Inject(method="onPlayerInteraction", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;sendPickup(Lnet/minecraft/entity/Entity;I)V", shift = At.Shift.BEFORE))
    public void onPickUp(PlayerEntity par1, CallbackInfo ci) {
        InvTweaks.instance.onItemPickup();
    }
}
