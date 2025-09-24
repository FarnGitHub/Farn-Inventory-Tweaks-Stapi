package net.invtweaks.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.ButtonWidget;

public class GuiTooltipButton extends ButtonWidget {
	public static final int DEFAULT_BUTTON_WIDTH = 200;
	public static final int LINE_HEIGHT = 11;
	private int hoverTime;
	private long prevSystemTime;
	private String tooltip;
	private String[] tooltipLines;
	private int tooltipWidth;

	public GuiTooltipButton(int id, int x, int y, String displayString) {
		this(id, x, y, 150, 20, displayString, (String)null);
	}

	public GuiTooltipButton(int id, int x, int y, String displayString, String tooltip) {
		this(id, x, y, 150, 20, displayString, tooltip);
	}

	public GuiTooltipButton(int id, int x, int y, int w, int h, String displayString) {
		this(id, x, y, w, h, displayString, (String)null);
	}

	public GuiTooltipButton(int id, int x, int y, int w, int h, String displayString, String tooltip) {
		super(id, x, y, w, h, displayString);
		this.hoverTime = 0;
		this.prevSystemTime = 0L;
		this.tooltip = null;
		this.tooltipLines = null;
		this.tooltipWidth = -1;
		if(tooltip != null) {
			this.setTooltip(tooltip);
		}

	}

	public void render(Minecraft minecraft, int i, int j) {
		super.render(minecraft, i, j);
		if(this.active) {
			if(this.tooltipLines != null) {
				if(this.isMouseOverButton(i, j)) {
					long fontRenderer = System.currentTimeMillis();
					if(this.prevSystemTime != 0L) {
						this.hoverTime = (int)((long)this.hoverTime + (fontRenderer - this.prevSystemTime));
					}

					this.prevSystemTime = fontRenderer;
				} else {
					this.hoverTime = 0;
					this.prevSystemTime = 0L;
				}

				if(this.hoverTime > 1000 && this.tooltipLines != null) {
					TextRenderer fontRenderer12 = minecraft.textRenderer;
					int x = i + 12;
					int y = j - 11 * this.tooltipLines.length;
					int len$;
					if(this.tooltipWidth == -1) {
						String[] lineCount = this.tooltipLines;
						int arr$ = lineCount.length;

						for(len$ = 0; len$ < arr$; ++len$) {
							String i$ = lineCount[len$];
							this.tooltipWidth = Math.max(fontRenderer12.getWidth(i$), this.tooltipWidth);
						}
					}

					if(x + this.tooltipWidth > minecraft.currentScreen.width) {
						x = minecraft.currentScreen.width - this.tooltipWidth;
					}

					this.fillGradient(x - 3, y - 3, x + this.tooltipWidth + 3, y + 11 * this.tooltipLines.length, -1073741824, -1073741824);
					int i13 = 0;
					String[] string14 = this.tooltipLines;
					len$ = string14.length;

					for(int i15 = 0; i15 < len$; ++i15) {
						String line = string14[i15];
						minecraft.textRenderer.drawWithShadow(line, x, y + i13++ * 11, -1);
					}
				}
			}

		}
	}

	protected boolean isMouseOverButton(int i, int j) {
		return i >= this.x && j >= this.y && i < this.x + this.width && j < this.y + this.height;
	}

	protected int getTextColor(int i, int j) {
		int textColor = -2039584;
		if(!this.active) {
			textColor = -6250336;
		} else if(this.isMouseOverButton(i, j)) {
			textColor = -96;
		}

		return textColor;
	}

	public void setTooltip(String tooltip) {
		this.tooltip = tooltip;
		this.tooltipLines = tooltip.split("\n");
	}

	public String getTooltip() {
		return this.tooltip;
	}
}
