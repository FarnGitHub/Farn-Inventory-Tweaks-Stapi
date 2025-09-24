package net.invtweaks;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;

import farn.invtweaksStapi.InvTweaksStapi;
import net.invtweaks.config.InvTweaksConfig;
import net.invtweaks.config.InvTweaksConfigManager;
import net.invtweaks.config.SortingRule;
import net.invtweaks.gui.GuiInventorySettingsButton;
import net.invtweaks.gui.GuiSortingButton;
import net.invtweaks.library.ContainerManager;
import net.invtweaks.library.ContainerSectionManager;
import net.invtweaks.library.Obfuscation;
import net.invtweaks.logic.SortingHandler;
import farn.invtweaksStapi.mixin.ContainerScreenAccessor;
import farn.invtweaksStapi.mixin.ScreenAccessor;
import net.invtweaks.tree.ItemTree;
import net.minecraft.client.Minecraft;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.ingame.SignEditScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import org.apache.logging.log4j.Logger;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public class InvTweaks extends Obfuscation {
	private static final Logger log = InvTweaksStapi.LOGGER;
	private static InvTweaks instance;
	public InvTweaksConfigManager cfgManager = null;
	private int chestAlgorithm = 0;
	private long chestAlgorithmClickTimestamp = 0L;
	private boolean chestAlgorithmButtonDown = false;
	private long sortingKeyPressedDate = 0L;
	private int storedStackId = 0;
	private int storedStackDamage = -1;
	private int storedFocusedSlot = -1;
	private ItemStack[] hotbarClone = new ItemStack[9];
	private boolean mouseWasInWindow = true;
	private boolean mouseWasDown = false;
	private int tickNumber = 0;
	private int lastPollingTickNumber = -3;

	public InvTweaks(Minecraft mc) {
		super(mc);
		instance = this;
		this.cfgManager = new InvTweaksConfigManager(mc);
		if(this.cfgManager.makeSureConfigurationIsLoaded()) {
			log.info("Mod initialized");
		} else {
			log.error("Mod failed to initialize!");
		}

	}

	public final void onSortingKeyPressed() {
		synchronized(this) {
			if(this.cfgManager.makeSureConfigurationIsLoaded()) {
				Screen guiScreen = this.getCurrentScreen();
				if(guiScreen == null || guiScreen instanceof HandledScreen) {
					this.handleSorting((HandledScreen)guiScreen);
				}
			}
		}
	}

	public void onItemPickup() {
		if(this.cfgManager.makeSureConfigurationIsLoaded()) {
			InvTweaksConfig config = this.cfgManager.getConfig();
			if(!this.cfgManager.getConfig().getProperty("enableSortingOnPickup").equals("false")) {
				try {
					ContainerSectionManager e = new ContainerSectionManager(this.mc, ContainerManager.ContainerSection.INVENTORY);
					int currentSlot = -1;

					do {
						if(this.isMultiplayerWorld() && currentSlot == -1) {
							try {
								Thread.sleep(3L);
							} catch (InterruptedException interruptedException14) {
							}
						}

						for(int prefferedPositions = 0; prefferedPositions < 9; ++prefferedPositions) {
							ItemStack tree = e.getItemStack(prefferedPositions + 27);
							if(tree != null && tree.bobbingAnimationTime == 5 && this.hotbarClone[prefferedPositions] == null) {
								currentSlot = prefferedPositions + 27;
							}
						}
					} while(this.isMultiplayerWorld() && currentSlot == -1);

					if(currentSlot != -1) {
						LinkedList linkedList17 = new LinkedList();
						ItemTree itemTree18 = config.getTree();
						ItemStack stack = e.getItemStack(currentSlot);
						List items = itemTree18.getItems(this.getItemID(stack), this.getItemDamage(stack));
						Iterator i = config.getRules().iterator();

						while(true) {
							SortingRule hasToBeMoved;
							do {
								if(!i.hasNext()) {
									boolean z19 = true;
									Iterator iterator21 = linkedList17.iterator();

									int i20;
									while(iterator21.hasNext()) {
										i20 = ((Integer)iterator21.next()).intValue();

										try {
											if(i20 == currentSlot) {
												z19 = false;
												break;
											}

											if(e.getItemStack(i20) == null && e.move(currentSlot, i20)) {
												break;
											}
										} catch (TimeoutException timeoutException15) {
											this.logInGameError("Failed to move picked up stack", timeoutException15);
										}
									}

									if(z19) {
										for(i20 = 0; i20 < e.getSize() && (e.getItemStack(i20) != null || !e.move(currentSlot, i20)); ++i20) {
										}
									}

									return;
								}

								hasToBeMoved = (SortingRule)i.next();
							} while(!itemTree18.matches(items, hasToBeMoved.getKeyword()));

							int[] i13;
							int i12 = (i13 = hasToBeMoved.getPreferredSlots()).length;

							for(int e1 = 0; e1 < i12; ++e1) {
								int slot = i13[e1];
								linkedList17.add(slot);
							}
						}
					}
				} catch (Exception exception16) {
					this.logInGameError("Failed to move picked up stack", exception16);
				}

			}
		}
	}

	public void onTickInGame() {
		synchronized(this) {
			if(this.onTick()) {
				this.handleAutoRefill();
			}
		}
	}

	public void onTickInGUI(Screen guiScreen) {
		synchronized(this) {
			if(this.onTick()) {
				if(this.isTimeForPolling()) {
					this.unlockKeysIfNecessary();
				}

				this.handleGUILayout(guiScreen);
				this.handleMiddleClick(guiScreen);
				this.handleShortcuts(guiScreen);
			}
		}
	}

	public void logInGame(String message) {
		String formattedMsg = this.buildlogString(Level.INFO, message);
		this.addChatMessage(formattedMsg);
		log.info(formattedMsg);
	}

	public void logInGameError(String message, Exception e) {
		String formattedMsg = this.buildlogString(Level.SEVERE, message, e);
		this.addChatMessage(formattedMsg);
		log.error(formattedMsg);
		e.printStackTrace();
	}

	public static void logInGameStatic(String message) {
		getInstance().logInGame(message);
	}

	public static void logInGameErrorStatic(String message, Exception e) {
		getInstance().logInGameError(message, e);
	}

	public static InvTweaks getInstance() {
		return instance;
	}

	public static boolean getIsMouseOverSlot(HandledScreen guiContainer, Slot slot, int i, int j) {
		int k = (guiContainer.width - ((ContainerScreenAccessor)guiContainer).bgWidths()) / 2;
		int l = (guiContainer.height - ((ContainerScreenAccessor)guiContainer).bgHeights()) / 2;
		i -= k;
		j -= l;
		return i >= slot.x - 1 && i < slot.x + 16 + 1 && j >= slot.y - 1 && j < slot.y + 16 + 1;
	}

	private boolean onTick() {
		++this.tickNumber;
		InvTweaksConfig config = this.cfgManager.getConfig();
		if(config == null) {
			return false;
		} else {
			Screen currentScreen = this.getCurrentScreen();
			if(currentScreen == null || currentScreen instanceof InventoryScreen) {
				this.cloneHotbar();
			}

			if(Keyboard.isKeyDown(this.getKeycode(Const.SORT_KEY_BINDING))) {
				long currentTime = System.currentTimeMillis();
				if(this.sortingKeyPressedDate == 0L) {
					this.sortingKeyPressedDate = currentTime;
				} else if(currentTime - this.sortingKeyPressedDate > 1000L) {
					String previousRuleset = config.getCurrentRulesetName();
					String newRuleset = config.switchConfig();
					if(newRuleset == null) {
						this.logInGameError("Failed to switch the configuration", (Exception)null);
					} else if(!previousRuleset.equals(newRuleset)) {
						this.logInGame("\'" + newRuleset + "\' enabled");
						this.handleSorting(currentScreen);
					}

					this.sortingKeyPressedDate = currentTime;
				}
			} else {
				this.sortingKeyPressedDate = 0L;
			}

			return true;
		}
	}

	private void handleSorting(Screen guiScreen) {
		ItemStack selectedItem = this.getMainInventory()[this.getFocusedSlot()];
		InvTweaksConfig config = this.cfgManager.getConfig();
		Vector downKeys = this.cfgManager.getShortcutsHandler().getDownShortcutKeys();
		if(Keyboard.isKeyDown(Const.SORT_KEY_BINDING.code)) {
			Iterator iterator6 = downKeys.iterator();

			while(iterator6.hasNext()) {
				int e = ((Integer)iterator6.next()).intValue();
				String newRuleset = null;
				switch(e) {
				case 2:
				case 79:
					newRuleset = config.switchConfig(0);
					break;
				case 3:
				case 80:
					newRuleset = config.switchConfig(1);
					break;
				case 4:
				case 81:
					newRuleset = config.switchConfig(2);
					break;
				case 5:
				case 75:
					newRuleset = config.switchConfig(3);
					break;
				case 6:
				case 76:
					newRuleset = config.switchConfig(4);
					break;
				case 7:
				case 77:
					newRuleset = config.switchConfig(5);
					break;
				case 8:
				case 71:
					newRuleset = config.switchConfig(6);
					break;
				case 9:
				case 72:
					newRuleset = config.switchConfig(7);
					break;
				case 10:
				case 73:
					newRuleset = config.switchConfig(8);
				}

			}
		}

		try {
			(new SortingHandler(this.mc, this.cfgManager.getConfig(), ContainerManager.ContainerSection.INVENTORY, 3)).sort();
		} catch (Exception exception8) {
			this.logInGame("Failed to sort inventory: " + exception8.getMessage());
		}

		this.playClick();
		if(selectedItem != null && this.getMainInventory()[this.getFocusedSlot()] == null) {
			this.storedStackId = 0;
		}

	}

	private void handleAutoRefill() {
		ItemStack currentStack = this.getFocusedStack();
		int currentStackId = currentStack == null ? 0 : this.getItemID(currentStack);
		int currentStackDamage = currentStack == null ? 0 : this.getItemDamage(currentStack);
		int focusedSlot = this.getFocusedSlot() + 27;
		InvTweaksConfig config = this.cfgManager.getConfig();
		if(currentStackId != this.storedStackId || currentStackDamage != this.storedStackDamage) {
			if(this.storedFocusedSlot != focusedSlot) {
				this.storedFocusedSlot = focusedSlot;
			} else if((currentStack == null || this.getItemID(currentStack) == 281 && this.storedStackId == 282) && (this.getCurrentScreen() == null || this.getCurrentScreen() instanceof SignEditScreen) && config.isAutoRefillEnabled(this.storedStackId, this.storedStackId)) {
				try {
					this.cfgManager.getAutoRefillHandler().autoRefillSlot(focusedSlot, this.storedStackId, this.storedStackDamage);
				} catch (Exception exception7) {
					this.logInGameError("Failed to trigger auto-refill", exception7);
				}
			}
		}

		this.storedStackId = currentStackId;
		this.storedStackDamage = currentStackDamage;
	}

	private void handleMiddleClick(Screen guiScreen) {
		if(Mouse.isButtonDown(2)) {
			if(!this.cfgManager.makeSureConfigurationIsLoaded()) {
				return;
			}

			InvTweaksConfig config = this.cfgManager.getConfig();
			if(config.getProperty("enableMiddleClick").equals("true") && !this.chestAlgorithmButtonDown) {
				this.chestAlgorithmButtonDown = true;
				if(this.isChestOrDispenser(guiScreen)) {
					HandledScreen guiContainer = (HandledScreen)guiScreen;
					ScreenHandler container = this.getContainer((HandledScreen)guiScreen);
					int slotCount = this.getSlots(container).size();
					int mouseX = Mouse.getEventX() * guiContainer.width / this.mc.displayWidth;
					int mouseY = guiContainer.height - Mouse.getEventY() * guiContainer.height / this.mc.displayHeight - 1;
					int target = 0;

					for(int timestamp = 0; timestamp < slotCount; ++timestamp) {
						Slot slot = this.getSlot(container, timestamp);
						int e = (guiContainer.width - ((ContainerScreenAccessor)guiContainer).bgWidths()) / 2;
						int l = (guiContainer.height - ((ContainerScreenAccessor)guiContainer).bgHeights()) / 2;
						if(mouseX - e >= slot.x - 1 && mouseX - e < slot.x + 16 + 1 && mouseY - l >= slot.y - 1 && mouseY - l < slot.y + 16 + 1) {
							target = timestamp < slotCount - 36 ? 1 : 2;
							break;
						}
					}

					if(target == 1) {
						this.mc.world.playSound(this.getThePlayer(), "random.click", 0.2F, 1.8F);
						long j14 = System.currentTimeMillis();
						if(j14 - this.chestAlgorithmClickTimestamp > 3000L) {
							this.chestAlgorithm = 0;
						}

						try {
							(new SortingHandler(this.mc, this.cfgManager.getConfig(), ContainerManager.ContainerSection.CHEST, this.chestAlgorithm)).sort();
						} catch (Exception exception13) {
							this.logInGameError("Failed to sort container", exception13);
						}

						this.chestAlgorithm = (this.chestAlgorithm + 1) % 3;
						this.chestAlgorithmClickTimestamp = j14;
					} else if(target == 2) {
						this.handleSorting(guiScreen);
					}
				} else {
					this.handleSorting(guiScreen);
				}
			}
		} else {
			this.chestAlgorithmButtonDown = false;
		}

	}

	private void handleGUILayout(Screen guiScreen) {
		InvTweaksConfig config = this.cfgManager.getConfig();
		boolean isContainer = this.isChestOrDispenser(guiScreen);
		if(isContainer || guiScreen instanceof InventoryScreen || guiScreen.getClass().getSimpleName().equals("GuiInventoryMoreSlots")) {
			byte w = 10;
			byte h = 10;
			boolean customButtonsAdded = false;
			List<ButtonWidget> buttons = ((ScreenAccessor)guiScreen).getButtons();
			Iterator id = ((ScreenAccessor)guiScreen).getButtons().iterator();

			while(id.hasNext()) {
				Object guiContainer = id.next();
				ButtonWidget x = (ButtonWidget) guiContainer;
				if(x.id == 54696386) {
					customButtonsAdded = true;
					break;
				}
			}

			if(!customButtonsAdded) {
				if(!isContainer) {
					buttons.add(new GuiInventorySettingsButton(this.cfgManager, 54696386, guiScreen.width / 2 + 73, guiScreen.height / 2 - 78, w, h, "...", "Inventory settings"));
				} else {
					HandledScreen guiContainer12 = (HandledScreen)guiScreen;
					int i13 = 54696386;
					int i14 = ((ContainerScreenAccessor)guiContainer12).bgWidths() / 2 + guiContainer12.width / 2 - 17;
					int y = (guiContainer12.height - ((ContainerScreenAccessor)guiContainer12).bgHeights()) / 2 + 5;
					buttons.add(new GuiInventorySettingsButton(this.cfgManager, i13++, i14 - 1, y, w, h, "...", "Inventory settings"));
					if(!config.getProperty("showChestButtons").equals("false")) {
						GuiSortingButton button = new GuiSortingButton(this.cfgManager, i13++, i14 - 13, y, w, h, "h", "Sort in rows", 2);
						buttons.add(button);
						button = new GuiSortingButton(this.cfgManager, i13++, i14 - 25, y, w, h, "v", "Sort in columns", 1);
						buttons.add(button);
						button = new GuiSortingButton(this.cfgManager, i13++, i14 - 37, y, w, h, "s", "Default sorting", 0);
						buttons.add(button);
					}
				}
			}
		}

	}

	private void handleShortcuts(Screen guiScreen) {
		if(guiScreen instanceof HandledScreen && !guiScreen.getClass().getSimpleName().equals("MLGuiChestBuilding")) {
			if(!Mouse.isButtonDown(0) && !Mouse.isButtonDown(1)) {
				this.mouseWasDown = false;
			} else if(!this.mouseWasDown) {
				this.mouseWasDown = true;
				if(this.cfgManager.getConfig().getProperty("enableShortcuts").equals("true")) {
					this.cfgManager.getShortcutsHandler().handleShortcut((HandledScreen)guiScreen);
				}
			}

		}
	}

	private boolean isTimeForPolling() {
		if(this.tickNumber - this.lastPollingTickNumber >= 3) {
			this.lastPollingTickNumber = this.tickNumber;
		}

		return this.tickNumber - this.lastPollingTickNumber == 0;
	}

	private void unlockKeysIfNecessary() {
		boolean mouseInWindow = Mouse.isInsideWindow();
		if(!this.mouseWasInWindow && mouseInWindow) {
			Keyboard.destroy();
			boolean firstTry = true;

			while(!Keyboard.isCreated()) {
				try {
					Keyboard.create();
				} catch (LWJGLException lWJGLException4) {
					if(firstTry) {
						this.logInGameError("I\'m having troubles with the keyboard: ", lWJGLException4);
						firstTry = false;
					}
				}
			}

			if(!firstTry) {
				this.logInGame("Ok it\'s repaired, sorry about that.");
			}
		}

		this.mouseWasInWindow = mouseInWindow;
	}

	private void cloneHotbar() {
		ItemStack[] mainInventory = this.getMainInventory();

		for(int i = 0; i < 9; ++i) {
			if(mainInventory[i] != null) {
				this.hotbarClone[i] = mainInventory[i].copy();
			} else {
				this.hotbarClone[i] = null;
			}
		}

	}

	private void playClick() {
		if(!this.cfgManager.getConfig().getProperty("enableSortingSound").equals("false")) {
			this.mc.world.playSound(this.getThePlayer(), "random.click", 0.2F, 1.8F);
		}

	}

	private String buildlogString(Level level, String message, Exception e) {
		return e != null ? this.buildlogString(level, message) + ": " + e.getMessage() : this.buildlogString(level, message) + ": (unknown error)";
	}

	private String buildlogString(Level level, String message) {
		return "InvTweaks: " + (level.equals(Level.SEVERE) ? "[ERROR] " : "") + message;
	}
}
