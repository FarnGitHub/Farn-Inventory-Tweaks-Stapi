package net.invtweaks.tree;

import net.invtweaks.library.Obfuscation;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

public class ItemTreeItem extends Obfuscation implements Comparable {
	private String name;
	private int id;
	private int damage;
	private int order;

	public ItemTreeItem(String name, int id, int damage, int order) {
		super((Minecraft)null);
		this.name = name;
		this.id = id;
		this.damage = damage;
		this.order = order;
	}

	public String getName() {
		return this.name;
	}

	public int getId() {
		return this.id;
	}

	public int getDamage() {
		return this.damage;
	}

	public int getOrder() {
		return this.order;
	}

	public boolean matchesStack(ItemStack stack) {
		return this.getItemID(stack) == this.id && (this.getMaxStackSize(stack) == 1 || this.getItemDamage(stack) == this.damage);
	}

	public boolean equals(Object o) {
		if(o != null && o instanceof ItemTreeItem) {
			ItemTreeItem item = (ItemTreeItem)o;
			return this.id == item.getId() && (this.damage == -1 || this.damage == item.getDamage());
		} else {
			return false;
		}
	}

	public String toString() {
		return this.name;
	}

	public int compareTo(ItemTreeItem item) {
		return item.order - this.order;
	}

	public int compareTo(Object x0) {
		return this.compareTo((ItemTreeItem)x0);
	}
}
