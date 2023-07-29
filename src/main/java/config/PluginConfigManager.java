package config;

import config.serializable.Category;
import config.serializable.ListCategory;
import config.serializable.MasterCategory;
import config.serializable.WordCategory;
import cooldown.CMRegistry;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import sorting.SortingPattern;
import sorting.categorizer.Categorizer;
import sorting.categorizer.ListCategoryCategorizer;
import sorting.categorizer.MasterCategorizer;
import sorting.categorizer.PredicateCategorizer;
import utils.SortingAdminUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class PluginConfigManager {

    private static List<Material> blacklistStacking = null;
    private static List<Material> blacklistInventory = null;
    private static List<Material> blacklistAutorefill = null;

    private PluginConfigManager() {
    }

    public static boolean isDefaultClickSort() {
        return PluginConfig.getConfig().getBoolean(PluginConfig.ConfigPath.DEFAULT_CLICKSORT.getPath());
    }

    public static boolean isDefaultBreakableRefill() {
        return PluginConfig.getConfig().getBoolean(PluginConfig.ConfigPath.DEFAULT_BREAKABLE_ITEMS_REFILL.getPath());
    }

    public static Sound getDefaultSortingSound() {
        String soundName = PluginConfig.getConfig().getString(PluginConfig.ConfigPath.DEFAULT_SORTING_SOUND.getPath());
        return SortingAdminUtils.getSoundByName(soundName);
    }

    public static float getDefaultVolume() {
        return Float.parseFloat(Objects.requireNonNull(PluginConfig.getConfig().getString(PluginConfig.ConfigPath.DEFAULT_SORTING_SOUND_VOLUME.getPath())));
    }

    public static float getDefaultPitch() {
        return Float.parseFloat(Objects.requireNonNull(PluginConfig.getConfig().getString(PluginConfig.ConfigPath.DEFAULT_SORTING_SOUND_PITCH.getPath())));
    }

    public static boolean getDefaultSortingSoundBoolean() {
        return PluginConfig.getConfig().getBoolean(PluginConfig.ConfigPath.DEFAULT_SORTING_SOUND_BOOLEAN.getPath());
    }

    public static boolean getDefaultChatNotificationBoolean() {
        return PluginConfig.getConfig().getBoolean(PluginConfig.ConfigPath.CHAT_NOTIFICATION_BOOLEAN.getPath());
    }

    public static boolean isDurabilityLossActive() {
        return PluginConfig.getConfig().getBoolean(PluginConfig.ConfigPath.CLEANING_ITEM_DURABILITY.getPath());
    }

    public static boolean isCleaningItemActive() {
        return PluginConfig.getConfig().getBoolean(PluginConfig.ConfigPath.CLEANING_ITEM_ACTIVE.getPath());
    }

    public static ItemStack getCleaningItem() {
        return PluginConfig.getConfig().getItemStack(PluginConfig.ConfigPath.CLEANING_ITEM.getPath());
    }

    public static boolean isOpenEvent() {
        return PluginConfig.getConfig().getBoolean(PluginConfig.ConfigPath.CLEANING_ITEM_OPEN_EVENT.getPath());
    }

    public static boolean isDefaultBlockRefill() {
        return PluginConfig.getConfig().getBoolean(PluginConfig.ConfigPath.REFILL_BLOCKS.getPath());
    }

    public static boolean isDefaultConsumablesRefill() {
        return PluginConfig.getConfig().getBoolean(PluginConfig.ConfigPath.REFILL_CONSUMABLES.getPath());
    }

    public static boolean isCooldownActive(CMRegistry.CMIdentifier id) {
        return PluginConfig.getConfig().getBoolean(PluginConfig.ConfigPath.COOLDOWN_ACTIVE.getPath().concat(".").concat(id.toString()));
    }

    public static int getCooldown(CMRegistry.CMIdentifier id) {
        return PluginConfig.getConfig().getInt(PluginConfig.ConfigPath.COOLDOWN_TIME.getPath().concat(".").concat(id.toString()));
    }

    public static List<String> getCategoryOrder() {
        return PluginConfig.getConfig().getStringList(PluginConfig.ConfigPath.DEFAULT_CATEGORIES.getPath());
    }

    public static List<WordCategory> getWordCategories() {
        return getCastList(PluginConfig.getConfig().getList(PluginConfig.ConfigPath.CATEGORIES_WORDS.getPath(), new ArrayList<WordCategory>()));
    }

    public static List<ListCategory> getListCategories() {
        return getCastList(PluginConfig.getConfig().getList(PluginConfig.ConfigPath.CATEGORIES_LISTS.getPath(), new ArrayList<ListCategory>()));
    }

    public static List<MasterCategory> getMasterCategories() {
        return getCastList(PluginConfig.getConfig().getList(PluginConfig.ConfigPath.CATEGORIES_MASTER.getPath(), new ArrayList<MasterCategory>()));
    }

    public static void addWordCategory(WordCategory category) {
        List<WordCategory> categories = addOrUpdateCategory(category, getWordCategories());
        PluginConfig.setIntoConfig(PluginConfig.ConfigPath.CATEGORIES_WORDS, categories);
    }

    public static void addListCategory(ListCategory category) {
        List<ListCategory> categories = addOrUpdateCategory(category, getListCategories());
        PluginConfig.setIntoConfig(PluginConfig.ConfigPath.CATEGORIES_LISTS, categories);
    }


    public static void addMasterCategory(MasterCategory category) {
        List<MasterCategory> categories = addOrUpdateCategory(category, getMasterCategories());
        PluginConfig.setIntoConfig(PluginConfig.ConfigPath.CATEGORIES_MASTER, categories);
    }

    public static boolean removeCategory(Categorizer categorizer) {

        String path = PluginConfig.ConfigPath.CATEGORIES_WORDS.getPath();
        String categoryName = categorizer.getName();
        if (categorizer instanceof PredicateCategorizer) {
            path = PluginConfig.ConfigPath.CATEGORIES_WORDS.getPath();
        } else if (categorizer instanceof ListCategoryCategorizer) {
            path = PluginConfig.ConfigPath.CATEGORIES_LISTS.getPath();
        } else if (categorizer instanceof MasterCategorizer) {
            path = PluginConfig.ConfigPath.CATEGORIES_MASTER.getPath();
        }

        boolean removed = false;
        List<Category<?>> list = (List<Category<?>>) PluginConfig.getConfig().getList(path);

        assert list != null;

        for (Category<?> cat : list) {
            if (cat.getName().equalsIgnoreCase(categoryName)) {
                list.remove(cat);
                removed = true;
                break;
            }
        }

        PluginConfig.setIntoConfig(path, list);
        return removed;
    }

    private static <T extends Category> List<T> addOrUpdateCategory(T category, List<T> categories) {
        T existingCategory = categories.stream().filter(cat -> cat.getName().equalsIgnoreCase(category.getName())).findFirst().orElse(null);
        if (existingCategory != null) {
            existingCategory.setValue(category.getValue());
        } else {
            categories.add(category);
        }
        return categories;
    }

    public static SortingPattern getDefaultPattern() {
        return SortingPattern.getSortingPatternByName(PluginConfig.getConfig().getString(PluginConfig.ConfigPath.DEFAULT_PATTERN.getPath()));
    }

    public static boolean getDefaultAutoSortBoolean() {
        return PluginConfig.getConfig().getBoolean(PluginConfig.ConfigPath.DEFAULT_AUTOSORT.getPath());
    }

    public static List<Material> getBlacklistInventory() {
        if (blacklistInventory == null) {
            blacklistInventory = getMaterialList(PluginConfig.getConfig(), PluginConfig.ConfigPath.BLACKLIST_INVENTORY);
        }
        return blacklistInventory;
    }

    public static List<Material> getBlacklistStacking() {
        if (blacklistStacking == null) {
            blacklistStacking = getMaterialList(PluginConfig.getConfig(), PluginConfig.ConfigPath.BLACKLIST_STACKING);
        }
        return blacklistStacking;
    }

    public static List<Material> getBlacklistAutoRefill() {
        if (blacklistAutorefill == null) {
            blacklistAutorefill = getMaterialList(PluginConfig.getConfig(), PluginConfig.ConfigPath.BLACKLIST_AUTOREFILL);
        }
        return blacklistAutorefill;
    }

    private static ArrayList<Material> getMaterialList(FileConfiguration config, PluginConfig.ConfigPath path) {
        List<String> list = config.getStringList(path.getPath());
        ArrayList<Material> materials = new ArrayList<>();

        for (String name : list) {
            materials.add(Material.getMaterial(name.toUpperCase()));
        }
        return materials;
    }

    private static <T> List<T> getCastList(List<?> input) {
        if (input == null) {
            return new ArrayList<>();
        }
        return input.stream().map(o -> (T) o).collect(Collectors.toList());
    }
}
