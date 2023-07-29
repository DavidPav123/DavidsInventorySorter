package config

import org.bukkit.entity.Player
import sorting.SortingPattern

/**
 * A singleton class to organize the player data.
 *
 * @author Tom2208
 */
object PlayerDataManager {
    fun setClickSort(player: Player?, b: Boolean) {
        PluginConfig.getPlayerData()[PluginConfig.PlayerDataPath.CLICK_SORT.getPath(player)] = b
    }

    @JvmStatic
    fun isClickSort(player: Player?): Boolean {
        return if (PluginConfig.getPlayerData().contains(PluginConfig.PlayerDataPath.CLICK_SORT.getPath(player))) {
            PluginConfig.getPlayerData().getBoolean(PluginConfig.PlayerDataPath.CLICK_SORT.getPath(player))
        } else {
            PluginConfigManager.isDefaultClickSort()
        }
    }

    fun resetCategories(player: Player?) {
        PluginConfig.getPlayerData()[PluginConfig.PlayerDataPath.CATEGORIES_ORDER.getPath(player)] = null
    }

    fun reset(player: Player) {
        PluginConfig.getPlayerData()[player.uniqueId.toString()] = null
        PluginConfig.savePlayerData()
    }

    fun setRefillConumables(p: Player?, b: Boolean) {
        PluginConfig.setIntoPlayerData(p, PluginConfig.PlayerDataPath.REFILL_CONSUMABLES, b)
    }

    @JvmStatic
    fun isRefillConumables(p: Player?): Boolean {
        return PluginConfig.getPlayerData().getBoolean(PluginConfig.PlayerDataPath.REFILL_CONSUMABLES.getPath(p))
    }

    @JvmStatic
    fun containsRefillConumables(p: Player?): Boolean {
        return PluginConfig.getPlayerData().contains(PluginConfig.PlayerDataPath.REFILL_CONSUMABLES.getPath(p))
    }

    fun setRefillBlocks(p: Player?, b: Boolean) {
        PluginConfig.setIntoPlayerData(p, PluginConfig.PlayerDataPath.REFILL_BLOCKS, b)
    }

    @JvmStatic
    fun isRefillBlocks(p: Player?): Boolean {
        return PluginConfig.getPlayerData().getBoolean(PluginConfig.PlayerDataPath.REFILL_BLOCKS.getPath(p))
    }

    @JvmStatic
    fun containsRefillBlocks(p: Player?): Boolean {
        return PluginConfig.getPlayerData().contains(PluginConfig.PlayerDataPath.REFILL_BLOCKS.getPath(p))
    }

    fun setRefillBreakables(p: Player?, b: Boolean) {
        PluginConfig.setIntoPlayerData(p, PluginConfig.PlayerDataPath.REFILL_BREAKABLE_ITEMS, b)
    }

    @JvmStatic
    fun isRefillBreakables(p: Player?): Boolean {
        return PluginConfig.getPlayerData().getBoolean(PluginConfig.PlayerDataPath.REFILL_BREAKABLE_ITEMS.getPath(p))
    }

    @JvmStatic
    fun containsRefillBreakables(p: Player?): Boolean {
        return PluginConfig.getPlayerData().contains(PluginConfig.PlayerDataPath.REFILL_BREAKABLE_ITEMS.getPath(p))
    }

    fun setSortingPattern(p: Player?, pattern: SortingPattern) {
        PluginConfig.setIntoPlayerData(p, PluginConfig.PlayerDataPath.PATTERN, pattern.name)
    }

    @JvmStatic
    fun getSortingPattern(p: Player?): SortingPattern {
        val pattern = SortingPattern.getSortingPatternByName(
            PluginConfig.getPlayerData().getString(PluginConfig.PlayerDataPath.PATTERN.getPath(p))
        )
        return pattern ?: PluginConfigManager.getDefaultPattern()
    }

    @JvmStatic
    fun containsNotification(p: Player?): Boolean {
        return PluginConfig.getPlayerData().contains(PluginConfig.PlayerDataPath.NOTIFICATION.getPath(p))
    }

    fun setNotification(p: Player?, b: Boolean) {
        PluginConfig.setIntoPlayerData(p, PluginConfig.PlayerDataPath.NOTIFICATION, b)
    }

    @JvmStatic
    fun isNotification(p: Player?): Boolean {
        return if (PluginConfig.getPlayerData().contains(PluginConfig.PlayerDataPath.NOTIFICATION.getPath(p))) {
            PluginConfig.getPlayerData().getBoolean(PluginConfig.PlayerDataPath.NOTIFICATION.getPath(p))
        } else {
            PluginConfigManager.getDefaultChatNotificationBoolean()
        }
    }

    @JvmStatic
    fun containsSortingSound(p: Player?): Boolean {
        return PluginConfig.getPlayerData().contains(PluginConfig.PlayerDataPath.SOUND.getPath(p))
    }

    fun setSortingSound(p: Player?, b: Boolean) {
        PluginConfig.setIntoPlayerData(p, PluginConfig.PlayerDataPath.SOUND, b)
    }

    @JvmStatic
    fun isSortingSound(p: Player?): Boolean {
        return if (PluginConfig.getPlayerData().contains(PluginConfig.PlayerDataPath.SOUND.getPath(p))) {
            PluginConfig.getPlayerData().getBoolean(PluginConfig.PlayerDataPath.SOUND.getPath(p))
        } else {
            PluginConfigManager.getDefaultSortingSoundBoolean()
        }
    }

    fun setAutoSort(p: Player?, b: Boolean) {
        PluginConfig.setIntoPlayerData(p, PluginConfig.PlayerDataPath.AUTOSORT, b)
    }

    @JvmStatic
    fun isAutoSort(p: Player?): Boolean {
        return if (PluginConfig.getPlayerData().contains(PluginConfig.PlayerDataPath.AUTOSORT.getPath(p))) {
            PluginConfig.getPlayerData().getBoolean(PluginConfig.PlayerDataPath.AUTOSORT.getPath(p))
        } else {
            PluginConfigManager.getDefaultAutoSortBoolean()
        }
    }

    @JvmStatic
    fun getCategoryOrder(p: Player?): List<String> {
        val list = PluginConfig.getPlayerData().getStringList(PluginConfig.PlayerDataPath.CATEGORIES_ORDER.getPath(p))
        return list.ifEmpty { PluginConfigManager.getCategoryOrder() }
    }

    fun setCategoryOrder(p: Player?, categorizationOrder: List<String?>?) {
        PluginConfig.setIntoPlayerData(p, PluginConfig.PlayerDataPath.CATEGORIES_ORDER, categorizationOrder)
    }
}