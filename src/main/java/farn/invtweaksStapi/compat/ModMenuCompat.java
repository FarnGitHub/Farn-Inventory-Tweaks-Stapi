package farn.invtweaksStapi.compat;

import net.danygames2014.modmenu.api.ConfigScreenFactory;
import net.danygames2014.modmenu.api.ModMenuApi;
import net.invtweaks.InvTweaks;
import net.invtweaks.gui.GuiInventorySettings;

public class ModMenuCompat implements ModMenuApi {

    public ConfigScreenFactory<GuiInventorySettings> getModConfigScreenFactory() {
        return screen -> new GuiInventorySettings(
                InvTweaks.getInstance().mc,
                screen,
                InvTweaks.getInstance().cfgManager.getConfig()
        );
    }
}
