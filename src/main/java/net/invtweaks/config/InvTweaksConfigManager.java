package net.invtweaks.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import net.invtweaks.Const;
import net.invtweaks.InvTweaks;
import farn.invtweaksStapi.InvTweaksStapi;
import net.invtweaks.logic.AutoRefillHandler;
import net.invtweaks.logic.ShortcutsHandler;
import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.Logger;

public class InvTweaksConfigManager {
	private static final Logger log = InvTweaksStapi.LOGGER;
	private Minecraft mc;
	private InvTweaksConfig config = null;
	private long storedConfigLastModified = 0L;
	private AutoRefillHandler autoRefillHandler = null;
	private ShortcutsHandler shortcutsHandler = null;

	public InvTweaksConfigManager(Minecraft mc) {
		this.mc = mc;
	}

	public boolean makeSureConfigurationIsLoaded() {
		try {
			if(this.config != null && this.config.refreshProperties()) {
				this.shortcutsHandler = new ShortcutsHandler(this.mc, this.config);
				InvTweaks.logInGameStatic("Mod properties loaded");
			}
		} catch (IOException iOException3) {
			InvTweaks.logInGameErrorStatic("Failed to refresh properties from file", iOException3);
		}

		long configLastModified = this.computeConfigLastModified();
		if(this.config != null) {
			return this.storedConfigLastModified != configLastModified ? this.loadConfig() : true;
		} else {
			this.storedConfigLastModified = configLastModified;
			return this.loadConfig();
		}
	}

	public InvTweaksConfig getConfig() {
		return this.config;
	}

	public AutoRefillHandler getAutoRefillHandler() {
		return this.autoRefillHandler;
	}

	public ShortcutsHandler getShortcutsHandler() {
		return this.shortcutsHandler;
	}

	private long computeConfigLastModified() {
		return (new File(Const.CONFIG_RULES_FILE)).lastModified() + (new File(Const.CONFIG_TREE_FILE)).lastModified();
	}

	private boolean loadConfig() {
		if((new File(Const.OLDER_CONFIG_RULES_FILE)).exists()) {
			if((new File(Const.CONFIG_RULES_FILE)).exists()) {
				this.backupFile(new File(Const.CONFIG_RULES_FILE), Const.CONFIG_RULES_FILE);
			}

			(new File(Const.OLDER_CONFIG_RULES_FILE)).renameTo(new File(Const.CONFIG_RULES_FILE));
		}

		if((new File(Const.OLDER_CONFIG_TREE_FILE)).exists()) {
			this.backupFile(new File(Const.OLDER_CONFIG_TREE_FILE), Const.CONFIG_TREE_FILE);
		}

		if((new File(Const.OLD_CONFIG_TREE_FILE)).exists()) {
			(new File(Const.OLD_CONFIG_TREE_FILE)).renameTo(new File(Const.CONFIG_TREE_FILE));
		}

		if(!(new File(Const.CONFIG_RULES_FILE)).exists() && this.extractFile("/net/invtweaks/DefaultConfig.dat", Const.CONFIG_RULES_FILE)) {
			InvTweaks.logInGameStatic(Const.CONFIG_RULES_FILE + " missing, creating default one.");
		}

		if(!(new File(Const.CONFIG_TREE_FILE)).exists() && this.extractFile("/net/invtweaks/DefaultTree.dat", Const.CONFIG_TREE_FILE)) {
			InvTweaks.logInGameStatic(Const.CONFIG_TREE_FILE + " missing, creating default one.");
		}

		this.storedConfigLastModified = this.computeConfigLastModified();
		String error = null;

		try {
			if(this.config == null) {
				this.config = new InvTweaksConfig(Const.CONFIG_RULES_FILE, Const.CONFIG_TREE_FILE);
				this.autoRefillHandler = new AutoRefillHandler(this.mc, this.config);
				this.shortcutsHandler = new ShortcutsHandler(this.mc, this.config);
			}

			this.config.load();
			this.shortcutsHandler.reset();
			InvTweaks.logInGameStatic("Configuration loaded");
			this.showConfigErrors(this.config);
		} catch (FileNotFoundException fileNotFoundException3) {
			error = "Config file not found";
		} catch (Exception exception4) {
			error = "Error while loading config: " + exception4.getMessage();
		}

		if(error != null) {
			InvTweaks.logInGameStatic(error);
			log.error(error);
			this.config = null;
			return false;
		} else {
			return true;
		}
	}

	private void backupFile(File file, String baseName) {
		String newFileName;
		if((new File(baseName + ".bak")).exists()) {
			int i;
			for(i = 1; (new File(baseName + ".bak" + i)).exists(); ++i) {
			}

			newFileName = baseName + ".bak" + i;
		} else {
			newFileName = baseName + ".bak";
		}

		file.renameTo(new File(newFileName));
	}

	private boolean extractFile(String resource, String destination) {
		String resourceContents = "";
		URL resourceUrl = InvTweaks.class.getResource(resource);
		if(resourceUrl != null) {
			try {
				Object e = resourceUrl.getContent();
				byte[] arr$;
				if(e instanceof InputStream) {
					for(InputStream zips = (InputStream)e; zips.available() > 0; resourceContents = resourceContents + new String(arr$)) {
						arr$ = new byte[zips.available()];
						zips.read(arr$);
					}
				}
			} catch (IOException iOException17) {
				resourceUrl = null;
			}
		}

		if(resourceUrl == null) {
			File file18 = new File(Const.MINECRAFT_DIR + File.separatorChar + "mods");
			File[] file20 = file18.listFiles();
			if(file20 != null && file20.length > 0) {
				File[] file21 = file20;
				int len$ = file20.length;

				label49:
				for(int i$ = 0; i$ < len$; ++i$) {
					File zip = file21[i$];

					try {
						ZipFile e1 = new ZipFile(zip);
						ZipEntry zipResource = e1.getEntry(resource);
						if(zipResource != null) {
							InputStream content = e1.getInputStream(zipResource);

							while(true) {
								if(content.available() <= 0) {
									break label49;
								}

								byte[] bytes = new byte[content.available()];
								content.read(bytes);
								resourceContents = resourceContents + new String(bytes);
							}
						}
					} catch (Exception exception16) {
						log.warn("Failed to extract " + resource + " from mod: " + exception16.getMessage());
					}
				}
			}
		}

		if(!resourceContents.isEmpty()) {
			try {
				FileWriter fileWriter19 = new FileWriter(destination);
				fileWriter19.write(resourceContents);
				fileWriter19.close();
				return true;
			} catch (IOException iOException15) {
				InvTweaks.logInGameStatic("The mod won\'t work, because " + destination + " creation failed!");
				log.error("Cannot create " + destination + " file: " + iOException15.getMessage());
				return false;
			}
		} else {
			InvTweaks.logInGameStatic("The mod won\'t work, because " + resource + " could not be found!");
			log.error("Cannot create " + destination + " file: " + resource + " not found");
			return false;
		}
	}

	private void showConfigErrors(InvTweaksConfig config) {
		Vector invalid = config.getInvalidKeywords();
		if(invalid.size() > 0) {
			String error = "Invalid keywords found: ";

			String keyword;
			for(Iterator i$ = config.getInvalidKeywords().iterator(); i$.hasNext(); error = error + keyword + " ") {
				keyword = (String)i$.next();
			}

			InvTweaks.logInGameStatic(error);
		}

	}
}
