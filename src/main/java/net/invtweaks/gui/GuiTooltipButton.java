package net.invtweaks.gui;

import net.invtweaks.Const;
import net.minecraft.client.Minecraft;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.ButtonWidget;

/**
 * Icon-size button, which get drawns in a specific way to fit its small size.
 * @author Jimeo Wan
 *
 */
public class GuiTooltipButton extends ButtonWidget {
    
    public final static int DEFAULT_BUTTON_WIDTH = 200;
    public final static int LINE_HEIGHT = 11;
    
    private int hoverTime = 0;
    private long prevSystemTime = 0;
    
    private String tooltip = null;
    private String[] tooltipLines = null;
    private int tooltipWidth = -1;

    public GuiTooltipButton(int id,
            int x, int y, String displayString) {
        this(id, x, y, 150, 20, displayString, null);
    }
    
    /**
     * Default size is 150, the common "GuiSmallButton" button size.
     */
    public GuiTooltipButton(int id,
            int x, int y, String displayString, String tooltip) {
        this(id, x, y, 150, 20, displayString, tooltip);
    }

    public GuiTooltipButton(int id, int x, int y, int w, int h,
            String displayString) {
        this(id, x, y, w, h, displayString, null);
    }
    
    public GuiTooltipButton(int id, int x, int y, int w, int h,
            String displayString, String tooltip) {
        super(id, x, y, w, h, displayString);
        if (tooltip != null) {
            setTooltip(tooltip);
        }
    }

    public void render(Minecraft minecraft, int i, int j) {
        super.render(minecraft, i, j);
        
        if (!visible) {
            return;
        }
        
        if (tooltipLines != null) {
            // Compute hover time
            if (isMouseOverButton(i, j)) {
                long systemTime = System.currentTimeMillis();
                if (prevSystemTime != 0) {
                    hoverTime += systemTime - prevSystemTime;
                }
                prevSystemTime = systemTime;
            }
            else {
                hoverTime = 0;
                prevSystemTime = 0;
            }
            
            // Draw tooltip if hover time is long enough
            if (hoverTime > Const.TOOLTIP_DELAY && tooltipLines != null) {
                
                TextRenderer fontRenderer = minecraft.textRenderer;

                // Compute tooltip params
                int x = i + 12, y = j - LINE_HEIGHT*tooltipLines.length;
                if (tooltipWidth == -1) {
                    for (String line : tooltipLines) {
                        tooltipWidth = Math.max(fontRenderer.getWidth(line), tooltipWidth);
                    }
                }
                if (x + tooltipWidth > minecraft.currentScreen.width) {
                    x = minecraft.currentScreen.width - tooltipWidth;
                }
                
                // Draw background
                fillGradient(x - 3, y - 3,
                        x + tooltipWidth + 3, y + LINE_HEIGHT*tooltipLines.length, 
                        0xc0000000, 0xc0000000);
                
                // Draw lines
                int lineCount = 0;
                for (String line : tooltipLines) {
                    minecraft.textRenderer.drawWithShadow(
                            line, x, y + (lineCount++)*LINE_HEIGHT, -1);
                }
            }
        }

    }
    
    protected boolean isMouseOverButton(int i, int j) {
        return i >= x && j >= y && i < x + width && j < y + height;
    }
    
    protected int getTextColor(int i, int j) {
        
        int textColor = 0xffe0e0e0;
        if (!active) {
            textColor = 0xffa0a0a0;
        } else if (isMouseOverButton(i, j)) {
            textColor = 0xffffffa0;
        }
        return textColor;

    }
    
    public void setTooltip(String tooltip) {
        this.tooltip = tooltip;
        this.tooltipLines = tooltip.split("\n");
    }
    
    public String getTooltip() {
        return tooltip;
    }
    
}