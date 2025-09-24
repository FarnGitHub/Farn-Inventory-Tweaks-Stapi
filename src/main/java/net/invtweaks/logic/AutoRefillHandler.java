package net.invtweaks.logic;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.TimeoutException;

import farn.invtweaksStapi.InvTweaksStapi;
import net.invtweaks.config.InvTweaksConfig;
import net.invtweaks.config.SortingRule;
import net.invtweaks.library.ContainerManager;
import net.invtweaks.library.ContainerSectionManager;
import net.invtweaks.library.Obfuscation;
import net.invtweaks.tree.ItemTree;
import net.invtweaks.tree.ItemTreeItem;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import org.apache.logging.log4j.Logger;

public class AutoRefillHandler extends Obfuscation {
	private static final Logger log = InvTweaksStapi.LOGGER;
	private InvTweaksConfig config = null;

	public AutoRefillHandler(Minecraft mc, InvTweaksConfig config) {
		super(mc);
		this.setConfig(config);
	}

	public void setConfig(InvTweaksConfig config) {
		this.config = config;
	}

	public void autoRefillSlot(int slot, int wantedId, int wantedDamage) throws Exception {
		ContainerSectionManager container = new ContainerSectionManager(this.mc, ContainerManager.ContainerSection.INVENTORY);
		ItemStack replacementStack = null;
		int replacementStackSlot = -1;
		ArrayList matchingRules = new ArrayList();
		Vector rules = this.config.getRules();
		ItemTree tree = this.config.getTree();
		List items = tree.getItems(wantedId, wantedDamage);
		Iterator i$ = items.iterator();

		while(i$.hasNext()) {
			ItemTreeItem rule = (ItemTreeItem)i$.next();
			matchingRules.add(new SortingRule(tree, "D" + (slot - 27), rule.getName(), 36, 9));
		}

		i$ = rules.iterator();

		while(true) {
			while(true) {
				SortingRule sortingRule18;
				do {
					if(!i$.hasNext()) {
						i$ = matchingRules.iterator();

						while(i$.hasNext()) {
							sortingRule18 = (SortingRule)i$.next();

							for(int i19 = 0; i19 < 36; ++i19) {
								ItemStack candidateStack = container.getItemStack(i19);
								if(candidateStack != null) {
									List list20 = tree.getItems(this.getItemID(candidateStack), this.getItemDamage(candidateStack));
									if(tree.matches(list20, sortingRule18.getKeyword()) && (replacementStack == null || this.getStackSize(replacementStack) > this.getStackSize(candidateStack) || this.getStackSize(replacementStack) == this.getStackSize(candidateStack) && this.getMaxStackSize(replacementStack) == 1 && this.getItemDamage(replacementStack) < this.getItemDamage(candidateStack))) {
										replacementStack = candidateStack;
										replacementStackSlot = i19;
									}
								}
							}

							if(replacementStack != null) {
								break;
							}
						}

						if(replacementStack != null) {
							log.info("Automatic stack replacement.");
							(new Thread((new Runnable() {
								private ContainerSectionManager containerMgr;
								private int targetedSlot;
								private int i;
								private int expectedItemId;

								public Runnable init(Minecraft mc, int i, int currentItem) throws Exception {
									this.containerMgr = new ContainerSectionManager(mc, ContainerManager.ContainerSection.INVENTORY);
									this.targetedSlot = currentItem;
									this.expectedItemId = AutoRefillHandler.this.getItemID(this.containerMgr.getItemStack(i));
									this.i = i;
									return this;
								}

								public void run() {
									if(AutoRefillHandler.this.isMultiplayerWorld()) {
										byte e = 0;
										AutoRefillHandler.this.setHasInventoryChanged(false);

										while(!AutoRefillHandler.this.hasInventoryChanged() && e < 1500) {
											AutoRefillHandler.trySleep(3);
										}

										if(e < 200) {
											AutoRefillHandler.trySleep(200 - e);
										}

										if(e >= 1500) {
											AutoRefillHandler.log.warn("Autoreplace timout");
										}
									} else {
										AutoRefillHandler.trySleep(200);
									}

									try {
										ItemStack itemStack5 = this.containerMgr.getItemStack(this.i);
										if(itemStack5 != null && AutoRefillHandler.this.getItemID(itemStack5) == this.expectedItemId) {
											if(this.containerMgr.move(this.i, this.targetedSlot)) {
												if(!AutoRefillHandler.this.config.getProperty("enableAutoRefillSound").equals("false")) {
													AutoRefillHandler.this.mc.world.playSound(AutoRefillHandler.this.getThePlayer(), "mob.chickenplop", 0.15F, 0.2F);
												}

												if(this.containerMgr.getItemStack(this.i) != null && this.i >= 27) {
													for(int j = 0; j < 36; ++j) {
														if(this.containerMgr.getItemStack(j) == null) {
															this.containerMgr.move(this.i, j);
															break;
														}
													}
												}
											} else {
												AutoRefillHandler.log.warn("Failed to move stack for autoreplace, despite of prior tests.");
											}
										}
									} catch (NullPointerException nullPointerException3) {
									} catch (TimeoutException timeoutException4) {
										AutoRefillHandler.log.error("Failed to trigger autoreplace: " + timeoutException4.getMessage());
									}

								}
							}).init(this.mc, replacementStackSlot, slot))).start();
						}

						return;
					}

					sortingRule18 = (SortingRule)i$.next();
				} while(sortingRule18.getType() != SortingRule.RuleType.TILE && sortingRule18.getType() != SortingRule.RuleType.COLUMN);

				int[] i = sortingRule18.getPreferredSlots();
				int candidateItems = i.length;

				for(int i$1 = 0; i$1 < candidateItems; ++i$1) {
					int preferredSlot = i[i$1];
					if(slot == preferredSlot) {
						matchingRules.add(sortingRule18);
						break;
					}
				}
			}
		}
	}

	private static void trySleep(int delay) {
		try {
			Thread.sleep((long)delay);
		} catch (InterruptedException interruptedException2) {
		}

	}
}
