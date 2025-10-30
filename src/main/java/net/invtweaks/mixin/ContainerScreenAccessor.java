package net.invtweaks.mixin;

import net.minecraft.client.gui.screen.inventory.menu.InventoryMenuScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(InventoryMenuScreen.class)
public interface ContainerScreenAccessor {

    @Accessor("backgroundWidth")
    public int bgWidths();

    @Accessor("backgroundHeight")
    public int bgHeights();
}
