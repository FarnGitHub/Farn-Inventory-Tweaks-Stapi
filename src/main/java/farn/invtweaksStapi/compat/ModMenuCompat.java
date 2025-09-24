package farn.invtweaksStapi.compat;

import io.github.prospector.modmenu.api.ModMenuApi;
import net.invtweaks.InvTweaks;
import net.invtweaks.gui.GuiInventorySettings;
import net.invtweaks.library.Obfuscation;
import net.minecraft.client.gui.screen.Screen;

import java.util.function.Function;

public class ModMenuCompat implements ModMenuApi{

    @Override
    public String getModId() {
        return "og_invtweak";
    }

    public Function<Screen, ? extends Screen> getConfigScreenFactory() {
        return screen1 -> new GuiInventorySettings(InvTweaks.getInstance().mc, Obfuscation.getCurrentScreenStatic(InvTweaks.getInstance().mc), InvTweaks.getInstance().cfgManager.getConfig());
    }
}
