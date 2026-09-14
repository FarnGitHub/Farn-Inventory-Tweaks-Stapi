package net.invtweaks.gui;

import net.invtweaks.InvTweaks;
import net.invtweaks.config.InvTweaksConfigManager;
import net.invtweaks.library.ContainerManager.ContainerSection;
import net.invtweaks.logic.SortingHandler;
import net.minecraft.client.Minecraft;

/**
 * Chest sorting button
 * @author Jimeo Wan
 *
 */
public class GuiSortingButton extends GuiIconButton {

    private final ContainerSection section = ContainerSection.CHEST;
    
    private int algorithm;

    public GuiSortingButton(InvTweaksConfigManager cfgManager, 
            int id, int x, int y, int w, int h,
            String displayString, String tooltip,
            int algorithm) {
        super(cfgManager, id, x, y, w, h, displayString, tooltip);
        this.algorithm = algorithm;
    }

    public void render(Minecraft minecraft, int i, int j) {
        super.render(minecraft, i, j);
        
        if (!visible) {
            return;
        }
        
        // Display symbol
        int textColor = getTextColor(i, j);
        if (text.equals("h")) {
            fill(x + 3, y + 3, x + width - 3, y + 4, textColor);
            fill(x + 3, y + 6, x + width - 3, y + 7, textColor);
        } else if (text.equals("v")) {
            fill(x + 3, y + 3, x + 4, y + height - 3, textColor);
            fill(x + 6, y + 3, x + 7, y + height - 3, textColor);
        } else {
            fill(x + 3, y + 3, x + width - 3, y + 4, textColor);
            fill(x + 5, y + 4, x + 6, y + 5, textColor);
            fill(x + 4, y + 5, x + 5, y + 6, textColor);
            fill(x + 3, y + 6, x + width - 3, y + 7, textColor);
        }
    }

    /**
     * Sort container
     */
    public boolean isMouseOver(Minecraft minecraft, int i, int j) {
        if (super.isMouseOver(minecraft, i, j)) {
            try {
                new SortingHandler(cfgManager.getConfig(),
                        section, algorithm).sort();
            } catch (Exception e) {
                InvTweaks.logInGameErrorStatic("Failed to sort container", e);
            }
            return true;
        } else {
            return false;
        }

    }
    
}