package net.invtweaks.logic;

import java.util.ArrayList;
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

	public AutoRefillHandler(InvTweaksConfig config) {
		this.setConfig(config);
	}

	public void setConfig(InvTweaksConfig config) {
		this.config = config;
	}

	public void autoRefillSlot(int slot, int wantedId, int wantedDamage) throws Exception {
		ContainerSectionManager container = new ContainerSectionManager(ContainerManager.ContainerSection.INVENTORY);
		ItemStack replacementStack = null;
		int replacementStackSlot = -1;
		ArrayList<SortingRule> matchingRules = new ArrayList<>();
		Vector<SortingRule> rules = this.config.getRules();
		ItemTree tree = this.config.getTree();
		List<ItemTreeItem> items = tree.getItems(wantedId, wantedDamage);
        for (ItemTreeItem rule : items) {
            matchingRules.add(new SortingRule(tree, "D" + (slot - 27), rule.getName(), 36, 9));
        }


		while (true) {
            SortingRule rule = null;
			for(SortingRule targetRule : matchingRules) {
				if(targetRule.getType() == SortingRule.RuleType.TILE || targetRule.getType() == SortingRule.RuleType.COLUMN) {
					rule = targetRule;
					break;
				}
			}

			if(rule == null) {
                for (SortingRule matchingRule : matchingRules) {
                    rule = matchingRule;

                    for (int index = 0; index < 36; ++index) {
                        ItemStack candidateStack = container.getItemStack(index);
                        if (candidateStack != null) {
                            List<ItemTreeItem> list20 = tree.getItems(this.getItemID(candidateStack), this.getItemDamage(candidateStack));
                            if (tree.matches(list20, rule.getKeyword()) && (replacementStack == null || this.getStackSize(replacementStack) > this.getStackSize(candidateStack) || this.getStackSize(replacementStack) == this.getStackSize(candidateStack) && this.getMaxStackSize(replacementStack) == 1 && this.getItemDamage(replacementStack) < this.getItemDamage(candidateStack))) {
                                replacementStack = candidateStack;
                                replacementStackSlot = index;
                            }
                        }
                    }

                    if (replacementStack != null) {
                        break;
                    }
                }

				if (replacementStack != null) {
					log.info("Automatic stack replacement.");
					autoStackReplacer(replacementStackSlot, slot).start();
				}

				return;
			}

            for (int preferredSlot : rule.getPreferredSlots()) {
                if (slot == preferredSlot) {
                    matchingRules.add(rule);
                    break;
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

	private Thread autoStackReplacer(int replacementStackSlot, int slot) throws Exception {
		return (new Thread((new Runnable() {
			private ContainerSectionManager containerMgr;
			private int targetedSlot;
			private int i;
			private int expectedItemId;

			public Runnable init(Minecraft mc, int i, int currentItem) throws Exception {
				this.containerMgr = new ContainerSectionManager(ContainerManager.ContainerSection.INVENTORY);
				this.targetedSlot = currentItem;
				this.expectedItemId = AutoRefillHandler.this.getItemID(this.containerMgr.getItemStack(i));
				this.i = i;
				return this;
			}

			public void run() {
				if(AutoRefillHandler.this.isMultiplayerWorld()) {
					int e = 0;
					AutoRefillHandler.this.setHasInventoryChanged(false);

					while(!AutoRefillHandler.this.hasInventoryChanged() && e < 1500) {
						AutoRefillHandler.trySleep(3);
						++e;
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
								Minecraft.INSTANCE.world.playSound(AutoRefillHandler.this.getThePlayer(), "mob.chickenplop", 0.15F, 0.2F);
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
				} catch (NullPointerException ignored) {
				} catch (TimeoutException timeoutException4) {
					AutoRefillHandler.log.error("Failed to trigger autoreplace: " + timeoutException4.getMessage());
				}

			}
		}).init(Minecraft.INSTANCE, replacementStackSlot, slot)));
	}
}
