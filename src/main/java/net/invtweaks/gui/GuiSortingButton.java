package net.invtweaks.gui;

import net.invtweaks.InvTweaks;
import net.invtweaks.config.InvTweaksConfigManager;
import net.invtweaks.library.ContainerManager;
import net.invtweaks.logic.SortingHandler;
import net.minecraft.client.Minecraft;

public class GuiSortingButton extends GuiIconButton {
	private final ContainerManager.ContainerSection section = ContainerManager.ContainerSection.CHEST;
	private int algorithm;

	public GuiSortingButton(InvTweaksConfigManager cfgManager, int id, int x, int y, int w, int h, String displayString, String tooltip, int algorithm) {
		super(cfgManager, id, x, y, w, h, displayString, tooltip);
		this.algorithm = algorithm;
	}

	public void render(Minecraft minecraft, int i, int j) {
		super.render(minecraft, i, j);
		if(this.active) {
			int textColor = this.getTextColor(i, j);
			if(this.text.equals("h")) {
				this.fill(this.x + 3, this.y + 3, this.x + this.width - 3, this.y + 4, textColor);
				this.fill(this.x + 3, this.y + 6, this.x + this.width - 3, this.y + 7, textColor);
			} else if(this.text.equals("v")) {
				this.fill(this.x + 3, this.y + 3, this.x + 4, this.y + this.height - 3, textColor);
				this.fill(this.x + 6, this.y + 3, this.x + 7, this.y + this.height - 3, textColor);
			} else {
				this.fill(this.x + 3, this.y + 3, this.x + this.width - 3, this.y + 4, textColor);
				this.fill(this.x + 5, this.y + 4, this.x + 6, this.y + 5, textColor);
				this.fill(this.x + 4, this.y + 5, this.x + 5, this.y + 6, textColor);
				this.fill(this.x + 3, this.y + 6, this.x + this.width - 3, this.y + 7, textColor);
			}

		}
	}

	public boolean isMouseOver(Minecraft minecraft, int i, int j) {
		if(super.isMouseOver(minecraft, i, j)) {
			try {
				(new SortingHandler(minecraft, this.cfgManager.getConfig(), this.section, this.algorithm)).sort();
			} catch (Exception exception5) {
				InvTweaks.logInGameErrorStatic("Failed to sort container", exception5);
			}

			return true;
		} else {
			return false;
		}
	}
}
