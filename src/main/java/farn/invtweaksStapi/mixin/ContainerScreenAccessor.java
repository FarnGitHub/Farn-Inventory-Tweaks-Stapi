package farn.invtweaksStapi.mixin;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(HandledScreen.class)
public interface ContainerScreenAccessor {

    @Accessor("backgroundWidth")
    public int bgWidths();

    @Accessor("backgroundHeight")
    public int bgHeights();
}
