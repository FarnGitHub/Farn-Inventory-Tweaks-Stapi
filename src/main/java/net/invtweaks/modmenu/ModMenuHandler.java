package net.invtweaks.modmenu;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.invtweaks.InvTweaks;
import net.invtweaks.gui.GuiInventorySettings;
import net.minecraft.client.gui.screen.Screen;

public class ModMenuHandler implements ModMenuApi {

	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return ModMenuHandler::getConfigScreen;
	}

	private static Screen getConfigScreen(Screen parent) {
		return new GuiInventorySettings(InvTweaks.getInstance().mc, parent, InvTweaks.getInstance().cfgManager.getConfig());
	}
}
