package net.invtweaks.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;

import net.invtweaks.Const;
import net.invtweaks.InvTweaks;
import farn.invtweaksStapi.InvTweaksStapi;
import net.invtweaks.tree.ItemTree;
import net.invtweaks.tree.ItemTreeLoader;
import org.apache.logging.log4j.Logger;

public class InvTweaksConfig {
	private static final Logger log = InvTweaksStapi.LOGGER;
	public static final String PROP_ENABLE_MIDDLE_CLICK = "enableMiddleClick";
	public static final String PROP_SHOW_CHEST_BUTTONS = "showChestButtons";
	public static final String PROP_ENABLE_SORTING_ON_PICKUP = "enableSortingOnPickup";
	public static final String PROP_ENABLE_SHORTCUTS = "enableShortcuts";
	public static final String PROP_SHORTCUT_PREFIX = "shortcutKey";
	public static final String PROP_SHORTCUT_ONE_ITEM = "shortcutKeyOneItem";
	public static final String PROP_SHORTCUT_ONE_STACK = "shortcutKeyOneStack";
	public static final String PROP_SHORTCUT_ALL_ITEMS = "shortcutKeyAllItems";
	public static final String PROP_SHORTCUT_DROP = "shortcutKeyDrop";
	public static final String PROP_SHORTCUT_UP = "shortcutKeyToUpperSection";
	public static final String PROP_SHORTCUT_DOWN = "shortcutKeyToLowerSection";
	public static final String PROP_ENABLE_SORTING_SOUND = "enableSortingSound";
	public static final String PROP_ENABLE_AUTO_REFILL_SOUND = "enableAutoRefillSound";
	public static final String VALUE_TRUE = "true";
	public static final String VALUE_FALSE = "false";
	public static final Object VALUE_DEFAULT = "DEFAULT";
	public static final String VALUE_CI_COMPATIBILITY = "convenientInventoryCompatibility";
	public static final String LOCKED = "LOCKED";
	public static final String FROZEN = "FROZEN";
	public static final String AUTOREPLACE = "AUTOREPLACE";
	public static final String AUTOREPLACE_NOTHING = "nothing";
	public static final String DEBUG = "DEBUG";
	public static final boolean DEFAULT_AUTO_REFILL_BEHAVIOUR = true;
	private String rulesFile;
	private String treeFile;
	private InvTweaksProperties properties;
	private ItemTree tree;
	private Vector rulesets;
	private int currentRuleset = 0;
	private String currentRulesetName = null;
	private Vector invalidKeywords;
	private long storedConfigLastModified;

	public InvTweaksConfig(String rulesFile, String treeFile) {
		this.rulesFile = rulesFile;
		this.treeFile = treeFile;
		this.reset();
	}

	public void load() throws Exception {
		synchronized(this) {
			this.reset();
			this.loadProperties();
			this.saveProperties();
			this.tree = (new ItemTreeLoader()).load(this.treeFile);
			File f = new File(this.rulesFile);
			char[] bytes = new char[(int)f.length()];
			FileReader reader = new FileReader(f);
			reader.read(bytes);
			String[] configLines = String.valueOf(bytes).replace("\r\n", "\n").replace('\r', '\n').split("\n");
			InventoryConfigRuleset activeRuleset = new InventoryConfigRuleset(this.tree, "Default");
			boolean defaultRuleset = true;
			boolean defaultRulesetEmpty = true;
			String[] rulesetIndex = configLines;
			int i$ = configLines.length;

			for(int ruleset = 0; ruleset < i$; ++ruleset) {
				String line = rulesetIndex[ruleset];
				if(line.matches("^[\\w]*\\:$")) {
					if(!defaultRuleset || !defaultRulesetEmpty) {
						activeRuleset.finalize();
						this.rulesets.add(activeRuleset);
					}

					activeRuleset = new InventoryConfigRuleset(this.tree, line.substring(0, line.length() - 1));
				}

				try {
					String invalidKeyword = activeRuleset.registerLine(line);
					if(defaultRuleset) {
						defaultRulesetEmpty = false;
					}

					if(invalidKeyword != null) {
						this.invalidKeywords.add(invalidKeyword);
					}
				} catch (InvalidParameterException invalidParameterException16) {
				}
			}

			activeRuleset.finalize();
			this.rulesets.add(activeRuleset);
			this.currentRuleset = 0;
			if(this.currentRulesetName != null) {
				int i18 = 0;

				for(Iterator iterator19 = this.rulesets.iterator(); iterator19.hasNext(); ++i18) {
					InventoryConfigRuleset inventoryConfigRuleset20 = (InventoryConfigRuleset)iterator19.next();
					if(inventoryConfigRuleset20.getName().equals(this.currentRulesetName)) {
						this.currentRuleset = i18;
						break;
					}
				}
			}

			if(this.currentRuleset == 0) {
				if(!this.rulesets.isEmpty()) {
					this.currentRulesetName = ((InventoryConfigRuleset)this.rulesets.get(this.currentRuleset)).getName();
				} else {
					this.currentRulesetName = null;
				}
			}

		}
	}

	public boolean refreshProperties() throws IOException {
		long configLastModified = (new File(Const.CONFIG_PROPS_FILE)).lastModified();
		if(this.storedConfigLastModified != configLastModified) {
			this.storedConfigLastModified = configLastModified;
			this.loadProperties();
			return true;
		} else {
			return false;
		}
	}

	public void saveProperties() {
		File configPropsFile = this.getPropertyFile();
		if(configPropsFile.exists()) {
			try {
				FileOutputStream e = new FileOutputStream(configPropsFile);
				this.properties.store(e, "Inventory Tweaks Configuration\n(Regarding shortcuts, all key names can be found at: http://www.lwjgl.org/javadoc/org/lwjgl/input/Keyboard.html)");
				e.flush();
				e.close();
				this.storedConfigLastModified = (new File(Const.CONFIG_PROPS_FILE)).lastModified();
			} catch (IOException iOException3) {
				InvTweaks.logInGameStatic("Failed to save config file " + Const.CONFIG_PROPS_FILE);
			}
		}

	}

	public Map getProperties(String prefix) {
		HashMap result = new HashMap();
		Iterator i$ = this.properties.keySet().iterator();

		while(i$.hasNext()) {
			Object o = i$.next();
			String key = (String)o;
			if(key.startsWith(prefix)) {
				result.put(key, this.properties.getProperty(key));
			}
		}

		return result;
	}

	public String getProperty(String key) {
		return this.properties.getProperty(key);
	}

	public void setProperty(String key, String value) {
		this.properties.put(key, value);
		this.saveProperties();

	}

	public ItemTree getTree() {
		return this.tree;
	}

	public String getCurrentRulesetName() {
		return this.currentRulesetName;
	}

	public String switchConfig(int i) {
		if(!this.rulesets.isEmpty() && i < this.rulesets.size()) {
			this.currentRuleset = i;
			this.currentRulesetName = ((InventoryConfigRuleset)this.rulesets.get(this.currentRuleset)).getName();
			return this.currentRulesetName;
		} else {
			return null;
		}
	}

	public String switchConfig() {
		return this.currentRuleset == -1 ? this.switchConfig(0) : this.switchConfig((this.currentRuleset + 1) % this.rulesets.size());
	}

	public Vector getRules() {
		return ((InventoryConfigRuleset)this.rulesets.get(this.currentRuleset)).getRules();
	}

	public Vector getInvalidKeywords() {
		return this.invalidKeywords;
	}

	public int[] getLockPriorities() {
		return ((InventoryConfigRuleset)this.rulesets.get(this.currentRuleset)).getLockPriorities();
	}

	public boolean[] getFrozenSlots() {
		return ((InventoryConfigRuleset)this.rulesets.get(this.currentRuleset)).getFrozenSlots();
	}

	public Vector getLockedSlots() {
		return ((InventoryConfigRuleset)this.rulesets.get(this.currentRuleset)).getLockedSlots();
	}

	public Level getLogLevel() {
		return ((InventoryConfigRuleset)this.rulesets.get(this.currentRuleset)).isDebugEnabled() ? Level.INFO : Level.WARNING;
	}

	public boolean isAutoRefillEnabled(int itemID, int itemDamage) {
		List items = this.tree.getItems(itemID, itemDamage);
		Vector autoReplaceRules = ((InventoryConfigRuleset)this.rulesets.get(this.currentRuleset)).getAutoReplaceRules();
		boolean found = false;
		Iterator i$ = autoReplaceRules.iterator();

		while(i$.hasNext()) {
			String keyword = (String)i$.next();
			if(keyword.equals("nothing")) {
				return false;
			}

			if(this.tree.matches(items, keyword)) {
				found = true;
			}
		}

		if(found) {
			return true;
		} else if(autoReplaceRules.isEmpty()) {
			return true;
		} else {
			return false;
		}
	}

	private void reset() {
		this.rulesets = new Vector();
		this.currentRuleset = -1;
		this.properties = new InvTweaksProperties();
		this.properties.put("enableMiddleClick", "true");
		this.properties.put("showChestButtons", "true");
		this.properties.put("enableSortingOnPickup", "true");
		this.properties.put("enableAutoRefillSound", "true");
		this.properties.put("enableSortingSound", "true");
		this.properties.put("enableShortcuts", "true");
		this.properties.put("shortcutKeyAllItems", "LSHIFT, RSHIFT");
		this.properties.put("shortcutKeyOneItem", "LCONTROL, RCONTROL");
		this.properties.put("shortcutKeyOneStack", VALUE_DEFAULT);
		this.properties.put("shortcutKeyToUpperSection", "UP");
		this.properties.put("shortcutKeyToLowerSection", "DOWN");
		this.properties.put("shortcutKeyDrop", "LALT, RALT");
		this.invalidKeywords = new Vector();
	}

	private void loadProperties() throws IOException {
		File configPropsFile = this.getPropertyFile();
		if(configPropsFile != null) {
			FileInputStream fis = new FileInputStream(configPropsFile);
			this.properties.load(fis);
			fis.close();
		}

		this.properties.sortKeys();
		if(((String)this.properties.get("shortcutKeyDrop")).contains("META")) {
			this.properties.setProperty("shortcutKeyDrop", "LALT, RALT");
		}

		if(((String)this.properties.get("shortcutKeyOneItem")).contains("CTRL")) {
			this.properties.setProperty("shortcutKeyOneItem", "LCONTROL, RCONTROL");
		}

		if(this.properties.contains("enableAutoreplaceSound")) {
			this.properties.put("enableAutoRefillSound", (String)this.properties.get("enableAutoreplaceSound"));
			this.properties.remove("enableAutoreplaceSound");
		}

	}

	private File getPropertyFile() {
		File configPropsFile = new File(Const.CONFIG_PROPS_FILE);
		if(!configPropsFile.exists()) {
			try {
				configPropsFile.createNewFile();
			} catch (IOException iOException3) {
				InvTweaks.logInGameStatic("Failed to create the config file " + Const.CONFIG_PROPS_FILE);
				return null;
			}
		}

		return configPropsFile;
	}
}
