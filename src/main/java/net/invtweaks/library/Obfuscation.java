package net.invtweaks.library;

import java.io.File;
import java.util.List;

import net.minecraft.client.InteractionManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.DispenserScreen;
import net.minecraft.client.gui.screen.ingame.DoubleChestScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

public class Obfuscation {
	public Minecraft mc;

	public Obfuscation(Minecraft mc) {
		this.mc = mc;
	}

	protected void addChatMessage(String message) {
		if(this.mc.inGameHud != null) {
			this.mc.inGameHud.addChatMessage(message);
		}
	}

	protected boolean isMultiplayerWorld() {
		return this.mc.isWorldRemote();
	}

	protected PlayerEntity getThePlayer() {
		return this.mc.player;
	}

	protected InteractionManager getPlayerController() {
		return this.mc.interactionManager;
	}

	protected Screen getCurrentScreen() {
		return this.mc.currentScreen;
	}

	protected PlayerInventory getInventoryPlayer() {
		return this.getThePlayer().inventory;
	}

	protected ItemStack getCurrentEquippedItem() {
		return this.getThePlayer().getHeldItem();
	}

	protected ScreenHandler getCraftingInventory() {
		return this.getThePlayer().currentScreenHandler;
	}

	protected PlayerScreenHandler getPlayerContainer() {
		return (PlayerScreenHandler)this.getThePlayer().playerScreenHandler;
	}

	protected ItemStack[] getMainInventory() {
		return this.getInventoryPlayer().main;
	}

	protected void setMainInventory(ItemStack[] value) {
		this.getInventoryPlayer().main = value;
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
		return this.getInventoryPlayer().getSelectedItem();
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
		return itemStack.getMaxCount();
	}

	protected int getStackSize(ItemStack itemStack) {
		return itemStack.count;
	}

	protected void setStackSize(ItemStack itemStack, int value) {
		itemStack.count = value;
	}

	protected int getItemID(ItemStack itemStack) {
		return itemStack.itemId;
	}


	protected boolean areSameItemType(ItemStack itemStack1, ItemStack itemStack2) {
		return itemStack1.isItemEqual(itemStack2) || itemStack1.isDamageable() && this.getItemID(itemStack1) == this.getItemID(itemStack2);
	}

	protected ItemStack clickInventory(InteractionManager playerController, int windowId, int slot, int clickButton, boolean shiftHold, PlayerEntity entityPlayer) {
		return playerController.clickSlot(windowId, slot, clickButton, shiftHold, entityPlayer);
	}

	protected int getWindowId(ScreenHandler container) {
		return container.syncId;
	}

	protected List getSlots(ScreenHandler container) {
		return container.slots;
	}

	protected Slot getSlot(ScreenHandler container, int i) {
		return (Slot)this.getSlots(container).get(i);
	}

	protected ItemStack getSlotStack(ScreenHandler container, int i) {
		Slot slot = (Slot)this.getSlots(container).get(i);
		return slot == null ? null : slot.getStack();
	}

	protected ScreenHandler getContainer(HandledScreen guiContainer) {
		return guiContainer.container;
	}

	protected boolean isChestOrDispenser(Screen guiScreen) {
		return guiScreen instanceof DoubleChestScreen || guiScreen instanceof DispenserScreen;
	}

	protected int getKeycode(KeyBinding keyBinding) {
		return keyBinding.code;
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
