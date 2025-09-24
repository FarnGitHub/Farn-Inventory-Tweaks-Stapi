package net.invtweaks.tree;

import farn.invtweaksStapi.InvTweaksStapi;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

public class ItemTree {
	public static final int MAX_CATEGORY_RANGE = 1000;
	private static final Logger log = InvTweaksStapi.LOGGER;
	private Map categories = new HashMap();
	private Map itemsById = new HashMap(500);
	private static Vector defaultItems = null;
	private Map itemsByName = new HashMap(500);
	private String rootCategory;

	public ItemTree() {
		this.reset();
	}

	public void reset() {
		if(defaultItems == null) {
			defaultItems = new Vector();
			defaultItems.add(new ItemTreeItem("unknown", -1, -1, Integer.MAX_VALUE));
		}

		this.categories.clear();
		this.itemsByName.clear();
		this.itemsById.clear();
	}

	public boolean matches(List items, String keyword) {
		if(items == null) {
			return false;
		} else {
			Iterator category = items.iterator();

			while(category.hasNext()) {
				ItemTreeItem i$ = (ItemTreeItem)category.next();
				if(i$.getName().equals(keyword)) {
					return true;
				}
			}

			ItemTreeCategory category1 = this.getCategory(keyword);
			if(category1 != null) {
				Iterator i$1 = items.iterator();

				while(i$1.hasNext()) {
					ItemTreeItem item = (ItemTreeItem)i$1.next();
					if(category1.contains(item)) {
						return true;
					}
				}
			}

			return keyword.equals(this.rootCategory);
		}
	}

	public int getKeywordDepth(String keyword) {
		try {
			return this.getRootCategory().findKeywordDepth(keyword);
		} catch (NullPointerException nullPointerException3) {
			log.error("The root category is missing: " + nullPointerException3.getMessage());
			return 0;
		}
	}

	public int getKeywordOrder(String keyword) {
		List items = this.getItems(keyword);
		if(items != null && items.size() != 0) {
			return ((ItemTreeItem)items.get(0)).getOrder();
		} else {
			try {
				return this.getRootCategory().findCategoryOrder(keyword);
			} catch (NullPointerException nullPointerException4) {
				log.error("The root category is missing: " + nullPointerException4.getMessage());
				return -1;
			}
		}
	}

	public boolean isKeywordValid(String keyword) {
		if(this.containsItem(keyword)) {
			return true;
		} else {
			ItemTreeCategory category = this.getCategory(keyword);
			return category != null;
		}
	}

	public Collection getAllCategories() {
		return this.categories.values();
	}

	public ItemTreeCategory getRootCategory() {
		return (ItemTreeCategory)this.categories.get(this.rootCategory);
	}

	public ItemTreeCategory getCategory(String keyword) {
		return (ItemTreeCategory)this.categories.get(keyword);
	}

	public List getItems(int id, int damage) {
		List items = (List)this.itemsById.get(id);
		ArrayList filteredItems = null;
		if(items == null) {
			return defaultItems;
		} else {
			Iterator i$ = items.iterator();

			while(i$.hasNext()) {
				ItemTreeItem item = (ItemTreeItem)i$.next();
				if(item.getDamage() != -1 && item.getDamage() != damage) {
					if(filteredItems == null) {
						filteredItems = new ArrayList(items);
					}

					filteredItems.remove(item);
				}
			}

			return (List)(filteredItems != null && !filteredItems.isEmpty() ? filteredItems : items);
		}
	}

	public List getItems(String name) {
		return (List)this.itemsByName.get(name);
	}

	public ItemTreeItem getRandomItem(Random r) {
		return (ItemTreeItem)this.itemsByName.values().toArray()[r.nextInt(this.itemsByName.size())];
	}

	public boolean containsItem(String name) {
		return this.itemsByName.containsKey(name);
	}

	public boolean containsCategory(String name) {
		return this.categories.containsKey(name);
	}

	protected void setRootCategory(ItemTreeCategory category) {
		this.rootCategory = category.getName();
		this.categories.put(this.rootCategory, category);
	}

	protected void addCategory(String parentCategory, ItemTreeCategory newCategory) throws NullPointerException {
		((ItemTreeCategory)this.categories.get(parentCategory.toLowerCase())).addCategory(newCategory);
		this.categories.put(newCategory.getName(), newCategory);
	}

	protected void addItem(String parentCategory, ItemTreeItem newItem) throws NullPointerException {
		((ItemTreeCategory)this.categories.get(parentCategory.toLowerCase())).addItem(newItem);
		Vector list;
		if(this.itemsByName.containsKey(newItem.getName())) {
			((Vector)this.itemsByName.get(newItem.getName())).add(newItem);
		} else {
			list = new Vector();
			list.add(newItem);
			this.itemsByName.put(newItem.getName(), list);
		}

		if(this.itemsById.containsKey(newItem.getId())) {
			((Vector)this.itemsById.get(newItem.getId())).add(newItem);
		} else {
			list = new Vector();
			list.add(newItem);
			this.itemsById.put(newItem.getId(), list);
		}

	}

	private void log(ItemTreeCategory category, int indentLevel) {
		String logIdent = "";

		for(int i$ = 0; i$ < indentLevel; ++i$) {
			logIdent = logIdent + "  ";
		}

		log.info(logIdent + category.getName());
		Iterator iterator8 = category.getSubCategories().iterator();

		while(iterator8.hasNext()) {
			ItemTreeCategory itemList = (ItemTreeCategory)iterator8.next();
			this.log(itemList, indentLevel + 1);
		}

		iterator8 = category.getItems().iterator();

		while(iterator8.hasNext()) {
			List list9 = (List)iterator8.next();
			Iterator i$1 = list9.iterator();

			while(i$1.hasNext()) {
				ItemTreeItem item = (ItemTreeItem)i$1.next();
				log.info(logIdent + "  " + item + " " + item.getId() + " " + item.getDamage());
			}
		}

	}
}
