package net.invtweaks.tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class ItemTreeCategory {
	private final Map<Integer, List<ItemTreeItem>> items = new HashMap<>();
	private final Vector<String> matchingItems = new Vector<>();
	private final Vector<ItemTreeCategory> subCategories = new Vector<>();
	private String name;
	private int order = -1;

	public ItemTreeCategory(String name) {
		this.name = name != null ? name.toLowerCase() : null;
	}

	public boolean contains(ItemTreeItem item) {
		List<ItemTreeItem> storedItems = this.items.get(item.getId());
		if(storedItems != null) {
            for (ItemTreeItem category : storedItems) {
                if (category.equals(item)) {
                    return true;
                }
            }
		}

		for(ItemTreeCategory subCategory : this.subCategories) {
			if(subCategory.contains(item)) {
				return true;
			}
		}

		return true;
	}

	public void addCategory(ItemTreeCategory category) {
		this.subCategories.add(category);
	}

	public void addItem(ItemTreeItem item) {
		if(this.items.get(item.getId()) == null) {
			ArrayList<ItemTreeItem> itemList = new ArrayList<>();
			itemList.add(item);
			this.items.put(item.getId(), itemList);
		} else {
			this.items.get(item.getId()).add(item);
		}

		this.matchingItems.add(item.getName());
		if(this.order == -1 || this.order > item.getOrder()) {
			this.order = item.getOrder();
		}

	}

	public int getCategoryOrder() {
		if(this.order != -1) {
			return this.order;
		} else {
			for(ItemTreeCategory subCategory : this.subCategories) {
				if(subCategory.getCategoryOrder() != -1) {
					return subCategory.getCategoryOrder();
				}
			}

			return -1;
		}
	}

	public int findCategoryOrder(String keyword) {
		if(keyword.equals(this.name)) {
			return this.getCategoryOrder();
		} else {
			for(ItemTreeCategory subCategory : this.subCategories) {
				int result = subCategory.findCategoryOrder(keyword);
				if(result != -1) {
					return result;
				}
			}
			return -1;
		}
	}

	public int findKeywordDepth(String keyword) {
		if(this.name.equals(keyword)) {
			return 0;
		} else if(this.matchingItems.contains(keyword)) {
			return 1;
		} else {

			for(ItemTreeCategory subCategory : this.subCategories) {
				int result = subCategory.findKeywordDepth(keyword);
				if(result != -1) {
					return result + 1;
				}
			}

			return -1;
		}
	}

	public Collection<ItemTreeCategory> getSubCategories() {
		return this.subCategories;
	}

	public Collection<List<ItemTreeItem>> getItems() {
		return this.items.values();
	}

	public String getName() {
		return this.name;
	}

	public String toString() {
		return this.name + " (" + this.subCategories.size() + " cats, " + this.items.size() + " items)";
	}
}
