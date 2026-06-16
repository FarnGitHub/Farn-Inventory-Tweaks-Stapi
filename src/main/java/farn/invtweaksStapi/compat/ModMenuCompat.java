package farn.invtweaksStapi.compat;

import net.danygames2014.modmenu.api.ConfigScreenFactory;
import net.danygames2014.modmenu.api.ModMenuApi;
import net.invtweaks.InvTweaks;
import net.invtweaks.gui.GuiInventorySettings;
import net.minecraft.client.Minecraft;

public class ModMenuCompat implements ModMenuApi {

    public ConfigScreenFactory<GuiInventorySettings> getModConfigScreenFactory() {
        return screen -> new GuiInventorySettings(
                Minecraft.INSTANCE,
                screen,
                InvTweaks.instance.cfgManager.getConfig()
        );
    }
}
