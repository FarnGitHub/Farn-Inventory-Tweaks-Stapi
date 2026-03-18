package net.invtweaks.config;

import java.security.InvalidParameterException;
import java.util.Collections;
import java.util.Vector;

import net.invtweaks.tree.ItemTree;

public class InventoryConfigRuleset {
	//private static final Logger log = InvTweaksStapi.LOGGER;
	private final String name;
	private final int[] lockPriorities;
	private final boolean[] frozenSlots;
	private final Vector<Integer> lockedSlots;
	private final Vector<SortingRule> rules;
	private final Vector<String> autoReplaceRules;
	private boolean debugEnabled;
	private final ItemTree tree;

	public InventoryConfigRuleset(ItemTree tree, String name) {
		this.tree = tree;
		this.name = name;
		this.lockPriorities = new int[36];

		int i;
		for(i = 0; i < this.lockPriorities.length; ++i) {
			this.lockPriorities[i] = 0;
		}

		this.frozenSlots = new boolean[36];

		for(i = 0; i < this.frozenSlots.length; ++i) {
			this.frozenSlots[i] = false;
		}

		this.lockedSlots = new Vector<>();
		this.rules = new Vector<>();
		this.autoReplaceRules = new Vector<>();
		this.debugEnabled = false;
	}

	public String registerLine(String rawLine) throws InvalidParameterException {
		String[] words = rawLine.split(" ");
		String lineText = rawLine.toLowerCase();
		SortingRule newRule = null;
		if(words.length == 2) {
			if(lineText.matches("^([a-d]|[1-9]|[r]){1,2} [\\w]*$") || lineText.matches("^[a-d][1-9]-[a-d][1-9][rv]?[rv]? [\\w]*$")) {
				words[0] = words[0].toLowerCase();
				int[] i11;
				int i16;
				int i17;
				if(words[1].equals("LOCKED")) {
					i11 = SortingRule.getRulePreferredPositions(words[0], 36, 9);
					int i13 = SortingRule.getRuleType(words[0]).getHighestPriority();
					int[] i15 = i11;
					i16 = i11.length;

					for(i17 = 0; i17 < i16; ++i17) {
						int i = i15[i17];
						this.lockPriorities[i] = i13;
					}

					return null;
				} else if(words[1].equals("FROZEN")) {
					i11 = SortingRule.getRulePreferredPositions(words[0], 36, 9);
					int[] i12 = i11;
					int i14 = i11.length;

					for(i16 = 0; i16 < i14; ++i16) {
						i17 = i12[i16];
						this.frozenSlots[i17] = true;
					}

					return null;
				} else {
					String keyword = words[1].toLowerCase();
					boolean isValidKeyword = this.tree.isKeywordValid(keyword);
					if(!isValidKeyword) {
						Vector<String> wordVariants = this.getKeywordVariants(keyword);

						for(String wordVariant : wordVariants) {
							if(this.tree.isKeywordValid(wordVariant.toLowerCase())) {
								isValidKeyword = true;
								keyword = wordVariant;
								break;
							}
						}
					}

					if(isValidKeyword) {
						newRule = new SortingRule(this.tree, words[0], keyword.toLowerCase(), 36, 9);
						this.rules.add(newRule);
						return null;
					} else {
						return keyword.toLowerCase();
					}
				}
			}

			if(words[0].equals("AUTOREPLACE")) {
				words[1] = words[1].toLowerCase();
				if(this.tree.isKeywordValid(words[1]) || words[1].equals("nothing")) {
					this.autoReplaceRules.add(words[1]);
				}

				return null;
			}
		} else if(words.length == 1 && words[0].equals("DEBUG")) {
			this.debugEnabled = true;
			return null;
		}

		throw new InvalidParameterException();
	}

	public void resortInv() {
		if(this.autoReplaceRules.isEmpty()) {
			try {
				this.autoReplaceRules.add(this.tree.getRootCategory().getName());
			} catch (NullPointerException nullPointerException2) {
				throw new NullPointerException("No root category is defined.");
			}
		}

		this.rules.sort(Collections.reverseOrder());

		for(int i = 0; i < this.lockPriorities.length; ++i) {
			if(this.lockPriorities[i] > 0) {
				this.lockedSlots.add(i);
			}
		}

	}

	public String getName() {
		return this.name;
	}

	public int[] getLockPriorities() {
		return this.lockPriorities;
	}

	public boolean[] getFrozenSlots() {
		return this.frozenSlots;
	}

	public Vector<Integer> getLockedSlots() {
		return this.lockedSlots;
	}

	public Vector<SortingRule> getRules() {
		return this.rules;
	}

	public Vector<String> getAutoReplaceRules() {
		return this.autoReplaceRules;
	}

	public boolean isDebugEnabled() {
		return this.debugEnabled;
	}

	private Vector<String> getKeywordVariants(String keyword) {
		Vector<String> variants = new Vector<>();
		if(keyword.endsWith("es")) {
			variants.add(keyword.substring(0, keyword.length() - 2));
		}

		if(keyword.endsWith("s")) {
			variants.add(keyword.substring(0, keyword.length() - 1));
		}

		if(keyword.contains("en")) {
			variants.add(keyword.replaceAll("en", ""));
		} else {
			if(keyword.contains("wood")) {
				variants.add(keyword.replaceAll("wood", "wooden"));
			}

			if(keyword.contains("gold")) {
				variants.add(keyword.replaceAll("gold", "golden"));
			}
		}

		if(keyword.matches("\\w*[A-Z]\\w*")) {
			byte[] keywordBytes = keyword.getBytes();

			for(int i = 0; i < keywordBytes.length; ++i) {
				if(keywordBytes[i] >= 65 && keywordBytes[i] <= 90) {
					String swapped = (keyword.substring(i) + keyword.substring(0, i)).toLowerCase();
					variants.add(swapped);
					variants.addAll(this.getKeywordVariants(swapped));
				}
			}
		}

		return variants;
	}
}
