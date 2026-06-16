package net.invtweaks.tree;

import farn.invtweaksStapi.InvTweaksStapi;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

@SuppressWarnings("unused")
public class ItemTree {
	public static final int MAX_CATEGORY_RANGE = 1000;
	private static final Logger log = InvTweaksStapi.LOGGER;
	private Map<String, ItemTreeCategory> categories = new HashMap<>();
	private Map<Integer, Vector<ItemTreeItem>> itemsById = new HashMap<>(500);
	private static Vector<ItemTreeItem> defaultItems = null;
	private Map<String, List<ItemTreeItem>> itemsByName = new HashMap<>(500);
	private String rootCategory;

	public ItemTree() {
		this.reset();
	}

	public void reset() {
		if(defaultItems == null) {
			defaultItems = new Vector<>();
			defaultItems.add(new ItemTreeItem("unknown", -1, -1, Integer.MAX_VALUE));
		}

		this.categories.clear();
		this.itemsByName.clear();
		this.itemsById.clear();
	}

	public boolean matches(List<ItemTreeItem> items, String keyword) {
		if(items == null) {
			return false;
		} else {

            for (ItemTreeItem i$ : items) {
                if (i$.getName().equals(keyword)) {
                    return true;
                }
            }

			ItemTreeCategory category1 = this.getCategory(keyword);
			if(category1 != null) {
                for (ItemTreeItem item : items) {
                    if (category1.contains(item)) {
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
		List<ItemTreeItem> items = this.getItems(keyword);
		if(items != null && !items.isEmpty()) {
			return items.get(0).getOrder();
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

	public Collection<ItemTreeCategory> getAllCategories() {
		return this.categories.values();
	}

	public ItemTreeCategory getRootCategory() {
		return this.categories.get(this.rootCategory);
	}

	public ItemTreeCategory getCategory(String keyword) {
		return this.categories.get(keyword);
	}

	public List<ItemTreeItem> getItems(int id, int damage) {
		List<ItemTreeItem> items = this.itemsById.get(id);
		ArrayList<ItemTreeItem> filteredItems = null;
		if(items == null) {
			return defaultItems;
		} else {

            for (ItemTreeItem item : items) {
                if (item.getDamage() != -1 && item.getDamage() != damage) {
                    if (filteredItems == null) {
                        filteredItems = new ArrayList<>(items);
                    }

                    filteredItems.remove(item);
                }
            }

			return filteredItems != null && !filteredItems.isEmpty() ? filteredItems : items;
		}
	}

	public List<ItemTreeItem> getItems(String name) {
		return this.itemsByName.get(name);
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
		this.categories.get(parentCategory.toLowerCase()).addCategory(newCategory);
		this.categories.put(newCategory.getName(), newCategory);
	}

	protected void addItem(String parentCategory, ItemTreeItem newItem) throws NullPointerException {
		this.categories.get(parentCategory.toLowerCase()).addItem(newItem);
		Vector<ItemTreeItem> list;
		if(this.itemsByName.containsKey(newItem.getName())) {
			this.itemsByName.get(newItem.getName()).add(newItem);
		} else {
			list = new Vector<>();
			list.add(newItem);
			this.itemsByName.put(newItem.getName(), list);
		}

		if(this.itemsById.containsKey(newItem.getId())) {
			this.itemsById.get(newItem.getId()).add(newItem);
		} else {
			list = new Vector<>();
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

        for (ItemTreeCategory itemList : category.getSubCategories()) {
            this.log(itemList, indentLevel + 1);
        }

        for (List<ItemTreeItem> itemTreeItems : category.getItems()) {
            for (ItemTreeItem item : itemTreeItems) {
                log.info(logIdent + "  " + item + " " + item.getId() + " " + item.getDamage());
            }
        }

	}
}
