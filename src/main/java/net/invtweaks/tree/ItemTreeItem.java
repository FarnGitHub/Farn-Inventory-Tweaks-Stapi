package net.invtweaks.tree;

import net.invtweaks.library.Obfuscation;
import net.minecraft.item.ItemStack;

public class ItemTreeItem extends Obfuscation implements Comparable<ItemTreeItem> {
	private final String name;
	private final int id;
	private final int damage;
	private final int order;

	public ItemTreeItem(String name, int id, int damage, int order) {
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

	@SuppressWarnings("unused")
	public boolean matchesStack(ItemStack stack) {
		return this.getItemID(stack) == this.id && (this.getMaxStackSize(stack) == 1 || this.getItemDamage(stack) == this.damage);
	}

	public boolean equals(Object o) {
		if(o instanceof ItemTreeItem item) {
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
}
