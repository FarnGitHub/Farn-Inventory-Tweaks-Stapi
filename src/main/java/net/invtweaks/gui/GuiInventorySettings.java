package net.invtweaks.gui;

import java.awt.Desktop;
import java.io.File;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import net.invtweaks.Const;
import net.invtweaks.InvTweaks;
import net.invtweaks.config.InvTweaksConfig;
import net.minecraft.client.Minecraft;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import org.lwjgl.input.Keyboard;
import org.lwjgl.util.Point;

/**
 * The inventory and chest settings menu.
 * 
 * @author Jimeo Wan
 * 
 */
public class GuiInventorySettings extends Screen {

    @SuppressWarnings("unused")
    private static final Logger log = Logger.getLogger("InvTweaks");

    private final static String SCREEN_TITLE = "Inventory and chests settings";

    private final static String MIDDLE_CLICK = "Middle click";
    private final static String CHEST_BUTTONS = "Chest buttons";
    private final static String SORT_ON_PICKUP = "Sort on pickup";
    private final static String SHORTCUTS = "Shortcuts";
    private final static String ON = ": ON";
    private final static String OFF = ": OFF";
    private final static String DISABLE_CI = ": Disable CI";
    private final static String SP_ONLY = ": Only in SP";

    private final static int ID_MIDDLE_CLICK = 1;
    private final static int ID_CHESTS_BUTTONS = 2;
    private final static int ID_SORT_ON_PICKUP = 3;
    private final static int ID_SHORTCUTS = 4;
    private final static int ID_SHORTCUTS_HELP = 5;

    private final static int ID_EDITRULES = 100;
    private final static int ID_EDITTREE = 101;
    private final static int ID_SORT_KEY = 102;
    private final static int ID_DONE = 200;

    private Minecraft mc;
    private Screen parentScreen;
    private InvTweaksConfig config;

    private boolean selectSortingKey = false;
    private ButtonWidget sortingButton;

    public GuiInventorySettings(Minecraft mc, Screen parentScreen,
            InvTweaksConfig config) {
        this.mc = mc;
        this.parentScreen = parentScreen;
        this.config = config;
    }

    public void init() {

        List<ButtonWidget> controlList = new LinkedList<>();
        Point p = new Point();
        int i = 0;

        // Create large buttons

        moveToButtonCoords(1, p);
        controlList.add(new ButtonWidget(ID_EDITRULES, p.getX() + 55, height / 6 + 96, "Open the sorting rules file..."));
        controlList.add(new ButtonWidget(ID_EDITTREE, p.getX() + 55, height / 6 + 120, "Open the item tree file..."));
        controlList.add(sortingButton = new ButtonWidget(ID_SORT_KEY, p.getX() + 55, height / 6 + 144, computeSortKeyButtonLabel()));
        controlList.add(new ButtonWidget(ID_DONE, p.getX() + 55, height / 6 + 168, "Done"));

        // Create settings buttons

        moveToButtonCoords(i++, p);
        GuiTooltipButton middleClickBtn = new GuiTooltipButton(ID_MIDDLE_CLICK, p.getX(), p.getY(), computeBooleanButtonLabel(
                InvTweaksConfig.PROP_ENABLE_MIDDLE_CLICK, MIDDLE_CLICK), "To sort using the middle click");
        controlList.add(middleClickBtn);

        moveToButtonCoords(i++, p);
        controlList.add(new GuiTooltipButton(ID_SHORTCUTS_HELP, 
                p.getX() + 130, p.getY(), 20, 20, "?", "Shortcuts help"));
        GuiTooltipButton shortcutsBtn = new GuiTooltipButton(ID_SHORTCUTS, p.getX(), p.getY(), 130, 20, computeBooleanButtonLabel(
                InvTweaksConfig.PROP_ENABLE_SHORTCUTS, SHORTCUTS), "Enables various shortcuts\nto move items around");
        controlList.add(shortcutsBtn);
        
        moveToButtonCoords(i++, p);
        GuiTooltipButton sortOnPickupBtn = new GuiTooltipButton(ID_SORT_ON_PICKUP, p.getX(), p.getY(), computeBooleanButtonLabel(
                InvTweaksConfig.PROP_ENABLE_SORTING_ON_PICKUP, SORT_ON_PICKUP), "Moves picked up items\nto the right slots");
        controlList.add(sortOnPickupBtn);
        if (mc.isWorldRemote()) {
            // Sorting on pickup unavailable in SMP
            sortOnPickupBtn.active = false;
            sortOnPickupBtn.text = SORT_ON_PICKUP + SP_ONLY;
            sortOnPickupBtn.setTooltip(sortOnPickupBtn.getTooltip() + "\n(Single player only)");
        }

        moveToButtonCoords(i++, p);
        controlList.add(new GuiTooltipButton(ID_CHESTS_BUTTONS, p.getX(), p.getY(), computeBooleanButtonLabel(
                InvTweaksConfig.PROP_SHOW_CHEST_BUTTONS, CHEST_BUTTONS), "Adds three buttons\non chests to sort them"));

        // Check if links to files are supported, if not disable the buttons
        if (!Desktop.isDesktopSupported()) {
            for (Object o : controlList) {
                ButtonWidget button = (ButtonWidget) o;
                if (button.id < ID_EDITTREE) {
                    button.active = false;
                }
            }
        }

        // Save control list
        this.buttons = controlList;

    }

    public void render(int i, int j, float f) {
        renderBackground();
        drawCenteredTextWithShadow(textRenderer, SCREEN_TITLE, width / 2, 20, 0xffffff);
        super.render(i, j, f);
    }

    protected void keyPressed(char character, int keyCode) {
        super.keyPressed(character, keyCode);
        if(sortingButton != null && selectSortingKey) {
            selectSortingKey = false;
            Const.SORT_KEY_BINDING.code = keyCode;
            sortingButton.text = computeSortKeyButtonLabel();
        }
    }

    protected void buttonClicked(ButtonWidget guibutton) {

        switch (guibutton.id) {

        // Toggle middle click shortcut
        case ID_MIDDLE_CLICK:
            toggleBooleanButton(guibutton, InvTweaksConfig.PROP_ENABLE_MIDDLE_CLICK, MIDDLE_CLICK);
            break;

        // Toggle chest buttons&
        case ID_CHESTS_BUTTONS:
            toggleBooleanButton(guibutton, InvTweaksConfig.PROP_SHOW_CHEST_BUTTONS, CHEST_BUTTONS);
            break;

        // Toggle auto-refill sound
        case ID_SORT_ON_PICKUP:
            toggleBooleanButton(guibutton, InvTweaksConfig.PROP_ENABLE_SORTING_ON_PICKUP, SORT_ON_PICKUP);
            break;

        // Toggle shortcuts
        case ID_SHORTCUTS:
            toggleBooleanButton(guibutton, InvTweaksConfig.PROP_ENABLE_SHORTCUTS, SHORTCUTS);
            break;

        // Shortcuts help
        case ID_SHORTCUTS_HELP:
            mc.setScreen(new GuiShortcutsHelp(mc, this, config));
            break;

        // Open rules configuration in external editor
        case ID_EDITRULES:
            try {
                Desktop.getDesktop().open(new File(Const.CONFIG_RULES_FILE));
            } catch (Exception e) {
                InvTweaks.logInGameErrorStatic("Failed to open rules file", e);
            }
            break;

        // Open tree configuration in external editor
        case ID_EDITTREE:
            try {
                Desktop.getDesktop().open(new File(Const.CONFIG_TREE_FILE));
            } catch (Exception e) {
                InvTweaks.logInGameErrorStatic("Failed to open tree file", e);
            }
            break;

        // Open help in external editor
        case ID_SORT_KEY:
            selectSortingKey = true;
            guibutton.text = computeSortKeyButtonLabel();
            break;
        case ID_DONE:
            mc.setScreen(parentScreen);
        }

    }

    private void moveToButtonCoords(int buttonOrder, Point p) {
        p.setX(width / 2 - 155 + ((buttonOrder+1) % 2) * 160);
        p.setY(height / 6 + (buttonOrder / 2) * 24);
    }

    private void toggleBooleanButton(ButtonWidget guibutton, String property, String label) {
        boolean enabled = !config.getProperty(property).equalsIgnoreCase("true");
        config.setProperty(property, String.valueOf(enabled));
        guibutton.text = computeBooleanButtonLabel(property, label);
    }

    private String computeBooleanButtonLabel(String property, String label) {
        String propertyValue = config.getProperty(property);
        if (propertyValue.equals(InvTweaksConfig.VALUE_CI_COMPATIBILITY)) {
            return label + DISABLE_CI;
        } else {
            boolean enabled = propertyValue.equalsIgnoreCase("true");
            return label + ((enabled) ? ON : OFF);
        }
    }

    private String computeSortKeyButtonLabel() {
        String keyName = Keyboard.getKeyName(Const.SORT_KEY_BINDING.code);
        if(selectSortingKey) keyName = "> " + keyName + " <";
        return "Sorting Key: " + keyName;
    }

}
