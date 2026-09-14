package net.invtweaks.gui;

import net.invtweaks.config.InvTweaksConfigManager;
import net.minecraft.client.Minecraft;

import org.lwjgl.opengl.GL11;

/**
 * Icon-size button, which get drawns in a specific way to fit its small size.
 * @author Jimeo Wan
 *
 */
public class GuiIconButton extends GuiTooltipButton {
    
    protected InvTweaksConfigManager cfgManager;
    
    public GuiIconButton(InvTweaksConfigManager cfgManager,
            int id, int x, int y, int w, int h, 
            String displayString, String tooltip) {
        super(id, x, y, w, h, displayString, tooltip);
        this.cfgManager = cfgManager;
    }

    public void render(Minecraft minecraft, int i, int j) {
        super.render(minecraft, i, j);

        if (!visible) {
            return;
        }
        
        // Draw background (use the 4 corners of the texture to fit best its small size)
        GL11.glBindTexture(3553, minecraft.textureManager.getTextureId("/gui/gui.png"));
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        int k = getYImage(isMouseOverButton(i, j));
        drawTexture(x, y, 1, 46 + k * 20 + 1, width / 2, height / 2);
        drawTexture(x, y + height / 2, 1, 46 + k * 20 + 20 - height / 2 - 1, width / 2, height / 2);
        drawTexture(x + width / 2, y, 200 - width / 2 - 1, 46 + k * 20 + 1, width / 2, height / 2);
        drawTexture(x + width / 2, y + height / 2, 200 - width / 2 - 1, 46 + k * 20 + 19 - height / 2, width / 2,
                height / 2);
        
    }
}