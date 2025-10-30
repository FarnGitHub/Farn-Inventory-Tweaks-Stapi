package net.invtweaks.tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Logger;

public class ItemTreeCategory {
	private final Map items = new HashMap();
	private final Vector matchingItems = new Vector();
	private final Vector subCategories = new Vector();
	private String name;
	private int order = -1;

	public ItemTreeCategory(String name) {
		this.name = name != null ? name.toLowerCase() : null;
	}

	public boolean contains(ItemTreeItem item) {
		List storedItems = (List)this.items.get(item.getId());
		Iterator i$;
		if(storedItems != null) {
			i$ = storedItems.iterator();

			while(i$.hasNext()) {
				ItemTreeItem category = (ItemTreeItem)i$.next();
				if(category.equals(item)) {
					return true;
				}
			}
		}

		i$ = this.subCategories.iterator();

		ItemTreeCategory category1;
		do {
			if(!i$.hasNext()) {
				return false;
			}

			category1 = (ItemTreeCategory)i$.next();
		} while(!category1.contains(item));

		return true;
	}

	public void addCategory(ItemTreeCategory category) {
		this.subCategories.add(category);
	}

	public void addItem(ItemTreeItem item) {
		if(this.items.get(item.getId()) == null) {
			ArrayList itemList = new ArrayList();
			itemList.add(item);
			this.items.put(item.getId(), itemList);
		} else {
			((List)this.items.get(item.getId())).add(item);
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
			Iterator i$ = this.subCategories.iterator();

			int order;
			do {
				if(!i$.hasNext()) {
					return -1;
				}

				ItemTreeCategory category = (ItemTreeCategory)i$.next();
				order = category.getCategoryOrder();
			} while(order == -1);

			return order;
		}
	}

	public int findCategoryOrder(String keyword) {
		if(keyword.equals(this.name)) {
			return this.getCategoryOrder();
		} else {
			Iterator i$ = this.subCategories.iterator();

			int result;
			do {
				if(!i$.hasNext()) {
					return -1;
				}

				ItemTreeCategory category = (ItemTreeCategory)i$.next();
				result = category.findCategoryOrder(keyword);
			} while(result == -1);

			return result;
		}
	}

	public int findKeywordDepth(String keyword) {
		if(this.name.equals(keyword)) {
			return 0;
		} else if(this.matchingItems.contains(keyword)) {
			return 1;
		} else {
			Iterator i$ = this.subCategories.iterator();

			int result;
			do {
				if(!i$.hasNext()) {
					return -1;
				}

				ItemTreeCategory category = (ItemTreeCategory)i$.next();
				result = category.findKeywordDepth(keyword);
			} while(result == -1);

			return result + 1;
		}
	}

	public Collection getSubCategories() {
		return this.subCategories;
	}

	public Collection getItems() {
		return this.items.values();
	}

	public String getName() {
		return this.name;
	}

	public String toString() {
		return this.name + " (" + this.subCategories.size() + " cats, " + this.items.size() + " items)";
	}
}
