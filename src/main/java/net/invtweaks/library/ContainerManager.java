package net.invtweaks.library;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.*;
import net.minecraft.screen.slot.Slot;

public class ContainerManager extends Obfuscation {
	public static final int DROP_SLOT = -999;
	public static final int INVENTORY_SIZE = 36;
	public static final int HOTBAR_SIZE = 9;
	public static final int ACTION_TIMEOUT = 500;
	public static final int POLLING_DELAY = 3;
	private ScreenHandler container;
	private Map slotRefs = new HashMap();

	public ContainerManager(Minecraft mc) {
		super(mc);
		Screen currentScreen = this.getCurrentScreen();
		if(currentScreen instanceof HandledScreen) {
			this.container = this.getContainer((HandledScreen) currentScreen);
		} else {
			this.container = this.getPlayerContainer();
		}

		List slots = this.container.slots;
		int size = slots.size();
		boolean guiWithInventory = true;
		if(this.container instanceof PlayerScreenHandler) {
			this.slotRefs.put(ContainerManager.ContainerSection.CRAFTING_OUT, slots.subList(0, 1));
			this.slotRefs.put(ContainerManager.ContainerSection.CRAFTING_IN, slots.subList(1, 5));
			this.slotRefs.put(ContainerManager.ContainerSection.ARMOR, slots.subList(5, 9));
		} else if(!(this.container instanceof GenericContainerScreenHandler) && !(this.container instanceof DispenserScreenHandler)) {
			if(this.container instanceof FurnaceScreenHandler) {
				this.slotRefs.put(ContainerManager.ContainerSection.FURNACE_IN, slots.subList(0, 1));
				this.slotRefs.put(ContainerManager.ContainerSection.FURNACE_FUEL, slots.subList(1, 2));
				this.slotRefs.put(ContainerManager.ContainerSection.FURNACE_OUT, slots.subList(2, 3));
			} else if(this.container instanceof CraftingScreenHandler) {
				this.slotRefs.put(ContainerManager.ContainerSection.CRAFTING_OUT, slots.subList(0, 1));
				this.slotRefs.put(ContainerManager.ContainerSection.CRAFTING_IN, slots.subList(1, 10));
			} else if(size >= 36) {
				this.slotRefs.put(ContainerManager.ContainerSection.UNKNOWN, slots.subList(0, size - 36));
			} else {
				guiWithInventory = false;
				this.slotRefs.put(ContainerManager.ContainerSection.UNKNOWN, slots.subList(0, size));
			}
		} else {
			this.slotRefs.put(ContainerManager.ContainerSection.CHEST, slots.subList(0, size - 36));
		}

		if(guiWithInventory) {
			this.slotRefs.put(ContainerManager.ContainerSection.INVENTORY, slots.subList(size - 36, size));
			this.slotRefs.put(ContainerManager.ContainerSection.INVENTORY_NOT_HOTBAR, slots.subList(size - 36, size - 9));
			this.slotRefs.put(ContainerManager.ContainerSection.INVENTORY_HOTBAR, slots.subList(size - 9, size));
		}

	}

	public boolean move(ContainerManager.ContainerSection srcSection, int srcIndex, ContainerManager.ContainerSection destSection, int destIndex) throws TimeoutException {
		ItemStack srcStack = this.getItemStack(srcSection, srcIndex);
		ItemStack destStack = this.getItemStack(destSection, destIndex);
		if(srcStack == null) {
			return false;
		} else if(srcSection == destSection && srcIndex == destIndex) {
			return true;
		} else {
			if(this.getHoldStack() != null) {
				int destinationEmpty = this.getFirstEmptyIndex(ContainerManager.ContainerSection.INVENTORY);
				if(destinationEmpty == -1) {
					return false;
				}

				this.leftClick(ContainerManager.ContainerSection.INVENTORY, destinationEmpty);
			}

			boolean destinationEmpty1 = this.getItemStack(destSection, destIndex) == null;
			if(destStack != null && this.getItemID(srcStack) == this.getItemID(destStack) && srcStack.getMaxCount() == 1) {
				int intermediateSlot = this.getFirstEmptyUsableSlotNumber();
				ContainerManager.ContainerSection intermediateSection = this.getSlotSection(intermediateSlot);
				int intermediateIndex = this.getSlotIndex(intermediateSlot);
				if(intermediateIndex == -1) {
					return false;
				}

				this.leftClick(destSection, destIndex);
				this.leftClick(intermediateSection, intermediateIndex);
				this.leftClick(srcSection, srcIndex);
				this.leftClick(destSection, destIndex);
				this.leftClick(intermediateSection, intermediateIndex);
				this.leftClick(srcSection, srcIndex);
			} else {
				this.leftClick(srcSection, srcIndex);
				this.leftClick(destSection, destIndex);
				if(!destinationEmpty1) {
					this.leftClick(srcSection, srcIndex);
				}
			}

			return true;
		}
	}

	public boolean moveSome(ContainerManager.ContainerSection srcSection, int srcIndex, ContainerManager.ContainerSection destSection, int destIndex, int amount) throws TimeoutException {
		ItemStack source = this.getItemStack(srcSection, srcIndex);
		if(source == null || srcSection == destSection && srcIndex == destIndex) {
			return true;
		} else {
			ItemStack destination = this.getItemStack(srcSection, srcIndex);
			int sourceSize = this.getStackSize(source);
			int movedAmount = Math.min(amount, sourceSize);
			if(source == null || destination != null && !source.isItemEqual(destination)) {
				return false;
			} else {
				this.leftClick(srcSection, srcIndex);

				for(int i = 0; i < movedAmount; ++i) {
					this.rightClick(destSection, destIndex);
				}

				if(movedAmount < sourceSize) {
					this.leftClick(srcSection, srcIndex);
				}

				return true;
			}
		}
	}

	public boolean drop(ContainerManager.ContainerSection srcSection, int srcIndex) throws TimeoutException {
		return this.move(srcSection, srcIndex, (ContainerManager.ContainerSection)null, -999);
	}

	public boolean dropSome(ContainerManager.ContainerSection srcSection, int srcIndex, int amount) throws TimeoutException {
		return this.moveSome(srcSection, srcIndex, (ContainerManager.ContainerSection)null, -999, amount);
	}

	public void leftClick(ContainerManager.ContainerSection section, int index) throws TimeoutException {
		this.click(section, index, false);
	}

	public void rightClick(ContainerManager.ContainerSection section, int index) throws TimeoutException {
		this.click(section, index, true);
	}

	public void click(ContainerManager.ContainerSection section, int index, boolean rightClick) throws TimeoutException {
		int slot = this.indexToSlot(section, index);
		if(slot != -1) {
			this.clickInventory(this.getPlayerController(), this.getWindowId(this.container), slot, rightClick ? 1 : 0, false, this.getThePlayer());
		}

	}

	public boolean hasSection(ContainerManager.ContainerSection section) {
		return this.slotRefs.containsKey(section);
	}

	public List getSlots(ContainerManager.ContainerSection section) {
		return (List)this.slotRefs.get(section);
	}

	public int getSize() {
		int result = 0;

		List slots;
		for(Iterator i$ = this.slotRefs.values().iterator(); i$.hasNext(); result += slots.size()) {
			slots = (List)i$.next();
		}

		return result;
	}

	public int getSize(ContainerManager.ContainerSection section) {
		return this.hasSection(section) ? ((List)this.slotRefs.get(section)).size() : 0;
	}

	public int getFirstEmptyIndex(ContainerManager.ContainerSection section) {
		int i = 0;

		for(Iterator i$ = ((List)this.slotRefs.get(section)).iterator(); i$.hasNext(); ++i) {
			Slot slot = (Slot)i$.next();
			if(!slot.hasStack()) {
				return i;
			}
		}

		return -1;
	}

	public boolean isSlotEmpty(ContainerManager.ContainerSection section, int slot) {
		return this.hasSection(section) ? this.getItemStack(section, slot) == null : false;
	}

	public Slot getSlot(ContainerManager.ContainerSection section, int index) {
		List slots = (List)this.slotRefs.get(section);
		return slots != null ? (Slot)slots.get(index) : null;
	}

	public int getSlotIndex(int slotNumber) {
		Iterator i$ = this.slotRefs.keySet().iterator();

		while(true) {
			ContainerManager.ContainerSection section;
			do {
				if(!i$.hasNext()) {
					return -1;
				}

				section = (ContainerManager.ContainerSection)i$.next();
			} while(section == ContainerManager.ContainerSection.INVENTORY);

			int i = 0;

			for(Iterator i$1 = ((List)this.slotRefs.get(section)).iterator(); i$1.hasNext(); ++i) {
				Slot slot = (Slot)i$1.next();
				if(slot.id == slotNumber) {
					return i;
				}
			}
		}
	}

	public ContainerManager.ContainerSection getSlotSection(int slotNumber) {
		Iterator i$ = this.slotRefs.keySet().iterator();

		while(true) {
			ContainerManager.ContainerSection section;
			do {
				if(!i$.hasNext()) {
					return null;
				}

				section = (ContainerManager.ContainerSection)i$.next();
			} while(section == ContainerManager.ContainerSection.INVENTORY);

			Iterator i$1 = ((List)this.slotRefs.get(section)).iterator();

			while(i$1.hasNext()) {
				Slot slot = (Slot)i$1.next();
				if(slot.id == slotNumber) {
					return section;
				}
			}
		}
	}

	public ItemStack getItemStack(ContainerManager.ContainerSection section, int index) throws NullPointerException, IndexOutOfBoundsException {
		int slot = this.indexToSlot(section, index);
		return slot >= 0 && slot < this.getSlots(this.container).size() ? this.getSlotStack(this.container, slot) : null;
	}

	public ScreenHandler getContainer() {
		return this.container;
	}

	private int getFirstEmptyUsableSlotNumber() {
		Iterator i$ = this.slotRefs.keySet().iterator();

		while(i$.hasNext()) {
			ContainerManager.ContainerSection section = (ContainerManager.ContainerSection)i$.next();
			Iterator i$1 = ((List)this.slotRefs.get(section)).iterator();

			while(i$1.hasNext()) {
				Slot slot = (Slot)i$1.next();
				if(slot.getClass().equals(Slot.class) && !slot.hasStack()) {
					return slot.id;
				}
			}
		}

		return -1;
	}

	private int indexToSlot(ContainerManager.ContainerSection section, int index) {
		if(index == -999) {
			return -999;
		} else if(this.hasSection(section)) {
			Slot slot = (Slot)((List)this.slotRefs.get(section)).get(index);
			return slot != null ? slot.id : -1;
		} else {
			return -1;
		}
	}

	public static enum ContainerSection {
		INVENTORY,
		INVENTORY_HOTBAR,
		INVENTORY_NOT_HOTBAR,
		CHEST,
		CRAFTING_IN,
		CRAFTING_OUT,
		ARMOR,
		FURNACE_IN,
		FURNACE_OUT,
		FURNACE_FUEL,
		UNKNOWN;
	}
}
