package config;

import com.github.davidpav123.inventorysorter.InventorySorter;
import config.serializable.ListCategory;
import config.serializable.MasterCategory;
import config.serializable.WordCategory;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * This is a singleton class to combine all configs and their utility methods
 * for this project.
 *
 * @author Tom2208
 */
public class PluginConfig {

    private static PluginConfig instance = null;

    private final File playerDataFile;
    private final FileConfiguration playerDataConfig;

    protected PluginConfig() {
        assert InventorySorter.main != null;
        InventorySorter.main.saveDefaultConfig();
        InventorySorter.main.getConfig().options().copyDefaults(true);
        playerDataFile = new File("plugins/" + InventorySorter.main.getName(), "playerdata.yml");
        playerDataConfig = YamlConfiguration.loadConfiguration(playerDataFile);
    }

    /**
     * Loads the Locale and Categories from the config.yml,
     * or uses the defaults from the jar/default config.yml if not set
     */
    public void loadConfig() {

        assert InventorySorter.main != null;
        FileConfiguration config = InventorySorter.main.getConfig();
        // Locale
        String language = config.getString(ConfigPath.LOCALE_LANGUAGE.getPath());
        String country = config.getString(ConfigPath.LOCALE_COUNTRY.getPath());
        String variant = InventorySorter.main.getDescription().getVersion().replace(".", "-");
        InventorySorter.main.setLocale(language, country, variant);
        // Categorizers

        CategoryLoader.loadCategorizers(
                (List<WordCategory>) config.getList(ConfigPath.CATEGORIES_WORDS.getPath()),
                (List<ListCategory>) config.getList(ConfigPath.CATEGORIES_LISTS.getPath()),
                (List<MasterCategory>) config.getList(ConfigPath.CATEGORIES_MASTER.getPath())
        );

        InventorySorter.main.saveConfig();
    }

    public static FileConfiguration getConfig() {
        assert InventorySorter.main != null;
        return InventorySorter.main.getConfig();
    }

    public static void setIntoConfig(ConfigPath path, Object obj) {
        InventorySorter.main.getConfig().set(path.getPath(), obj);
        InventorySorter.main.saveConfig();
    }

    public static void setIntoConfig(String path, Object obj) {
        InventorySorter.main.getConfig().set(path, obj);
        InventorySorter.main.saveConfig();
    }

    public static void removeFormConfig() {

    }

    public static FileConfiguration getPlayerData() {
        return getInstance().playerDataConfig;
    }

    public static void setIntoPlayerData(Player p, PlayerDataPath path, Object obj) {
        getPlayerData().set(path.getPath(p), obj);
        savePlayerData();
    }

    /**
     * Saves this {@code FileConfiguration} to the ChestCleaner folder. If the
     * file does not exist, it will be created. If it already exists, it will be
     * overwritten.
     * This method will save using the system default encoding, or possibly using
     * UTF8.
     *
     * @param configFile the file in which the FileConfiguration should be saved in.
     * @param config     the FileConfiguration that should get saved into the file.
     */
    private static void save(File configFile, FileConfiguration config) {

        try {
            config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void savePlayerData() {
        PluginConfig c = getInstance();
        save(c.playerDataFile, c.playerDataConfig);
    }


    /**
     * Returns the instance of this singleton if it's null it creates one.
     *
     * @return The Instance of the singleton.
     */
    public static PluginConfig getInstance() {
        if (instance == null) {
            instance = new PluginConfig();
        }
        return instance;
    }

    public enum ConfigPath {
        DEFAULT_AUTOSORT("default.autosort"),
        DEFAULT_PATTERN("default.pattern"),
        DEFAULT_CATEGORIES("default.categories"),
        DEFAULT_CLICKSORT("default.clicksort"),
        LOCALE_LANGUAGE("locale.lang"),
        LOCALE_COUNTRY("locale.country"),
        CLEANING_ITEM("cleaningItem.item"),
        CLEANING_ITEM_ACTIVE("cleaningItem.active"),
        CLEANING_ITEM_DURABILITY("cleaningItem.durability"),
        CLEANING_ITEM_OPEN_EVENT("cleaningItem.openEvent"),
        REFILL_CONSUMABLES("default.refill.consumables"),
        REFILL_BLOCKS("default.refill.blocks"),
        BLACKLIST_STACKING("blacklist.stacking"),
        BLACKLIST_INVENTORY("blacklist.inventory"),
        BLACKLIST_AUTOREFILL("blacklist.autorefill"),
        COOLDOWN_TIME("cooldown.time"),
        COOLDOWN_ACTIVE("cooldown.active"),
        CATEGORIES_WORDS("categories.words"),
        CATEGORIES_LISTS("categories.lists"),
        CATEGORIES_MASTER("categories.master"),
        CHAT_NOTIFICATION_BOOLEAN("default.sorting.notification"),
        DEFAULT_SORTING_SOUND_BOOLEAN("default.sorting.sound.active"),
        DEFAULT_SORTING_SOUND("default.sorting.sound.name"),
        DEFAULT_SORTING_SOUND_VOLUME("default.sorting.sound.volume"),
        DEFAULT_SORTING_SOUND_PITCH("default.sorting.sound.pitch"),
        DEFAULT_BREAKABLE_ITEMS_REFILL("default.refill.breakables");

        private final String path;

        ConfigPath(String path) {
            this.path = path;
        }

        public String getPath() {
            return path;
        }
    }

    public enum PlayerDataPath {

        AUTOSORT("autosort"),
        PATTERN("sortingpattern"),
        CATEGORIES_ORDER("categories.order"),
        NOTIFICATION("notification"),
        SOUND("sound"),
        REFILL_CONSUMABLES("refill.consumables"),
        REFILL_BLOCKS("refill.blocks"),
        REFILL_BREAKABLE_ITEMS("refill.breakableitems"),
        CLICK_SORT("clicksort");

        private final String path;

        PlayerDataPath(String path) {
            this.path = path;
        }

        public String getPath(Player p) {
            return p.getUniqueId() + "." + path;
        }
    }
}
