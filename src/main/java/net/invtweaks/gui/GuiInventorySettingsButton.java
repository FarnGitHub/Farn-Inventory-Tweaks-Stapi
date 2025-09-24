package net.invtweaks.gui;

import java.util.concurrent.TimeoutException;

import net.invtweaks.InvTweaks;
import net.invtweaks.config.InvTweaksConfig;
import net.invtweaks.config.InvTweaksConfigManager;
import net.invtweaks.library.ContainerManager;
import net.invtweaks.library.ContainerSectionManager;
import net.invtweaks.library.Obfuscation;
import net.minecraft.client.Minecraft;

public class GuiInventorySettingsButton extends GuiIconButton {
	public GuiInventorySettingsButton(InvTweaksConfigManager cfgManager, int id, int x, int y, int w, int h, String displayString, String tooltip) {
		super(cfgManager, id, x, y, w, h, displayString, tooltip);
	}

	public void render(Minecraft minecraft, int i, int j) {
		super.render(minecraft, i, j);
		if(this.active) {
			this.drawCenteredTextWithShadow(minecraft.textRenderer, this.text, this.x + 5, this.y - 1, this.getTextColor(i, j));
		}
	}

	public boolean isMouseOver(Minecraft minecraft, int i, int j) {
		InvTweaksConfig config = this.cfgManager.getConfig();
		if(!super.isMouseOver(minecraft, i, j)) {
			return false;
		} else {
			try {
				ContainerSectionManager containerMgr = new ContainerSectionManager(minecraft, ContainerManager.ContainerSection.INVENTORY);
				if(Obfuscation.getHoldStackStatic(minecraft) != null) {
					try {
						for(int e = containerMgr.getSize() - 1; e >= 0; --e) {
							if(containerMgr.getItemStack(e) == null) {
								containerMgr.leftClick(e);
								break;
							}
						}
					} catch (TimeoutException timeoutException7) {
						InvTweaks.logInGameErrorStatic("Failed to put item down", timeoutException7);
					}
				}
			} catch (Exception exception8) {
				InvTweaks.logInGameErrorStatic("Failed to set up settings button", exception8);
			}

			this.cfgManager.makeSureConfigurationIsLoaded();
			minecraft.setScreen(new GuiInventorySettings(minecraft, Obfuscation.getCurrentScreenStatic(minecraft), config));
			return true;
		}
	}
}
