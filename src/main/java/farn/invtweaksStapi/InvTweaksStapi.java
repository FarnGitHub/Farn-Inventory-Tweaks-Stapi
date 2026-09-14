package farn.invtweaksStapi;

import net.invtweaks.Const;
import net.invtweaks.InvTweaks;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;

public class InvTweaksStapi {

    private static boolean pressed = false;

    public static void tick() {
        Minecraft mc = Minecraft.INSTANCE;
        InvTweaks it = InvTweaks.instance;

        if (mc.currentScreen != null)
            it.onTickInGUI(mc.currentScreen);

        if (mc.world != null)
            it.onTickInGame();

        if (Keyboard.isKeyDown(Const.SORT_KEY_BINDING.code)) {
            if (!pressed) {
                pressed = true;
                it.onSortingKeyPressed();
            }
        } else pressed = false;
    }

    private InvTweaksStapi() {}

}
