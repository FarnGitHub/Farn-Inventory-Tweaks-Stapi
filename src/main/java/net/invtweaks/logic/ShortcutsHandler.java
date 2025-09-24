package net.invtweaks.logic;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import net.invtweaks.InvTweaks;
import net.invtweaks.config.InvTweaksConfig;
import net.invtweaks.library.ContainerManager;
import net.invtweaks.library.Obfuscation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public class ShortcutsHandler extends Obfuscation {
	private static final int DROP_SLOT = -999;
	private ShortcutType defaultAction = ShortcutType.MOVE_ONE_STACK;
	private ShortcutType defaultDestination = null;
	private InvTweaksConfig config;
	private ContainerManager container;
	private ContainerManager.ContainerSection fromSection;
	private int fromIndex;
	private ItemStack fromStack;
	private ContainerManager.ContainerSection toSection;
	private ShortcutType shortcutType;
	private Map shortcutKeysStatus;
	private Map shortcuts;

	public ShortcutsHandler(Minecraft mc, InvTweaksConfig config) {
		super(mc);
		this.config = config;
		this.reset();
	}

	public void reset() {
		this.shortcutKeysStatus = new HashMap();
		this.shortcuts = new HashMap();
		Map keys = this.config.getProperties("shortcutKey");
		Iterator upKeyCode = keys.keySet().iterator();

		while(true) {
			ShortcutType shortcutsHandler$ShortcutType14;
			label64:
			do {
				while(true) {
					int i$;
					int i;
					while(upKeyCode.hasNext()) {
						String downKeyCode = (String)upKeyCode.next();
						String keyBindings = (String)keys.get(downKeyCode);
						if(keyBindings.equals(InvTweaksConfig.VALUE_DEFAULT)) {
							shortcutsHandler$ShortcutType14 = this.propNameToShortcutType(downKeyCode);
							if(shortcutsHandler$ShortcutType14 != ShortcutType.MOVE_ALL_ITEMS && shortcutsHandler$ShortcutType14 != ShortcutType.MOVE_ONE_ITEM && shortcutsHandler$ShortcutType14 != ShortcutType.MOVE_ONE_STACK) {
								continue label64;
							}

							this.defaultAction = shortcutsHandler$ShortcutType14;
						} else {
							String[] hotbarKeys = ((String)keys.get(downKeyCode)).split("[ ]*,[ ]*");
							LinkedList arr$ = new LinkedList();
							String[] len$ = hotbarKeys;
							i$ = hotbarKeys.length;

							for(i = 0; i < i$; ++i) {
								String keyName = len$[i];
								arr$.add(Keyboard.getKeyIndex(keyName.replace("KEY_", "").replace("ALT", "MENU")));
							}

							ShortcutType shortcutsHandler$ShortcutType17 = this.propNameToShortcutType(downKeyCode);
							if(shortcutsHandler$ShortcutType17 != null) {
								this.shortcuts.put(shortcutsHandler$ShortcutType17, arr$);
							}

							Iterator iterator19 = arr$.iterator();

							while(iterator19.hasNext()) {
								Integer integer20 = (Integer)iterator19.next();
								this.shortcutKeysStatus.put(integer20, false);
							}
						}
					}

					int i11 = this.mc.options.forwardKey.code;
					int i12 = this.mc.options.backKey.code;
					((List)this.shortcuts.get(ShortcutType.MOVE_UP)).add(i11);
					((List)this.shortcuts.get(ShortcutType.MOVE_DOWN)).add(i12);
					this.shortcutKeysStatus.put(i11, false);
					this.shortcutKeysStatus.put(i12, false);
					LinkedList linkedList13 = new LinkedList();
					int[] i15 = new int[]{2, 3, 4, 5, 6, 7, 8, 9, 10, 79, 80, 81, 75, 76, 77, 71, 72, 73};
					int[] i16 = i15;
					int i18 = i15.length;

					for(i$ = 0; i$ < i18; ++i$) {
						i = i16[i$];
						linkedList13.add(i);
						this.shortcutKeysStatus.put(i, false);
					}

					this.shortcuts.put(ShortcutType.MOVE_TO_SPECIFIC_HOTBAR_SLOT, linkedList13);
					return;
				}
			} while(shortcutsHandler$ShortcutType14 != ShortcutType.MOVE_DOWN && shortcutsHandler$ShortcutType14 != ShortcutType.MOVE_UP);

			this.defaultDestination = shortcutsHandler$ShortcutType14;
		}
	}

	public Vector getDownShortcutKeys() {
		this.updateKeyStatuses();
		Vector downShortcutKeys = new Vector();
		Iterator i$ = this.shortcutKeysStatus.keySet().iterator();

		while(i$.hasNext()) {
			Integer key = (Integer)i$.next();
			if(((Boolean)this.shortcutKeysStatus.get(key)).booleanValue()) {
				downShortcutKeys.add(key);
			}
		}

		return downShortcutKeys;
	}

	public void handleShortcut(HandledScreen guiScreen) {
		this.updateKeyStatuses();
		int ex = Mouse.getEventX();
		int ey = Mouse.getEventY();
		int x = ex * guiScreen.width / this.mc.displayWidth;
		int y = guiScreen.height - ey * guiScreen.height / this.mc.displayHeight - 1;
		boolean shortcutValid = false;
		Slot slot = this.getSlotAtPosition(guiScreen, x, y);
		if(slot != null && slot.hasStack()) {
			ShortcutType shortcutType = this.defaultAction;
			if(this.isActive(ShortcutType.MOVE_TO_SPECIFIC_HOTBAR_SLOT) != -1) {
				shortcutType = ShortcutType.MOVE_TO_SPECIFIC_HOTBAR_SLOT;
				shortcutValid = true;
			}

			if(this.isActive(ShortcutType.MOVE_ALL_ITEMS) != -1) {
				shortcutType = ShortcutType.MOVE_ALL_ITEMS;
				shortcutValid = true;
			} else if(this.isActive(ShortcutType.MOVE_ONE_ITEM) != -1) {
				shortcutType = ShortcutType.MOVE_ONE_ITEM;
				shortcutValid = true;
			}

			try {
				ContainerManager e = new ContainerManager(this.mc);
				ContainerManager.ContainerSection srcSection = e.getSlotSection(slot.id);
				ContainerManager.ContainerSection destSection = null;
				Vector availableSections = new Vector();
				if(e.hasSection(ContainerManager.ContainerSection.CHEST)) {
					availableSections.add(ContainerManager.ContainerSection.CHEST);
				} else if(e.hasSection(ContainerManager.ContainerSection.CRAFTING_IN)) {
					availableSections.add(ContainerManager.ContainerSection.CRAFTING_IN);
				} else if(e.hasSection(ContainerManager.ContainerSection.FURNACE_IN)) {
					availableSections.add(ContainerManager.ContainerSection.FURNACE_IN);
				}

				availableSections.add(ContainerManager.ContainerSection.INVENTORY_NOT_HOTBAR);
				availableSections.add(ContainerManager.ContainerSection.INVENTORY_HOTBAR);
				byte destinationModifier = 0;
				if(this.isActive(ShortcutType.MOVE_UP) == -1 && this.defaultDestination != ShortcutType.MOVE_UP) {
					if(this.isActive(ShortcutType.MOVE_DOWN) != -1 || this.defaultDestination == ShortcutType.MOVE_DOWN) {
						destinationModifier = 1;
					}
				} else {
					destinationModifier = -1;
				}

				if(destinationModifier == 0) {
					switch(SyntheticClass_1.$SwitchMap$net$invtweaks$library$ContainerManager$ContainerSection[srcSection.ordinal()]) {
					case 1:
						destSection = ContainerManager.ContainerSection.INVENTORY_NOT_HOTBAR;
						break;
					case 2:
					case 3:
						destSection = ContainerManager.ContainerSection.INVENTORY_NOT_HOTBAR;
						break;
					default:
						destSection = ContainerManager.ContainerSection.INVENTORY_HOTBAR;
					}
				} else {
					shortcutValid = true;
					int keyName = availableSections.indexOf(srcSection);
					if(keyName != -1) {
						destSection = (ContainerManager.ContainerSection)availableSections.get((availableSections.size() + keyName + destinationModifier) % availableSections.size());
					} else {
						destSection = ContainerManager.ContainerSection.INVENTORY;
					}
				}

				if(srcSection == ContainerManager.ContainerSection.UNKNOWN) {
					shortcutValid = false;
				}

				if(shortcutValid || this.isActive(ShortcutType.DROP) != -1) {
					this.initAction(slot.id, shortcutType, destSection);
					if(shortcutType == ShortcutType.MOVE_TO_SPECIFIC_HOTBAR_SLOT) {
						String keyName1 = Keyboard.getKeyName(this.isActive(ShortcutType.MOVE_TO_SPECIFIC_HOTBAR_SLOT));
						int destIndex = -1 + Integer.parseInt(keyName1.replace("NUMPAD", ""));
						e.move(this.fromSection, this.fromIndex, ContainerManager.ContainerSection.INVENTORY_HOTBAR, destIndex);
					} else if(srcSection == ContainerManager.ContainerSection.CRAFTING_OUT) {
						this.craftAll(Mouse.isButtonDown(1), this.isActive(ShortcutType.DROP) != -1);
					} else {
						this.move(Mouse.isButtonDown(1), this.isActive(ShortcutType.DROP) != -1);
					}

					Mouse.destroy();
					Mouse.create();
					Mouse.setCursorPosition(ex, ey);
				}
			} catch (Exception exception16) {
				InvTweaks.logInGameErrorStatic("Failed to trigger shortcut", exception16);
			}
		}

	}

	private void move(boolean separateStacks, boolean drop) throws Exception {
		boolean toIndex = true;
		synchronized(this) {
			int toIndex1 = this.getNextIndex(separateStacks, drop);
			if(toIndex1 != -1) {
				switch(SyntheticClass_1.$SwitchMap$net$invtweaks$logic$ShortcutsHandler$ShortcutType[this.shortcutType.ordinal()]) {
				case 1:
					for(Slot i$1 = this.container.getSlot(this.fromSection, this.fromIndex); i$1.hasStack() && toIndex1 != -1; toIndex1 = this.getNextIndex(separateStacks, drop)) {
						this.container.move(this.fromSection, this.fromIndex, this.toSection, toIndex1);
					}

					return;
				case 2:
					this.container.moveSome(this.fromSection, this.fromIndex, this.toSection, toIndex1, 1);
					break;
				case 3:
					Iterator i$ = this.container.getSlots(this.fromSection).iterator();

					while(true) {
						Slot slot;
						do {
							do {
								if(!i$.hasNext()) {
									return;
								}

								slot = (Slot)i$.next();
							} while(!slot.hasStack());
						} while(!this.areSameItemType(this.fromStack, slot.getStack()));

						for(int fromIndex = this.container.getSlotIndex(slot.id); slot.hasStack() && toIndex1 != -1 && (this.fromSection != this.toSection || fromIndex != toIndex1); toIndex1 = this.getNextIndex(separateStacks, drop)) {
							boolean moveResult = this.container.move(this.fromSection, fromIndex, this.toSection, toIndex1);
							if(!moveResult) {
								break;
							}
						}
					}
				}
			}

		}
	}

	private void craftAll(boolean separateStacks, boolean drop) throws Exception {
		int toIndex = this.getNextIndex(separateStacks, drop);
		Slot slot = this.container.getSlot(this.fromSection, this.fromIndex);
		if(slot.hasStack()) {
			int idToCraft = this.getItemID(slot.getStack());

			do {
				this.container.move(this.fromSection, this.fromIndex, this.toSection, toIndex);
				toIndex = this.getNextIndex(separateStacks, drop);
				if(this.getHoldStack() != null) {
					this.container.leftClick(this.toSection, toIndex);
					toIndex = this.getNextIndex(separateStacks, drop);
				}
			} while(slot.hasStack() && this.getItemID(slot.getStack()) == idToCraft && toIndex != -1);
		}

	}

	private boolean haveControlsChanged() {
		return !this.shortcutKeysStatus.containsKey(this.mc.options.forwardKey.code) || !this.shortcutKeysStatus.containsKey(this.mc.options.backKey.code);
	}

	private void updateKeyStatuses() {
		if(this.haveControlsChanged()) {
			this.reset();
		}

		Iterator i$ = this.shortcutKeysStatus.keySet().iterator();

		while(i$.hasNext()) {
			int keyCode = ((Integer)i$.next()).intValue();
			if(Keyboard.isKeyDown(keyCode)) {
				if(!((Boolean)this.shortcutKeysStatus.get(keyCode)).booleanValue()) {
					this.shortcutKeysStatus.put(keyCode, true);
				}
			} else {
				this.shortcutKeysStatus.put(keyCode, false);
			}
		}

	}

	private int getNextIndex(boolean emptySlotOnly, boolean drop) {
		if(drop) {
			return -999;
		} else {
			int result = -1;
			if(!emptySlotOnly) {
				int i = 0;

				for(Iterator i$ = this.container.getSlots(this.toSection).iterator(); i$.hasNext(); ++i) {
					Slot slot = (Slot)i$.next();
					if(slot.hasStack()) {
						ItemStack stack = slot.getStack();
						if(stack.isItemEqual(this.fromStack) && this.getStackSize(stack) < this.getMaxStackSize(stack)) {
							result = i;
							break;
						}
					}
				}
			}

			if(result == -1) {
				result = this.container.getFirstEmptyIndex(this.toSection);
			}

			if(result == -1 && this.toSection == ContainerManager.ContainerSection.FURNACE_IN) {
				this.toSection = ContainerManager.ContainerSection.FURNACE_FUEL;
				result = this.container.getFirstEmptyIndex(this.toSection);
			}

			return result;
		}
	}

	private int isActive(ShortcutType shortcutType) {
		Iterator i$ = ((List)this.shortcuts.get(shortcutType)).iterator();

		Integer keyCode;
		do {
			do {
				if(!i$.hasNext()) {
					return -1;
				}

				keyCode = (Integer)i$.next();
			} while(!((Boolean)this.shortcutKeysStatus.get(keyCode)).booleanValue());
		} while(keyCode.intValue() == 29 && Keyboard.isKeyDown(Keyboard.KEY_RMENU));

		return keyCode.intValue();
	}

	private void initAction(int fromSlot, ShortcutType shortcutType, ContainerManager.ContainerSection destSection) throws Exception {
		this.container = new ContainerManager(this.mc);
		this.fromSection = this.container.getSlotSection(fromSlot);
		this.fromIndex = this.container.getSlotIndex(fromSlot);
		this.fromStack = this.container.getItemStack(this.fromSection, this.fromIndex);
		this.shortcutType = shortcutType;
		this.toSection = destSection;
		if(this.getHoldStack() != null) {
			this.container.leftClick(this.fromSection, this.fromIndex);
			if(this.getHoldStack() != null) {
				int firstEmptyIndex = this.container.getFirstEmptyIndex(ContainerManager.ContainerSection.INVENTORY);
				if(firstEmptyIndex == -1) {
					throw new Exception("Couldn\'t put hold item down");
				}

				this.fromSection = ContainerManager.ContainerSection.INVENTORY;
				this.container.leftClick(this.fromSection, firstEmptyIndex);
			}
		}

	}

	private Slot getSlotAtPosition(HandledScreen guiContainer, int i, int j) {
		for(int k = 0; k < guiContainer.container.slots.size(); ++k) {
			Slot slot = (Slot)guiContainer.container.slots.get(k);
			if(InvTweaks.getIsMouseOverSlot(guiContainer, slot, i, j)) {
				return slot;
			}
		}

		return null;
	}

	private ShortcutType propNameToShortcutType(String property) {
		return property.equals("shortcutKeyAllItems") ? ShortcutType.MOVE_ALL_ITEMS : (property.equals("shortcutKeyToLowerSection") ? ShortcutType.MOVE_DOWN : (property.equals("shortcutKeyDrop") ? ShortcutType.DROP : (property.equals("shortcutKeyOneItem") ? ShortcutType.MOVE_ONE_ITEM : (property.equals("shortcutKeyOneStack") ? ShortcutType.MOVE_ONE_STACK : (property.equals("shortcutKeyToUpperSection") ? ShortcutType.MOVE_UP : null)))));
	}

	static class SyntheticClass_1 {
		static final int[] $SwitchMap$net$invtweaks$library$ContainerManager$ContainerSection;
		static final int[] $SwitchMap$net$invtweaks$logic$ShortcutsHandler$ShortcutType = new int[ShortcutType.values().length];

		static {
			try {
				$SwitchMap$net$invtweaks$logic$ShortcutsHandler$ShortcutType[ShortcutType.MOVE_ONE_STACK.ordinal()] = 1;
			} catch (NoSuchFieldError noSuchFieldError6) {
			}

			try {
				$SwitchMap$net$invtweaks$logic$ShortcutsHandler$ShortcutType[ShortcutType.MOVE_ONE_ITEM.ordinal()] = 2;
			} catch (NoSuchFieldError noSuchFieldError5) {
			}

			try {
				$SwitchMap$net$invtweaks$logic$ShortcutsHandler$ShortcutType[ShortcutType.MOVE_ALL_ITEMS.ordinal()] = 3;
			} catch (NoSuchFieldError noSuchFieldError4) {
			}

			$SwitchMap$net$invtweaks$library$ContainerManager$ContainerSection = new int[ContainerManager.ContainerSection.values().length];

			try {
				$SwitchMap$net$invtweaks$library$ContainerManager$ContainerSection[ContainerManager.ContainerSection.INVENTORY_HOTBAR.ordinal()] = 1;
			} catch (NoSuchFieldError noSuchFieldError3) {
			}

			try {
				$SwitchMap$net$invtweaks$library$ContainerManager$ContainerSection[ContainerManager.ContainerSection.CRAFTING_IN.ordinal()] = 2;
			} catch (NoSuchFieldError noSuchFieldError2) {
			}

			try {
				$SwitchMap$net$invtweaks$library$ContainerManager$ContainerSection[ContainerManager.ContainerSection.FURNACE_IN.ordinal()] = 3;
			} catch (NoSuchFieldError noSuchFieldError1) {
			}

		}
	}

	private static enum ShortcutType {
		MOVE_TO_SPECIFIC_HOTBAR_SLOT,
		MOVE_ONE_STACK,
		MOVE_ONE_ITEM,
		MOVE_ALL_ITEMS,
		MOVE_UP,
		MOVE_DOWN,
		MOVE_TO_EMPTY_SLOT,
		DROP;
	}
}
