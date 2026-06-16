package farn.invtweaksStapi;

import net.invtweaks.Const;
import net.invtweaks.InvTweaks;
import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;

public class InvTweaksStapi {

    public static Logger LOGGER = LogManager.getLogger("InvTweaks");

    public static void pressKey() {
        if(Keyboard.getEventKeyState() && Keyboard.getEventKey() == Const.SORT_KEY_BINDING.code) {
            InvTweaks.instance.onSortingKeyPressed();
        }
    }

    public static void tickGame() {
        InvTweaks.checkConfigLoad();

        if(Minecraft.INSTANCE.currentScreen != null)
            InvTweaks.instance.onTickInGUI(Minecraft.INSTANCE.currentScreen);

        if(Minecraft.INSTANCE.world != null)
            InvTweaks.instance.onTickInGame();
    }

}
