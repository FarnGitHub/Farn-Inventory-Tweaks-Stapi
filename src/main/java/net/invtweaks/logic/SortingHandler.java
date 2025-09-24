package net.invtweaks.logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.TimeoutException;

import farn.invtweaksStapi.InvTweaksStapi;
import net.invtweaks.InvTweaks;
import net.invtweaks.config.InvTweaksConfig;
import net.invtweaks.config.SortingRule;
import net.invtweaks.library.ContainerManager;
import net.invtweaks.library.ContainerSectionManager;
import net.invtweaks.library.Obfuscation;
import net.invtweaks.tree.ItemTree;
import net.invtweaks.tree.ItemTreeItem;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import org.apache.logging.log4j.Logger;

public class SortingHandler extends Obfuscation {
	private static final Logger log = InvTweaksStapi.LOGGER;
	public static final boolean STACK_NOT_EMPTIED = true;
	public static final boolean STACK_EMPTIED = false;
	private static int[] DEFAULT_LOCK_PRIORITIES = null;
	private static boolean[] DEFAULT_FROZEN_SLOTS = null;
	private static final int MAX_CONTAINER_SIZE = 100;
	public static final int ALGORITHM_DEFAULT = 0;
	public static final int ALGORITHM_VERTICAL = 1;
	public static final int ALGORITHM_HORIZONTAL = 2;
	public static final int ALGORITHM_INVENTORY = 3;
	private ContainerSectionManager containerMgr;
	private int algorithm;
	private int size;
	private ItemTree tree;
	private Vector rules;
	private int[] rulePriority;
	private int[] keywordOrder;
	private int[] lockPriorities;
	private boolean[] frozenSlots;

	public SortingHandler(Minecraft mc, InvTweaksConfig config, ContainerManager.ContainerSection section, int algorithm) throws Exception {
		super(mc);
		int i;
		if(DEFAULT_LOCK_PRIORITIES == null) {
			DEFAULT_LOCK_PRIORITIES = new int[100];

			for(i = 0; i < 100; ++i) {
				DEFAULT_LOCK_PRIORITIES[i] = 0;
			}
		}

		if(DEFAULT_FROZEN_SLOTS == null) {
			DEFAULT_FROZEN_SLOTS = new boolean[100];

			for(i = 0; i < 100; ++i) {
				DEFAULT_FROZEN_SLOTS[i] = false;
			}
		}

		this.containerMgr = new ContainerSectionManager(mc, section);
		this.size = this.containerMgr.getSize();
		this.rules = config.getRules();
		this.tree = config.getTree();
		if(section == ContainerManager.ContainerSection.INVENTORY) {
			this.lockPriorities = config.getLockPriorities();
			this.frozenSlots = config.getFrozenSlots();
			this.algorithm = 3;
		} else {
			this.lockPriorities = DEFAULT_LOCK_PRIORITIES;
			this.frozenSlots = DEFAULT_FROZEN_SLOTS;
			this.algorithm = algorithm;
			if(algorithm != 0) {
				this.computeLineSortingRules(9, algorithm == 2);
			}
		}

		this.rulePriority = new int[this.size];
		this.keywordOrder = new int[this.size];

		for(i = 0; i < this.size; ++i) {
			this.rulePriority[i] = -1;
			ItemStack stack = this.containerMgr.getItemStack(i);
			if(stack != null) {
				this.keywordOrder[i] = this.getItemOrder(stack);
			} else {
				this.keywordOrder[i] = -1;
			}
		}

	}

	public void sort() throws TimeoutException {
		long timer = System.nanoTime();
		ContainerManager globalContainer = new ContainerManager(this.mc);
		if(this.isMultiplayerWorld()) {
			this.putHoldItemDown();
		}

		if(this.algorithm != 0) {
			int i;
			int i22;
			if(this.algorithm == 3) {
				if(globalContainer.hasSection(ContainerManager.ContainerSection.CRAFTING_IN)) {
					List rulesIt = globalContainer.getSlots(ContainerManager.ContainerSection.CRAFTING_IN);
					i = globalContainer.getFirstEmptyIndex(ContainerManager.ContainerSection.INVENTORY);
					if(i != -1) {
						Iterator rulePriority = rulesIt.iterator();

						while(rulePriority.hasNext()) {
							Slot i1 = (Slot)rulePriority.next();
							if(i1.hasStack()) {
								globalContainer.move(ContainerManager.ContainerSection.CRAFTING_IN, globalContainer.getSlotIndex(i1.id), ContainerManager.ContainerSection.INVENTORY, i);
								i = globalContainer.getFirstEmptyIndex(ContainerManager.ContainerSection.INVENTORY);
								if(i == -1) {
									break;
								}
							}
						}
					}
				}

				label144:
				for(int i15 = this.size - 1; i15 >= 0; --i15) {
					ItemStack itemStack17 = this.containerMgr.getItemStack(i15);
					if(itemStack17 != null) {
						Item item19 = itemStack17.getItem();
						if(!item19.isDamageable()) {
							i22 = 0;
							int[] i23 = this.lockPriorities;
							int i25 = i23.length;

							for(int i27 = 0; i27 < i25; ++i27) {
								Integer stackToMove = i23[i27];
								if(stackToMove.intValue() > 0) {
									ItemStack j = this.containerMgr.getItemStack(i22);
									if(j != null && itemStack17.isItemEqual(j)) {
										this.move(i15, i22, Integer.MAX_VALUE);
										this.markAsNotMoved(i22);
										if(this.containerMgr.getItemStack(i15) == null) {
											break;
										}
									}
								}

								++i22;
							}
						} else if(item19 instanceof ArmorItem) {
							ArmorItem itemArmor21 = (ArmorItem)item19;
							List from = globalContainer.getSlots(ContainerManager.ContainerSection.ARMOR);
							Iterator fromItems = from.iterator();

							while(true) {
								Slot preferredSlots;
								do {
									do {
										if(!fromItems.hasNext()) {
											continue label144;
										}

										preferredSlots = (Slot)fromItems.next();
									} while(!preferredSlots.canInsert(itemStack17));
								} while(preferredSlots.hasStack() && itemArmor21.type <= ((ArmorItem)preferredSlots.getStack().getItem()).type);

								globalContainer.move(ContainerManager.ContainerSection.INVENTORY, i15, ContainerManager.ContainerSection.ARMOR, globalContainer.getSlotIndex(preferredSlots.id));
							}
						}
					}
				}
			}

			Iterator iterator16 = this.rules.iterator();

			while(iterator16.hasNext()) {
				SortingRule sortingRule18 = (SortingRule)iterator16.next();
				int i20 = sortingRule18.getPriority();
				if(log.isDebugEnabled()) {
					log.info("Rule : " + sortingRule18.getKeyword() + "(" + i20 + ")");
				}

				for(i22 = 0; i22 < this.size; ++i22) {
					ItemStack itemStack24 = this.containerMgr.getItemStack(i22);
					if(this.hasToBeMoved(i22) && this.lockPriorities[i22] < i20) {
						List list26 = this.tree.getItems(this.getItemID(itemStack24), this.getItemDamage(itemStack24));
						if(this.tree.matches(list26, sortingRule18.getKeyword())) {
							int[] i28 = sortingRule18.getPreferredSlots();
							int i29 = i22;

							for(int i30 = 0; i30 < i28.length; ++i30) {
								int k = i28[i30];
								int moveResult = this.move(i29, k, i20);
								if(moveResult != -1) {
									if(moveResult == k) {
										break;
									}

									itemStack24 = this.containerMgr.getItemStack(moveResult);
									list26 = this.tree.getItems(this.getItemID(itemStack24), this.getItemDamage(itemStack24));
									if(!this.tree.matches(list26, sortingRule18.getKeyword())) {
										break;
									}

									i29 = moveResult;
									i30 = -1;
								}
							}
						}
					}
				}
			}

			for(i = 0; i < this.size; ++i) {
				if(this.hasToBeMoved(i) && this.lockPriorities[i] > 0) {
					this.markAsMoved(i, 1);
				}
			}
		}

		this.defaultSorting();
		if(log.isDebugEnabled()) {
			timer = System.nanoTime() - timer;
			log.info("Sorting done in " + timer + "ns");
		}

	}

	private void defaultSorting() throws TimeoutException {
		Vector remaining = new Vector();
		Vector nextRemaining = new Vector();

		int iterations;
		for(iterations = 0; iterations < this.size; ++iterations) {
			if(this.hasToBeMoved(iterations)) {
				remaining.add(iterations);
				nextRemaining.add(iterations);
			}
		}

		iterations = 0;

		label54:
		while(remaining.size() > 0 && iterations++ < 50) {
			Iterator i$ = remaining.iterator();

			while(true) {
				while(true) {
					while(i$.hasNext()) {
						int i = ((Integer)i$.next()).intValue();
						if(this.hasToBeMoved(i)) {
							for(int j = 0; j < this.size; ++j) {
								if(this.move(i, j, 1) != -1) {
									nextRemaining.removeElement(j);
									break;
								}
							}
						} else {
							nextRemaining.removeElement(i);
						}
					}

					remaining.clear();
					remaining.addAll(nextRemaining);
					continue label54;
				}
			}
		}

		if(iterations == 50) {
			log.info("Sorting takes too long, aborting.");
		}

	}

	private int putHoldItemDown() throws TimeoutException {
		ItemStack holdStack = this.getHoldStack();
		if(holdStack == null) {
			return -1;
		} else {
			for(int step = 1; step <= 2; ++step) {
				for(int i = this.size - 1; i >= 0; --i) {
					if(this.containerMgr.getItemStack(i) == null && this.lockPriorities[i] == 0 && !this.frozenSlots[i] || step == 2) {
						this.containerMgr.leftClick(i);
						return i;
					}
				}
			}

			return -1;
		}
	}

	private int move(int i, int j, int priority) throws TimeoutException {
		ItemStack from = this.containerMgr.getItemStack(i);
		ItemStack to = this.containerMgr.getItemStack(j);
		if(from != null && !this.frozenSlots[j] && !this.frozenSlots[i]) {
			if(this.lockPriorities[i] <= priority) {
				if(i == j) {
					this.markAsMoved(i, priority);
					return j;
				}

				if(to == null && this.lockPriorities[j] <= priority && !this.frozenSlots[j]) {
					this.rulePriority[i] = -1;
					this.keywordOrder[i] = -1;
					this.rulePriority[j] = priority;
					this.keywordOrder[j] = this.getItemOrder(from);
					this.containerMgr.move(i, j);
					return j;
				}

				if(to != null) {
					boolean canBeSwappedOrMerged = false;
					if(this.lockPriorities[j] <= priority) {
						if(this.rulePriority[j] < priority) {
							canBeSwappedOrMerged = true;
						} else if(this.rulePriority[j] == priority && this.isOrderedBefore(i, j)) {
							canBeSwappedOrMerged = true;
						}
					}

					if(!canBeSwappedOrMerged && from.isItemEqual(to) && this.getStackSize(to) < to.getMaxCount()) {
						canBeSwappedOrMerged = true;
					}

					if(canBeSwappedOrMerged) {
						this.keywordOrder[j] = this.keywordOrder[i];
						this.rulePriority[j] = priority;
						this.rulePriority[i] = -1;
						this.rulePriority[i] = -1;
						this.containerMgr.move(i, j);
						ItemStack remains = this.containerMgr.getItemStack(i);
						if(remains == null) {
							return j;
						}

						int dropSlot = i;
						if(this.lockPriorities[j] > this.lockPriorities[i]) {
							for(int k = 0; k < this.size; ++k) {
								if(this.containerMgr.getItemStack(k) == null && this.lockPriorities[k] == 0) {
									dropSlot = k;
									break;
								}
							}
						}

						if(dropSlot != i) {
							this.containerMgr.move(i, dropSlot);
						}

						this.rulePriority[dropSlot] = -1;
						this.keywordOrder[dropSlot] = this.getItemOrder(remains);
						return dropSlot;
					}
				}
			}

			return -1;
		} else {
			return -1;
		}
	}

	private void markAsMoved(int i, int priority) {
		this.rulePriority[i] = priority;
	}

	private void markAsNotMoved(int i) {
		this.rulePriority[i] = -1;
	}

	private boolean hasToBeMoved(int slot) {
		return this.containerMgr.getItemStack(slot) != null && this.rulePriority[slot] == -1;
	}

	private boolean isOrderedBefore(int i, int j) {
		ItemStack iStack = this.containerMgr.getItemStack(i);
		ItemStack jStack = this.containerMgr.getItemStack(j);
		if(jStack == null) {
			return true;
		} else if(iStack != null && this.keywordOrder[i] != -1) {
			if(this.keywordOrder[i] != this.keywordOrder[j]) {
				return this.keywordOrder[i] < this.keywordOrder[j];
			} else if(this.getItemID(iStack) == this.getItemID(jStack)) {
				if(this.getStackSize(iStack) != this.getStackSize(jStack)) {
					return this.getStackSize(iStack) > this.getStackSize(jStack);
				} else {
					int damageDiff = this.getItemDamage(iStack) - this.getItemDamage(jStack);
					return damageDiff < 0 && !iStack.isDamageable() || damageDiff > 0 && iStack.isDamageable();
				}
			} else {
				return this.getItemID(iStack) > this.getItemID(jStack);
			}
		} else {
			return false;
		}
	}

	private int getItemOrder(ItemStack item) {
		List items = this.tree.getItems(this.getItemID(item), this.getItemDamage(item));
		return items != null && items.size() > 0 ? ((ItemTreeItem)items.get(0)).getOrder() : Integer.MAX_VALUE;
	}

	private void computeLineSortingRules(int rowSize, boolean horizontal) {
		this.rules = new Vector();
		Map stats = this.computeContainerStats();
		ArrayList itemOrder = new ArrayList();
		int distinctItems = stats.size();
		int columnSize = this.getContainerColumnSize(rowSize);
		int availableSlots = this.size;
		int remainingStacks = 0;

		Integer hasStacksToOrderFirst;
		for(Iterator unorderedItems = stats.values().iterator(); unorderedItems.hasNext(); remainingStacks += hasStacksToOrderFirst.intValue()) {
			hasStacksToOrderFirst = (Integer)unorderedItems.next();
		}

		if(distinctItems != 0) {
			ArrayList arrayList22 = new ArrayList(stats.keySet());
			boolean z23 = true;

			while(true) {
				while(z23) {
					z23 = false;
					Iterator row = arrayList22.iterator();

					while(row.hasNext()) {
						ItemTreeItem maxRow = (ItemTreeItem)row.next();
						Integer column = (Integer)stats.get(maxRow);
						if(column.intValue() > (horizontal ? rowSize : columnSize) && !itemOrder.contains(maxRow)) {
							z23 = true;
							itemOrder.add(maxRow);
							arrayList22.remove(maxRow);
							break;
						}
					}
				}

				Collections.sort(arrayList22, Collections.reverseOrder());
				itemOrder.addAll(arrayList22);
				int spaceWidth;
				int spaceHeight;
				if(horizontal) {
					spaceHeight = 1;
					spaceWidth = rowSize / ((distinctItems + columnSize - 1) / columnSize);
				} else {
					spaceWidth = 1;
					spaceHeight = columnSize / ((distinctItems + rowSize - 1) / rowSize);
				}

				char c24 = 97;
				char c25 = (char)(c24 - 1 + columnSize);
				char c26 = 49;
				char maxColumn = (char)(c26 - 1 + rowSize);
				Iterator it = itemOrder.iterator();

				while(it.hasNext()) {
					ItemTreeItem defaultRule = (ItemTreeItem)it.next();
					int thisSpaceWidth = spaceWidth;
					int thisSpaceHeight = spaceHeight;

					while(((Integer)stats.get(defaultRule)).intValue() > thisSpaceHeight * thisSpaceWidth) {
						if(horizontal) {
							if(c26 + thisSpaceWidth < maxColumn) {
								thisSpaceWidth = maxColumn - c26 + 1;
							} else {
								if(c24 + thisSpaceHeight >= c25) {
									break;
								}

								++thisSpaceHeight;
							}
						} else if(c24 + thisSpaceHeight < c25) {
							thisSpaceHeight = c25 - c24 + 1;
						} else {
							if(c26 + thisSpaceWidth >= maxColumn) {
								break;
							}

							++thisSpaceWidth;
						}
					}

					if(horizontal && c26 + thisSpaceWidth == maxColumn) {
						++thisSpaceWidth;
					} else if(!horizontal && c24 + thisSpaceHeight == c25) {
						++thisSpaceHeight;
					}

					String constraint = c24 + "" + c26 + "-" + (char)(c24 - 1 + thisSpaceHeight) + (char)(c26 - 1 + thisSpaceWidth);
					if(!horizontal) {
						constraint = constraint + 'v';
					}

					this.rules.add(new SortingRule(this.tree, constraint, defaultRule.getName(), this.size, rowSize));
					availableSlots -= thisSpaceHeight * thisSpaceWidth;
					remainingStacks -= ((Integer)stats.get(defaultRule)).intValue();
					if(availableSlots < remainingStacks) {
						break;
					}

					if(horizontal) {
						if(c26 + thisSpaceWidth + spaceWidth <= maxColumn + 1) {
							c26 = (char)(c26 + thisSpaceWidth);
						} else {
							c26 = 49;
							c24 = (char)(c24 + thisSpaceHeight);
						}
					} else if(c24 + thisSpaceHeight + spaceHeight <= c25 + 1) {
						c24 = (char)(c24 + thisSpaceHeight);
					} else {
						c24 = 97;
						c26 = (char)(c26 + thisSpaceWidth);
					}

					if(c24 > c25 || c26 > maxColumn) {
						break;
					}
				}

				String string27;
				if(horizontal) {
					string27 = c25 + "1-a" + maxColumn;
				} else {
					string27 = "a" + maxColumn + "-" + c25 + "1v";
				}

				this.rules.add(new SortingRule(this.tree, string27, this.tree.getRootCategory().getName(), this.size, rowSize));
				return;
			}
		}
	}

	private Map computeContainerStats() {
		HashMap stats = new HashMap();
		HashMap itemSearch = new HashMap();

		for(int i = 0; i < this.size; ++i) {
			ItemStack stack = this.containerMgr.getItemStack(i);
			if(stack != null) {
				int itemSearchKey = this.getItemID(stack) * 100000 + (this.getMaxStackSize(stack) != 1 ? this.getItemDamage(stack) : 0);
				ItemTreeItem item = (ItemTreeItem)itemSearch.get(itemSearchKey);
				if(item == null) {
					item = (ItemTreeItem)this.tree.getItems(this.getItemID(stack), this.getItemDamage(stack)).get(0);
					itemSearch.put(itemSearchKey, item);
					stats.put(item, 1);
				} else {
					stats.put(item, ((Integer)stats.get(item)).intValue() + 1);
				}
			}
		}

		return stats;
	}

	private int getContainerColumnSize(int rowSize) {
		return this.size / rowSize;
	}
}

