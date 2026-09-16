package net.invtweaks.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.invtweaks.Const;
import net.invtweaks.InvTweaks;
import net.invtweaks.tree.ItemTree;
import net.invtweaks.tree.ItemTreeItem;
import net.invtweaks.tree.ItemTreeLoader;
import org.lwjgl.input.Keyboard;

/**
 * The global mod's configuration.
 * 
 * @author Jimeo Wan
 *
 */
public class InvTweaksConfig {

    @SuppressWarnings("unused")
    private static final Logger log = Logger.getLogger("InvTweaks");

    // Sorting settings
    public static final String PROP_ENABLE_MIDDLE_CLICK = "enableMiddleClick";
    public static final String PROP_SHOW_CHEST_BUTTONS = "showChestButtons";
    public static final String PROP_ENABLE_SORTING_ON_PICKUP = "enableSortingOnPickup";
    public static final String PROP_SORT_KEY = "sortKey";
    
    // Shortcuts
    public static final String PROP_ENABLE_SHORTCUTS = "enableShortcuts";
    public static final String PROP_SHORTCUT_PREFIX = "shortcutKey";
    public static final String PROP_SHORTCUT_ONE_ITEM = "shortcutKeyOneItem";
    public static final String PROP_SHORTCUT_ONE_STACK = "shortcutKeyOneStack";
    public static final String PROP_SHORTCUT_ALL_ITEMS = "shortcutKeyAllItems";
    public static final String PROP_SHORTCUT_DROP = "shortcutKeyDrop";
    public static final String PROP_SHORTCUT_UP = "shortcutKeyToUpperSection";
    public static final String PROP_SHORTCUT_DOWN = "shortcutKeyToLowerSection";
    
    // Sound
    public static final String PROP_ENABLE_SORTING_SOUND = "enableSortingSound";
    public static final String PROP_ENABLE_AUTO_REFILL_SOUND = "enableAutoRefillSound";

    public static final String VALUE_TRUE = "true";
    public static final String VALUE_FALSE = "false";
    public static final Object VALUE_DEFAULT = "DEFAULT"; // For shortcuts

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
    private Vector<InventoryConfigRuleset> rulesets;
    private int currentRuleset = 0;
    private String currentRulesetName = null;
    private Vector<String> invalidKeywords;

    private long storedConfigLastModified;

    /**
     * Creates a new configuration holder. The configuration is not yet loaded.
     */
    public InvTweaksConfig(String rulesFile, String treeFile) {
        this.rulesFile = rulesFile;
        this.treeFile = treeFile;
        reset();
    }

    public void load() throws Exception {
        
        synchronized (this) {

            // Reset all
            reset();

            // Load properties
            loadProperties();
            saveProperties(); // Needed to append non-saved properties to the file

            // Load tree
            tree = new ItemTreeLoader().load(treeFile);

            // Read file
            File f = new File(rulesFile);
            char[] bytes = new char[(int) f.length()];
            FileReader reader = new FileReader(f);
            reader.read(bytes);

            // Split lines into an array
            String[] configLines = String.valueOf(bytes)
                    .replace("\r\n", "\n").replace('\r', '\n').split("\n");

            // Register rules in various configurations (rulesets)
            InventoryConfigRuleset activeRuleset = new InventoryConfigRuleset(tree, "Default");
            boolean defaultRuleset = true, defaultRulesetEmpty = true;
            String invalidKeyword;

            for (String line : configLines) {
                // Change ruleset
                if (line.matches("^[\\w]*\\:$")) {
                    // Make sure not to add an empty default config to the
                    // rulesets
                    if (!defaultRuleset || !defaultRulesetEmpty) {
                        activeRuleset.finalizeRules();
                        rulesets.add(activeRuleset);
                    }
                    activeRuleset = new InventoryConfigRuleset(tree, 
                            line.substring(0, line.length() - 1));
                }

                // Register line
                try {
                    invalidKeyword = activeRuleset.registerLine(line);
                    if (defaultRuleset) {
                        defaultRulesetEmpty = false;
                    }
                    if (invalidKeyword != null) {
                        invalidKeywords.add(invalidKeyword);
                    }
                } catch (InvalidParameterException e) {
                    // Invalid line (comments), no problem
                }
            }

            // Finalize
            activeRuleset.finalizeRules();
            rulesets.add(activeRuleset);
            
            // If a specific ruleset was loaded, 
            // try to choose the same again, else load the first one
            currentRuleset = 0;
            if (currentRulesetName != null) {
                int rulesetIndex = 0;
                for (InventoryConfigRuleset ruleset : rulesets) {
                    if (ruleset.getName().equals(currentRulesetName)) {
                        currentRuleset = rulesetIndex;
                        break;
                    }
                    rulesetIndex++;
                }
            }
            if (currentRuleset == 0) {
                if (!rulesets.isEmpty()) {
                    currentRulesetName = rulesets.get(currentRuleset).getName();
                }
                else {
                    currentRulesetName = null;
                }
            }

        }

    }

    public boolean refreshProperties() throws IOException {
        // Check time of last edit
        long configLastModified = new File(Const.CONFIG_PROPS_FILE).lastModified();
        if (storedConfigLastModified != configLastModified) {
            storedConfigLastModified = configLastModified;
            loadProperties();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Saves properties
     */
    public void saveProperties() {
        File configPropsFile = getPropertyFile();
        if (configPropsFile.exists()) {
            try {
                FileOutputStream fos = new FileOutputStream(configPropsFile);
                properties.store(fos, "Inventory Tweaks Configuration\n"+
                        "(Regarding shortcuts, all key names can be found at: http://www.lwjgl.org/javadoc/org/lwjgl/input/Keyboard.html)");
                fos.flush();
                fos.close();
                storedConfigLastModified = new File(Const.CONFIG_PROPS_FILE).lastModified();
            } catch (IOException e) {
                InvTweaks.logInGameStatic("Failed to save config file " +
                        Const.CONFIG_PROPS_FILE);
            }
        }
    }

    public Map<String, String> getProperties(String prefix) {
        Map<String, String> result = new HashMap<String, String>();
        for (Object o : properties.keySet()) {
            String key = (String) o; 
            if (key.startsWith(prefix)) {
                result.put(key, properties.getProperty(key));
            }
        }
        return result;
    }
    
    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    public void setProperty(String key, String value) {
        properties.put(key, value);
        saveProperties();
    }

    public ItemTree getTree() {
        return tree;
    }

    public String getCurrentRulesetName() {
        return currentRulesetName;
    }

    /**
     * 
     * @param i from 0 to n-1, n being the number of available configurations.
     * @return null if the given ID is invalid
     */
    public String switchConfig(int i) {
        if (!rulesets.isEmpty() && i < rulesets.size()) {
            currentRuleset = i;
            currentRulesetName = rulesets.get(currentRuleset).getName();
            return currentRulesetName;
        } else {
            return null;
        }
    }
    
    public String switchConfig() {
        if (currentRuleset == -1) {
            return switchConfig(0);
        } else {
            return switchConfig((currentRuleset + 1) % rulesets.size());
        }
    }

    /**
     * Returns all sorting rules, themselves sorted by decreasing priority.
     * 
     * @return
     */
    public Vector<SortingRule> getRules() {
        return rulesets.get(currentRuleset).getRules();
    }

    /**
     * Returns all invalid keywords wrote in the config file.
     */
    public Vector<String> getInvalidKeywords() {
        return invalidKeywords;
    }

    /**
     * @return The locked slots array with locked priorities. WARNING: Not a
     *         copy.
     */
    public int[] getLockPriorities() {
        return rulesets.get(currentRuleset).getLockPriorities();
    }

    /**
     * @return The inventory slots array indicating which ones are frozen.
     *         WARNING: Not a copy.
     */
    public boolean[] getFrozenSlots() {
        return rulesets.get(currentRuleset).getFrozenSlots();
    }

    /**
     * @return The locked slots only
     */
    public Vector<Integer> getLockedSlots() {
        return rulesets.get(currentRuleset).getLockedSlots();
    }

    public Level getLogLevel() {
        return (rulesets.get(currentRuleset).isDebugEnabled())
                ? Level.INFO : Level.WARNING;
    }

    public boolean isAutoRefillEnabled(int itemID, int itemDamage) {
        List<ItemTreeItem> items = tree.getItems(itemID, itemDamage);
        Vector<String> autoReplaceRules = rulesets.get(currentRuleset).getAutoReplaceRules();
        boolean found = false;
        for (String keyword : autoReplaceRules) {
            if (keyword.equals(AUTOREPLACE_NOTHING))
                return false;
            if (tree.matches(items, keyword))
                found = true;
        }
        if (found)
            return true;
        else {
            if (autoReplaceRules.isEmpty()) {
                return DEFAULT_AUTO_REFILL_BEHAVIOUR;
            } else {
                return false;
            }
        }
    }

    private void reset() {
        rulesets = new Vector<>();
        currentRuleset = -1;

        // Default property values
        properties = new InvTweaksProperties();
        
        properties.put(PROP_ENABLE_MIDDLE_CLICK, VALUE_TRUE);
        properties.put(PROP_SHOW_CHEST_BUTTONS, VALUE_TRUE);
        properties.put(PROP_ENABLE_SORTING_ON_PICKUP, VALUE_TRUE);
        properties.put(PROP_ENABLE_AUTO_REFILL_SOUND, VALUE_TRUE);
        properties.put(PROP_ENABLE_SORTING_SOUND, VALUE_TRUE);
        properties.put(PROP_ENABLE_SHORTCUTS, VALUE_TRUE);
        properties.put(PROP_SORT_KEY, Keyboard.getKeyName(Keyboard.KEY_R));
        
        properties.put(PROP_SHORTCUT_ALL_ITEMS, "LSHIFT, RSHIFT");
        properties.put(PROP_SHORTCUT_ONE_ITEM, "LCONTROL, RCONTROL");
        properties.put(PROP_SHORTCUT_ONE_STACK, VALUE_DEFAULT);
        properties.put(PROP_SHORTCUT_UP, "UP");
        properties.put(PROP_SHORTCUT_DOWN, "DOWN");
        properties.put(PROP_SHORTCUT_DROP, "LALT, RALT");


        invalidKeywords = new Vector<>();
    }

    private void loadProperties() throws IOException {
        File configPropsFile = getPropertyFile();
        if (configPropsFile != null) {
            FileInputStream fis = new FileInputStream(configPropsFile);
            properties.load(fis);
            fis.close();
        }
        properties.sortKeys();
        
        // 1.30 patch: rename wrong shortcuts
        if (((String) properties.get(PROP_SHORTCUT_DROP)).contains("META"))
            properties.setProperty(PROP_SHORTCUT_DROP, "LALT, RALT");
        if (((String) properties.get(PROP_SHORTCUT_ONE_ITEM)).contains("CTRL"))
            properties.setProperty(PROP_SHORTCUT_ONE_ITEM, "LCONTROL, RCONTROL");
        
        // Retro-compatibility: rename autoreplace
        if (properties.contains("enableAutoreplaceSound")) {
            properties.put(PROP_ENABLE_AUTO_REFILL_SOUND, properties.get("enableAutoreplaceSound"));
            properties.remove("enableAutoreplaceSound");
        }

        Const.SORT_KEY_BINDING.code = Keyboard.getKeyIndex(properties.getProperty(PROP_SORT_KEY, Keyboard.getKeyName(Keyboard.KEY_R)));
    }

    /**
     * Returns the file when the properties are stored, after making sure the
     * file exists.
     * 
     * @return May return null in case of failure while creating the file.
     */
    private File getPropertyFile() {
        File configPropsFile = new File(Const.CONFIG_PROPS_FILE);
        if (!configPropsFile.exists()) {
            try {
                configPropsFile.createNewFile();
            } catch (IOException e) {
                InvTweaks.logInGameStatic("Failed to create the config file "
                        + Const.CONFIG_PROPS_FILE);
                return null;
            }
        }
        return configPropsFile;
    }

}
