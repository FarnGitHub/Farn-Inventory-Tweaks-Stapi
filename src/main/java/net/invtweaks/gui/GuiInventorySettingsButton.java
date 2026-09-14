package net.invtweaks.gui;

import java.util.concurrent.TimeoutException;

import net.invtweaks.InvTweaks;
import net.invtweaks.config.InvTweaksConfig;
import net.invtweaks.config.InvTweaksConfigManager;
import net.invtweaks.library.ContainerManager.ContainerSection;
import net.invtweaks.library.ContainerSectionManager;
import net.invtweaks.library.Obfuscation;
import net.minecraft.client.Minecraft;

/**
 * Button that opens the inventory & chest settings screen.
 * @author Jimeo Wan
 *
 */
public class GuiInventorySettingsButton extends GuiIconButton {
    
    public GuiInventorySettingsButton(InvTweaksConfigManager cfgManager,
            int id, int x, int y, int w, int h,
            String displayString, String tooltip) {
        super(cfgManager, id, x, y, w, h, displayString, tooltip);
    }

    public void render(Minecraft minecraft, int i, int j) {
        super.render(minecraft, i, j);

        if (!visible) {
            return;
        }

        // Display string
        drawCenteredTextWithShadow(minecraft.textRenderer, text,
                x + 5, y - 1, getTextColor(i, j));
    }

    /**
     * Sort container
     */
    public boolean isMouseOver(Minecraft minecraft, int i, int j) {
        
        InvTweaksConfig config = cfgManager.getConfig();
        
        if (super.isMouseOver(minecraft, i, j)) {
            // Put hold item down if necessary
            ContainerSectionManager containerMgr;
            
            try {
                containerMgr = new ContainerSectionManager(
                        ContainerSection.INVENTORY);
                if (Obfuscation.getHoldStackStatic() != null) {
                    try {
                        // Put hold item down
                        for (int k = containerMgr.getSize() - 1; k >= 0; k--) {
                            if (containerMgr.getItemStack(k) == null) {
                                containerMgr.leftClick(k);
                                break;
                            }
                        }
                    } catch (TimeoutException e) {
                        InvTweaks.logInGameErrorStatic("Failed to put item down", e);
                    }
                }
            } catch (Exception e) {
                InvTweaks.logInGameErrorStatic("Failed to set up settings button", e);
            }
            
            // Refresh config
            cfgManager.makeSureConfigurationIsLoaded();

            // Display menu
            minecraft.setScreen(new GuiInventorySettings(minecraft,
                    Obfuscation.getCurrentScreenStatic(), config));
            return true;
        } else {
            return false;
        }
    }
    
}