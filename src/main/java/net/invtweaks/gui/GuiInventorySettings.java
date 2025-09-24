package net.invtweaks.gui;

import java.awt.Desktop;
import java.io.File;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Logger;

import net.invtweaks.Const;
import net.invtweaks.InvTweaks;
import net.invtweaks.config.InvTweaksConfig;
import net.minecraft.client.Minecraft;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import org.lwjgl.util.Point;

public class GuiInventorySettings extends Screen {
	private static final String SCREEN_TITLE = "Inventory and chests settings";
	private static final String MIDDLE_CLICK = "Middle click";
	private static final String CHEST_BUTTONS = "Chest buttons";
	private static final String SORT_ON_PICKUP = "Sort on pickup";
	private static final String SHORTCUTS = "Shortcuts";
	private static final String ON = ": ON";
	private static final String OFF = ": OFF";
	private static final String DISABLE_CI = ": Disable CI";
	private static final String SP_ONLY = ": Only in SP";
	private static final int ID_MIDDLE_CLICK = 1;
	private static final int ID_CHESTS_BUTTONS = 2;
	private static final int ID_SORT_ON_PICKUP = 3;
	private static final int ID_SHORTCUTS = 4;
	private static final int ID_SHORTCUTS_HELP = 5;
	private static final int ID_EDITRULES = 100;
	private static final int ID_EDITTREE = 101;
	private static final int ID_HELP = 102;
	private static final int ID_DONE = 200;
	private Minecraft b;
	private Screen parentScreen;
	private InvTweaksConfig config;

	public GuiInventorySettings(Minecraft mc, Screen parentScreen, InvTweaksConfig config) {
		this.b = mc;
		this.parentScreen = parentScreen;
		this.config = config;
	}

	public void init() {
		LinkedList controlList = new LinkedList();
		Point p = new Point();
		byte i = 0;
		this.moveToButtonCoords(1, p);
		controlList.add(new ButtonWidget(100, p.getX() + 55, this.height / 6 + 96, "Open the sorting rules file..."));
		controlList.add(new ButtonWidget(101, p.getX() + 55, this.height / 6 + 120, "Open the item tree file..."));
		controlList.add(new ButtonWidget(102, p.getX() + 55, this.height / 6 + 144, "Open help in browser..."));
		controlList.add(new ButtonWidget(200, p.getX() + 55, this.height / 6 + 168, "Done"));
		String middleClick = this.config.getProperty("enableMiddleClick");
		int i12 = i + 1;
		this.moveToButtonCoords(i, p);
		GuiTooltipButton middleClickBtn = new GuiTooltipButton(1, p.getX(), p.getY(), this.computeBooleanButtonLabel("enableMiddleClick", "Middle click"), "To sort using the middle click");
		controlList.add(middleClickBtn);
		if(middleClick.equals("convenientInventoryCompatibility")) {
			middleClickBtn.active = false;
			middleClickBtn.setTooltip(middleClickBtn.getTooltip() + "\n(In conflict with Convenient Inventory)");
		}

		this.moveToButtonCoords(i12++, p);
		controlList.add(new GuiTooltipButton(5, p.getX() + 130, p.getY(), 20, 20, "?", "Shortcuts help"));
		String shortcuts = this.config.getProperty("enableShortcuts");
		GuiTooltipButton shortcutsBtn = new GuiTooltipButton(4, p.getX(), p.getY(), 130, 20, this.computeBooleanButtonLabel("enableShortcuts", "Shortcuts"), "Enables various shortcuts\nto move items around");
		controlList.add(shortcutsBtn);
		if(shortcuts.equals("convenientInventoryCompatibility")) {
			shortcutsBtn.active = false;
			shortcutsBtn.setTooltip(shortcutsBtn.getTooltip() + "\n(In conflict with Convenient Inventory)");
		}

		this.moveToButtonCoords(i12++, p);
		GuiTooltipButton sortOnPickupBtn = new GuiTooltipButton(3, p.getX(), p.getY(), this.computeBooleanButtonLabel("enableSortingOnPickup", "Sort on pickup"), "Moves picked up items\nto the right slots");
		controlList.add(sortOnPickupBtn);
		if(this.b.isWorldRemote()) {
			sortOnPickupBtn.active = false;
			sortOnPickupBtn.text = "Sort on pickup: Only in SP";
			sortOnPickupBtn.setTooltip(sortOnPickupBtn.getTooltip() + "\n(Single player only)");
		}

		this.moveToButtonCoords(i12++, p);
		controlList.add(new GuiTooltipButton(2, p.getX(), p.getY(), this.computeBooleanButtonLabel("showChestButtons", "Chest buttons"), "Adds three buttons\non chests to sort them"));
		if(!Desktop.isDesktopSupported()) {
			Iterator i$ = controlList.iterator();

			label29:
			while(true) {
				ButtonWidget button;
				do {
					if(!i$.hasNext()) {
						break label29;
					}

					ButtonWidget o = (ButtonWidget)i$.next();
					button = (ButtonWidget)o;
				} while(button.id != 100 && button.id >= 101);

				button.active = false;
			}
		}

		this.buttons = controlList;
	}

	public void render(int i, int j, float f) {
		this.renderBackground();
		this.drawCenteredTextWithShadow(this.textRenderer, "InventoryTweaks settings", this.width / 2, 20, 0xFFFFFF);
		super.render(i, j, f);
	}

	protected void buttonClicked(ButtonWidget guibutton) {
		switch(guibutton.id) {
		case 1:
			this.toggleBooleanButton(guibutton, "enableMiddleClick", "Middle click");
			break;
		case 2:
			this.toggleBooleanButton(guibutton, "showChestButtons", "Chest buttons");
			break;
		case 3:
			this.toggleBooleanButton(guibutton, "enableSortingOnPickup", "Sort on pickup");
			break;
		case 4:
			this.toggleBooleanButton(guibutton, "enableShortcuts", "Shortcuts");
			break;
		case 5:
			this.b.setScreen(new GuiShortcutsHelp(this.b, this, this.config));
			break;
		case 100:
			try {
				Desktop.getDesktop().open(new File(Const.CONFIG_RULES_FILE));
			} catch (Exception exception5) {
				InvTweaks.logInGameErrorStatic("Failed to open rules file", exception5);
			}
			break;
		case 101:
			try {
				Desktop.getDesktop().open(new File(Const.CONFIG_TREE_FILE));
			} catch (Exception exception4) {
				InvTweaks.logInGameErrorStatic("Failed to open tree file", exception4);
			}
			break;
		case 102:
			try {
				Desktop.getDesktop().browse((new URL("http://wan.ka.free.fr/?invtweaks")).toURI());
			} catch (Exception exception3) {
				InvTweaks.logInGameErrorStatic("Failed to open help", exception3);
			}
			break;
		case 200:
			this.b.setScreen(this.parentScreen);
		}

	}

	private void moveToButtonCoords(int buttonOrder, Point p) {
		p.setX(this.width / 2 - 155 + (buttonOrder + 1) % 2 * 160);
		p.setY(this.height / 6 + buttonOrder / 2 * 24);
	}

	private void toggleBooleanButton(ButtonWidget guibutton, String property, String label) {
		boolean enabled = !Boolean.parseBoolean(this.config.getProperty(property));
		this.config.setProperty(property, String.valueOf(enabled));
		guibutton.text = this.computeBooleanButtonLabel(property, label);
	}

	private String computeBooleanButtonLabel(String property, String label) {
		String propertyValue = this.config.getProperty(property);
		if(propertyValue.equals("convenientInventoryCompatibility")) {
			return label + ": Disable CI";
		} else {
			boolean enabled = Boolean.parseBoolean(propertyValue);
			return label + (enabled ? ": ON" : ": OFF");
		}
	}
}
