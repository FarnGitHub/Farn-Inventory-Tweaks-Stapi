package net.invtweaks.library;

import java.util.List;
import java.util.concurrent.TimeoutException;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

public class ContainerSectionManager {
	private ContainerManager containerMgr;
	private ContainerManager.ContainerSection section;

	public ContainerSectionManager(Minecraft mc, ContainerManager.ContainerSection section) throws Exception {
		this.containerMgr = new ContainerManager(mc);
		this.section = section;
		if(!this.containerMgr.hasSection(section)) {
			throw new Exception("Section not available");
		}
	}

	public boolean move(int srcIndex, int destIndex) throws TimeoutException {
		return this.containerMgr.move(this.section, srcIndex, this.section, destIndex);
	}

	public boolean moveSome(int srcIndex, int destIndex, int amount) throws TimeoutException {
		return this.containerMgr.moveSome(this.section, srcIndex, this.section, destIndex, amount);
	}

	public boolean drop(int srcIndex) throws TimeoutException {
		return this.containerMgr.drop(this.section, srcIndex);
	}

	public boolean dropSome(int srcIndex, int amount) throws TimeoutException {
		return this.containerMgr.dropSome(this.section, srcIndex, amount);
	}

	public void leftClick(int index) throws TimeoutException {
		this.containerMgr.leftClick(this.section, index);
	}

	public void rightClick(int index) throws TimeoutException {
		this.containerMgr.rightClick(this.section, index);
	}

	public void click(int index, boolean rightClick) throws TimeoutException {
		this.containerMgr.click(this.section, index, rightClick);
	}

	public List getSlots() {
		return this.containerMgr.getSlots(this.section);
	}

	public int getSize() {
		return this.containerMgr.getSize(this.section);
	}

	public int getFirstEmptyIndex() {
		return this.containerMgr.getFirstEmptyIndex(this.section);
	}

	public boolean isSlotEmpty(int slot) {
		return this.containerMgr.isSlotEmpty(this.section, slot);
	}

	public Slot getSlot(int index) {
		return this.containerMgr.getSlot(this.section, index);
	}

	public int getSlotIndex(int slotNumber) {
		return this.isSlotInSection(slotNumber) ? this.containerMgr.getSlotIndex(slotNumber) : -1;
	}

	public boolean isSlotInSection(int slotNumber) {
		return this.containerMgr.getSlotSection(slotNumber) == this.section;
	}

	public ItemStack getItemStack(int index) throws NullPointerException, IndexOutOfBoundsException {
		return this.containerMgr.getItemStack(this.section, index);
	}

	public ScreenHandler getContainer() {
		return this.containerMgr.getContainer();
	}
}
