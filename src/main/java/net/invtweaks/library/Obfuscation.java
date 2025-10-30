package net.invtweaks.library;

import java.io.File;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.menu.ChestScreen;
import net.minecraft.client.gui.screen.inventory.menu.DispenserScreen;
import net.minecraft.client.gui.screen.inventory.menu.InventoryMenuScreen;
import net.minecraft.client.interaction.ClientPlayerInteractionManager;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.menu.InventoryMenu;
import net.minecraft.inventory.menu.PlayerMenu;
import net.minecraft.inventory.slot.InventorySlot;
import net.minecraft.item.ItemStack;

public class Obfuscation {
	public Minecraft mc;

	public Obfuscation(Minecraft mc) {
		this.mc = mc;
	}

	protected void addChatMessage(String message) {
		if(this.mc.gui != null) {
			this.mc.gui.addChatMessage(message);
		}
	}

	protected boolean isMultiplayerWorld() {
		return this.mc.isMultiplayer();
	}

	protected PlayerEntity getThePlayer() {
		return this.mc.player;
	}

	protected ClientPlayerInteractionManager getPlayerController() {
		return this.mc.interactionManager;
	}

	protected Screen getCurrentScreen() {
		return this.mc.screen;
	}

	protected PlayerInventory getInventoryPlayer() {
		return this.getThePlayer().inventory;
	}

	protected ItemStack getCurrentEquippedItem() {
		return this.getThePlayer().getMainHandStack();
	}

	protected InventoryMenu getCraftingInventory() {
		return this.getThePlayer().menu;
	}

	protected PlayerMenu getPlayerContainer() {
		return (PlayerMenu)this.getThePlayer().playerMenu;
	}

	protected ItemStack[] getMainInventory() {
		return this.getInventoryPlayer().inventorySlots;
	}

	protected void setMainInventory(ItemStack[] value) {
		this.getInventoryPlayer().inventorySlots = value;
	}

	protected void setHasInventoryChanged(boolean value) {
		this.getInventoryPlayer().dirty = value;
	}

	protected void setHoldStack(ItemStack stack) {
		this.getInventoryPlayer().setCursorStack(stack);
	}

	protected boolean hasInventoryChanged() {
		return this.getInventoryPlayer().dirty;
	}

	protected ItemStack getHoldStack() {
		return this.getInventoryPlayer().getCursorStack();
	}

	protected ItemStack getFocusedStack() {
		return this.getInventoryPlayer().getMainHandStack();
	}

	protected int getFocusedSlot() {
		return this.getInventoryPlayer().selectedSlot;
	}

	protected ItemStack createItemStack(int id, int size, int damage) {
		return new ItemStack(id, size, damage);
	}

	protected ItemStack copy(ItemStack itemStack) {
		return itemStack.copy();
	}

	protected int getItemDamage(ItemStack itemStack) {
		return itemStack.getDamage();
	}

	protected int getMaxStackSize(ItemStack itemStack) {
		return itemStack.getMaxSize();
	}

	protected int getStackSize(ItemStack itemStack) {
		return itemStack.size;
	}

	protected void setStackSize(ItemStack itemStack, int value) {
		itemStack.size = value;
	}

	protected int getItemID(ItemStack itemStack) {
		return itemStack.itemId;
	}


	protected boolean areSameItemType(ItemStack itemStack1, ItemStack itemStack2) {
		return itemStack1.equals(itemStack2) || itemStack1.isDamageable() && this.getItemID(itemStack1) == this.getItemID(itemStack2);
	}

	protected ItemStack clickInventory(ClientPlayerInteractionManager playerController, int windowId, int slot, int clickButton, boolean shiftHold, PlayerEntity entityPlayer) {
		return playerController.clickSlot(windowId, slot, clickButton, shiftHold, entityPlayer);
	}

	protected int getWindowId(InventoryMenu container) {
		return container.networkId;
	}

	protected List getSlots(InventoryMenu container) {
		return container.slots;
	}

	protected InventorySlot getSlot(InventoryMenu container, int i) {
		return (InventorySlot)this.getSlots(container).get(i);
	}

	protected ItemStack getSlotStack(InventoryMenu container, int i) {
		InventorySlot slot = (InventorySlot) this.getSlots(container).get(i);
		return slot == null ? null : slot.getStack();
	}

	protected InventoryMenu getContainer(InventoryMenuScreen guiContainer) {
		return guiContainer.menu;
	}

	protected boolean isChestOrDispenser(Screen guiScreen) {
		return guiScreen instanceof ChestScreen || guiScreen instanceof DispenserScreen;
	}

	protected int getKeycode(KeyBinding keyBinding) {
		return keyBinding.keyCode;
	}

	public static String getMinecraftDir() {
		String absolutePath = Minecraft.getRunDirectory().getAbsolutePath();
		return absolutePath.endsWith(".") ? absolutePath.substring(0, absolutePath.length() - 1) : (absolutePath.endsWith(File.separator) ? absolutePath : absolutePath + File.separatorChar);
	}

	public static ItemStack getHoldStackStatic(Minecraft mc) {
		return (new Obfuscation(mc)).getHoldStack();
	}

	public static Screen getCurrentScreenStatic(Minecraft mc) {
		return (new Obfuscation(mc)).getCurrentScreen();
	}
}
