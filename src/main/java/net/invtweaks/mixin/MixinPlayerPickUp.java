package net.invtweaks.mixin;

import net.invtweaks.InvTweaks;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.living.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public class MixinPlayerPickUp {

    @Inject(method="onPlayerCollision", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/living/player/PlayerEntity;sendPickup(Lnet/minecraft/entity/Entity;I)V", shift = At.Shift.BEFORE))
    public void onPickUp(PlayerEntity par1, CallbackInfo ci) {
        InvTweaks.getInstance().onItemPickup();
    }
}
