package net.invtweaks.library;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.menu.InventoryMenuScreen;
import net.minecraft.client.gui.screen.inventory.menu.SurvivalInventoryScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.menu.*;
import net.minecraft.inventory.slot.InventorySlot;
import net.minecraft.item.ItemStack;

public class ContainerManager extends Obfuscation {
	public static final int DROP_SLOT = -999;
	public static final int INVENTORY_SIZE = 36;
	public static final int HOTBAR_SIZE = 9;
	public static final int ACTION_TIMEOUT = 500;
	public static final int POLLING_DELAY = 3;
	private InventoryMenu container;
	private Map slotRefs = new HashMap();

	public ContainerManager(Minecraft mc) {
		super(mc);
		Screen currentScreen = this.getCurrentScreen();
		if(currentScreen instanceof InventoryMenuScreen) {
			this.container = this.getContainer((InventoryMenuScreen) currentScreen);
		} else {
			this.container = this.getPlayerContainer();
		}

		List slots = this.container.slots;
		int size = slots.size();
		boolean guiWithInventory = true;
		if(this.container instanceof PlayerMenu) {
			this.slotRefs.put(ContainerSection.CRAFTING_OUT, slots.subList(0, 1));
			this.slotRefs.put(ContainerSection.CRAFTING_IN, slots.subList(1, 5));
			this.slotRefs.put(ContainerSection.ARMOR, slots.subList(5, 9));
		} else if(!(this.container instanceof ChestMenu) && !(this.container instanceof DispenserMenu)) {
			if(this.container instanceof FurnaceMenu) {
				this.slotRefs.put(ContainerSection.FURNACE_IN, slots.subList(0, 1));
				this.slotRefs.put(ContainerSection.FURNACE_FUEL, slots.subList(1, 2));
				this.slotRefs.put(ContainerSection.FURNACE_OUT, slots.subList(2, 3));
			} else if(this.container instanceof CraftingTableMenu) {
				this.slotRefs.put(ContainerSection.CRAFTING_OUT, slots.subList(0, 1));
				this.slotRefs.put(ContainerSection.CRAFTING_IN, slots.subList(1, 10));
			} else if(size >= 36) {
				this.slotRefs.put(ContainerSection.UNKNOWN, slots.subList(0, size - 36));
			} else {
				guiWithInventory = false;
				this.slotRefs.put(ContainerSection.UNKNOWN, slots.subList(0, size));
			}
		} else {
			this.slotRefs.put(ContainerSection.CHEST, slots.subList(0, size - 36));
		}

		if(guiWithInventory) {
			this.slotRefs.put(ContainerSection.INVENTORY, slots.subList(size - 36, size));
			this.slotRefs.put(ContainerSection.INVENTORY_NOT_HOTBAR, slots.subList(size - 36, size - 9));
			this.slotRefs.put(ContainerSection.INVENTORY_HOTBAR, slots.subList(size - 9, size));
		}

	}

	public boolean move(ContainerSection srcSection, int srcIndex, ContainerSection destSection, int destIndex) throws TimeoutException {
		ItemStack srcStack = this.getItemStack(srcSection, srcIndex);
		ItemStack destStack = this.getItemStack(destSection, destIndex);
		if(srcStack == null) {
			return false;
		} else if(srcSection == destSection && srcIndex == destIndex) {
			return true;
		} else {
			if(this.getHoldStack() != null) {
				int destinationEmpty = this.getFirstEmptyIndex(ContainerSection.INVENTORY);
				if(destinationEmpty == -1) {
					return false;
				}

				this.leftClick(ContainerSection.INVENTORY, destinationEmpty);
			}

			boolean destinationEmpty1 = this.getItemStack(destSection, destIndex) == null;
			if(destStack != null && this.getItemID(srcStack) == this.getItemID(destStack) && srcStack.getMaxSize() == 1) {
				int intermediateSlot = this.getFirstEmptyUsableSlotNumber();
				ContainerSection intermediateSection = this.getSlotSection(intermediateSlot);
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

	public boolean moveSome(ContainerSection srcSection, int srcIndex, ContainerSection destSection, int destIndex, int amount) throws TimeoutException {
		ItemStack source = this.getItemStack(srcSection, srcIndex);
		if(source == null || srcSection == destSection && srcIndex == destIndex) {
			return true;
		} else {
			ItemStack destination = this.getItemStack(srcSection, srcIndex);
			int sourceSize = this.getStackSize(source);
			int movedAmount = Math.min(amount, sourceSize);
			if(source == null || destination != null && !source.equals(destination)) {
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

	public boolean drop(ContainerSection srcSection, int srcIndex) throws TimeoutException {
		return this.move(srcSection, srcIndex, (ContainerSection)null, -999);
	}

	public boolean dropSome(ContainerSection srcSection, int srcIndex, int amount) throws TimeoutException {
		return this.moveSome(srcSection, srcIndex, (ContainerSection)null, -999, amount);
	}

	public void leftClick(ContainerSection section, int index) throws TimeoutException {
		this.click(section, index, false);
	}

	public void rightClick(ContainerSection section, int index) throws TimeoutException {
		this.click(section, index, true);
	}

	public void click(ContainerSection section, int index, boolean rightClick) throws TimeoutException {
		int slot = this.indexToSlot(section, index);
		if(slot != -1) {
			this.clickInventory(this.getPlayerController(), this.getWindowId(this.container), slot, rightClick ? 1 : 0, false, this.getThePlayer());
		}

	}

	public boolean hasSection(ContainerSection section) {
		return this.slotRefs.containsKey(section);
	}

	public List getSlots(ContainerSection section) {
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

	public int getSize(ContainerSection section) {
		return this.hasSection(section) ? ((List)this.slotRefs.get(section)).size() : 0;
	}

	public int getFirstEmptyIndex(ContainerSection section) {
		int i = 0;

		for(Iterator i$ = ((List)this.slotRefs.get(section)).iterator(); i$.hasNext(); ++i) {
			InventorySlot slot = (InventorySlot)i$.next();
			if(!slot.hasStack()) {
				return i;
			}
		}

		return -1;
	}

	public boolean isSlotEmpty(ContainerSection section, int slot) {
		return this.hasSection(section) ? this.getItemStack(section, slot) == null : false;
	}

	public InventorySlot getSlot(ContainerSection section, int index) {
		List slots = (List)this.slotRefs.get(section);
		return slots != null ? (InventorySlot)slots.get(index) : null;
	}

	public int getSlotIndex(int slotNumber) {
		Iterator i$ = this.slotRefs.keySet().iterator();

		while(true) {
			ContainerSection section;
			do {
				if(!i$.hasNext()) {
					return -1;
				}

				section = (ContainerSection)i$.next();
			} while(section == ContainerSection.INVENTORY);

			int i = 0;

			for(Iterator i$1 = ((List)this.slotRefs.get(section)).iterator(); i$1.hasNext(); ++i) {
				InventorySlot slot = (InventorySlot)i$1.next();
				if(slot.id == slotNumber) {
					return i;
				}
			}
		}
	}

	public ContainerSection getSlotSection(int slotNumber) {
		Iterator i$ = this.slotRefs.keySet().iterator();

		while(true) {
			ContainerSection section;
			do {
				if(!i$.hasNext()) {
					return null;
				}

				section = (ContainerSection)i$.next();
			} while(section == ContainerSection.INVENTORY);

			Iterator i$1 = ((List)this.slotRefs.get(section)).iterator();

			while(i$1.hasNext()) {
				InventorySlot slot = (InventorySlot)i$1.next();
				if(slot.id == slotNumber) {
					return section;
				}
			}
		}
	}

	public ItemStack getItemStack(ContainerSection section, int index) throws NullPointerException, IndexOutOfBoundsException {
		int slot = this.indexToSlot(section, index);
		return slot >= 0 && slot < this.getSlots(this.container).size() ? this.getSlotStack(this.container, slot) : null;
	}

	public InventoryMenu getContainer() {
		return this.container;
	}

	private int getFirstEmptyUsableSlotNumber() {
		Iterator i$ = this.slotRefs.keySet().iterator();

		while(i$.hasNext()) {
			ContainerSection section = (ContainerSection)i$.next();
			Iterator i$1 = ((List)this.slotRefs.get(section)).iterator();

			while(i$1.hasNext()) {
				InventorySlot slot = (InventorySlot)i$1.next();
				if(slot.getClass().equals(InventorySlot.class) && !slot.hasStack()) {
					return slot.id;
				}
			}
		}

		return -1;
	}

	private int indexToSlot(ContainerSection section, int index) {
		if(index == -999) {
			return -999;
		} else if(this.hasSection(section)) {
			InventorySlot slot = (InventorySlot)((List)this.slotRefs.get(section)).get(index);
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
