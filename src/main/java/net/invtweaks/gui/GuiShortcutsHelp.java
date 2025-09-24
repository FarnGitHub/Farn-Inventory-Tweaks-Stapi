package net.invtweaks.gui;

import java.util.LinkedList;

import net.invtweaks.Const;
import net.invtweaks.config.InvTweaksConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;


import org.lwjgl.input.Keyboard;

public class GuiShortcutsHelp extends Screen {
	private static final String SCREEN_TITLE = "Shortcuts help";
	private static final int ID_DONE = 0;
	private Minecraft b;
	private Screen parentScreen;
	private InvTweaksConfig config;

	public GuiShortcutsHelp(Minecraft mc, Screen parentScreen, InvTweaksConfig config) {
		this.b = mc;
		this.parentScreen = parentScreen;
		this.config = config;
	}

	public void init() {
		LinkedList controlList = new LinkedList();
		controlList.add(new ButtonWidget(0, this.width / 2 - 100, this.height / 6 + 168, "Done"));
		this.buttons = controlList;
	}

	public void render(int i, int j, float f) {
		this.renderBackground();
		this.drawCenteredTextWithShadow(this.textRenderer, "Shortcuts help", this.width / 2, 20, 0xFFFFFF);
		int y = this.height / 6;
		this.drawShortcutLine("Move", "Left click", 61183, y);
		y += 12;
		this.drawShortcutLine("Move to empty slot", "Right click", 61183, y);
		y += 20;
		this.drawShortcutLine("Move one stack", this.config.getProperty("shortcutKeyOneStack") + " + Click", 16776960, y);
		y += 12;
		this.drawShortcutLine("Move one item only", this.config.getProperty("shortcutKeyOneItem") + " + Click", 16776960, y);
		y += 12;
		this.drawShortcutLine("Move all items of same type", this.config.getProperty("shortcutKeyAllItems") + " + Click", 16776960, y);
		y += 20;
		this.drawShortcutLine("Move to upper section", this.config.getProperty("shortcutKeyToUpperSection") + " + Click", 65331, y);
		y += 12;
		this.drawShortcutLine("Move to lower section", this.config.getProperty("shortcutKeyToLowerSection") + " + Click", 65331, y);
		y += 12;
		this.drawShortcutLine("Move to hotbar", "0-9 + Click", 65331, y);
		y += 20;
		this.drawShortcutLine("Drop", this.config.getProperty("shortcutKeyDrop") + " + Click", 16746496, y);
		y += 12;
		this.drawShortcutLine("Craft all", "LSHIFT, RSHIFT + Click", 16746496, y);
		y += 12;
		this.drawShortcutLine("Select sorting configuration", "0-9 + " + Keyboard.getKeyName(Const.SORT_KEY_BINDING.code), 16746496, y);
		y += 12;
		super.render(i, j, f);
	}

	private void drawShortcutLine(String label, String value, int color, int y) {
		this.drawTextWithShadow(this.textRenderer, label, 50, y, -1);
		this.drawTextWithShadow(this.textRenderer, value.contains("DEFAULT") ? "-" : value, this.width / 2 + 40, y, color);
	}

	protected void buttonClicked(ButtonWidget guibutton) {
		switch(guibutton.id) {
		case 0:
			this.b.setScreen(this.parentScreen);
		default:
		}
	}
}
