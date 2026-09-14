package net.invtweaks.library;

import java.io.File;
import java.util.List;

import net.fabricmc.loader.api.FabricLoader;
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

/**
 * Obfuscation layer, used to centralize most calls to Minecraft code.
 * Eases transitions when Minecraft then MCP are updated.
 * 
 * @author Jimeo Wan
 *
 */
public class Obfuscation {

    protected Minecraft mc = Minecraft.INSTANCE;

    public Obfuscation() {
    }

    // Minecraft members

    protected void addChatMessage(String message) {
        if (mc.inGameHud != null) {
            mc.inGameHud.addChatMessage(message);
        }
    }

    protected boolean isMultiplayerWorld() {
        return mc.isWorldRemote();
    }

    protected PlayerEntity getThePlayer() {
        return mc.player;
    }

    protected InteractionManager getPlayerController() {
        return mc.interactionManager;
    }

    protected Screen getCurrentScreen() {
        return mc.currentScreen;
    }

    // EntityPlayer members

    protected PlayerInventory getInventoryPlayer() {
        return getThePlayer().inventory;
    }

    protected ItemStack getCurrentEquippedItem() {
        return getThePlayer().getHand();
    }

    protected ScreenHandler getCraftingInventory() {
        return getThePlayer().currentScreenHandler;
    }

    protected PlayerScreenHandler getPlayerContainer() {
        return (PlayerScreenHandler)getThePlayer().playerScreenHandler; // MCP name: inventorySlots
    }

    // InventoryPlayer members

    protected ItemStack[] getMainInventory() {
        return getInventoryPlayer().main;
    }

    protected void setMainInventory(ItemStack[] value) {
        getInventoryPlayer().main = value;
    }

    protected void setHasInventoryChanged(boolean value) {
        getInventoryPlayer().dirty = value;
    }

    protected void setHoldStack(ItemStack stack) {
        getInventoryPlayer().setCursorStack(stack); // MCP name: setItemStack
    }

    protected boolean hasInventoryChanged() {
        return getInventoryPlayer().dirty;
    }

    protected ItemStack getHoldStack() {
        return getInventoryPlayer().getCursorStack(); // MCP name: getItemStack
    }

    protected ItemStack getFocusedStack() {
        return getInventoryPlayer().getSelectedItem(); // MCP name: getCurrentItem
    }

    protected int getFocusedSlot() {
        return getInventoryPlayer().selectedSlot; // MCP name: currentItem
    }

    // ItemStack members

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

    protected boolean areItemStacksEqual(ItemStack itemStack1, ItemStack itemStack2) {
        return ItemStack.areEqual(itemStack1, itemStack2);
    }
    
    protected boolean areSameItemType(ItemStack itemStack1, ItemStack itemStack2) {
        return itemStack1.isItemEqual(itemStack2) ||
                (itemStack1.isDamageable() &&
                        getItemID(itemStack1) == getItemID(itemStack2));
    }

    // PlayerController members

    protected ItemStack clickInventory(InteractionManager playerController,
            int windowId, int slot, int clickButton, boolean shiftHold,
            PlayerEntity entityPlayer) {
        return playerController.clickSlot(windowId, slot, clickButton,
                shiftHold, entityPlayer); /* func_27174_a */
    }

    // Container members

    protected int getWindowId(ScreenHandler container) {
        return container.syncId;
    }

    protected List<?> getSlots(ScreenHandler container) {
        return container.slots;
    }

    protected Slot getSlot(ScreenHandler container, int i) {
        return (Slot) getSlots(container).get(i);
    }

    protected ItemStack getSlotStack(ScreenHandler container, int i) {
        Slot slot = (Slot) getSlots(container).get(i);
        return (slot == null) ? null : slot.getStack(); /* getStack */
    }

    protected void setSlotStack(ScreenHandler container, int i, ItemStack stack) {
        container.setStackInSlot(i, stack); /* putStackInSlot */
    }

    // GuiContainer members

    protected ScreenHandler getContainer(HandledScreen guiContainer) {
        return guiContainer.container;
    }

    // Other

    protected boolean isChestOrDispenser(Screen guiScreen) {
        return ((guiScreen instanceof DoubleChestScreen /* GuiChest */
                && !guiScreen.getClass().getSimpleName().equals("MLGuiChestBuilding")) // Millenaire mod
        || guiScreen instanceof DispenserScreen /* GuiDispenser */);
    }
    
    protected int getKeycode(KeyBinding keyBinding) {
        return keyBinding.code;
    }
    
    // Static access

    /**
     * Returns the Minecraft folder ensuring: - It is an absolute path - It ends
     * with a folder separator
     */
    public static String getMinecraftDir() {
        String absolutePath = FabricLoader.getInstance().getGameDir().toFile().getAbsolutePath();
        if (absolutePath.endsWith(".")) {
            return absolutePath.substring(0, absolutePath.length() - 1);
        }
        if (absolutePath.endsWith(File.separator)) {
            return absolutePath;
        } else {
            return absolutePath + File.separatorChar;
        }
    }
    
    public static ItemStack getHoldStackStatic() {
        return new Obfuscation().getHoldStack();
    }

    public static Screen getCurrentScreenStatic() {
        return new Obfuscation().getCurrentScreen();
    }

}