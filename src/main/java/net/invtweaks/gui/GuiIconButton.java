package net.invtweaks.gui;

import net.invtweaks.config.InvTweaksConfigManager;
import net.minecraft.client.Minecraft;

import org.lwjgl.opengl.GL11;

public class GuiIconButton extends GuiTooltipButton {
	protected InvTweaksConfigManager cfgManager;

	public GuiIconButton(InvTweaksConfigManager cfgManager, int id, int x, int y, int w, int h, String displayString, String tooltip) {
		super(id, x, y, w, h, displayString, tooltip);
		this.cfgManager = cfgManager;
	}

	public void render(Minecraft minecraft, int i, int j) {
		super.render(minecraft, i, j);
		if(this.active) {
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, minecraft.textureManager.getTextureId("/gui/gui.png"));
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			int k = this.getYImage(this.isMouseOverButton(i, j));
			this.drawTexture(this.x, this.y, 1, 46 + k * 20 + 1, this.width / 2, this.height / 2);
			this.drawTexture(this.x, this.y + this.height / 2, 1, 46 + k * 20 + 20 - this.height / 2 - 1, this.width / 2, this.height / 2);
			this.drawTexture(this.x + this.width / 2, this.y, 200 - this.width / 2 - 1, 46 + k * 20 + 1, this.width / 2, this.height / 2);
			this.drawTexture(this.x + this.width / 2, this.y + this.height / 2, 200 - this.width / 2 - 1, 46 + k * 20 + 19 - this.height / 2, this.width / 2, this.height / 2);
		}
	}
}
