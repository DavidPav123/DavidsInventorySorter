package com.github.davidpav123.inventorysorter;

import commands.SortingAdminCommand;
import commands.BlacklistCommand;
import commands.CleanInventoryCommand;
import commands.CleaningItemCommand;
import commands.SortingConfigCommand;
import config.PluginConfig;
import config.serializable.ListCategory;
import config.serializable.MasterCategory;
import config.serializable.WordCategory;
import listeners.RefillListener;
import listeners.SortingListener;
import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;

public class InventorySorter extends JavaPlugin {

	public static InventorySorter main;
	private ResourceBundle rb;
	private Locale locale;
	
	@Override
	public void onEnable() {
		main = this;		
		ConfigurationSerialization.registerClass(WordCategory.class);
		ConfigurationSerialization.registerClass(ListCategory.class);
		ConfigurationSerialization.registerClass(MasterCategory.class);
		PluginConfig.getInstance().loadConfig();

		String version = getDescription().getVersion().replace(".", "-");
		String bundleName = getName() + "_" + version;
		getPlugin(this.getClass()).saveResource(bundleName + "_en_GB.properties", false);

		try {
			URL fileUrl = new File(this.getDataFolder().toString()).toURI().toURL();
			ClassLoader loader = new URLClassLoader(new URL[] { fileUrl });
			rb = ResourceBundle.getBundle(bundleName, locale, loader);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		Objects.requireNonNull(getCommand(CleanInventoryCommand.COMMAND_ALIAS)).setExecutor(new CleanInventoryCommand());
		Objects.requireNonNull(getCommand(CleaningItemCommand.COMMAND_ALIAS)).setExecutor(new CleaningItemCommand());
		Objects.requireNonNull(getCommand(BlacklistCommand.COMMAND_ALIAS)).setExecutor(new BlacklistCommand());
		Objects.requireNonNull(getCommand(SortingConfigCommand.COMMAND_ALIAS)).setExecutor(new SortingConfigCommand());
		Objects.requireNonNull(getCommand(SortingAdminCommand.COMMAND_ALIAS)).setExecutor(new SortingAdminCommand());

		Bukkit.getPluginManager().registerEvents(new SortingListener(), this);
		Bukkit.getPluginManager().registerEvents(new RefillListener(), this);

	}

	public ResourceBundle getRB() {
		return rb;
	}

	public void setLocale(String language, String country, String variant) {
		locale = new Locale(language, country, variant);
	}

}
