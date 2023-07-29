package sorting

import config.PlayerDataManager.containsSortingSound
import config.PlayerDataManager.getCategoryOrder
import config.PlayerDataManager.getSortingPattern
import config.PlayerDataManager.isSortingSound
import config.PluginConfigManager
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import utils.InventoryConverter
import utils.InventoryDetector.getInventoryFormBlock
import utils.InventoryDetector.getPlayerInventoryList
import utils.PluginPermissions
import utils.messages.MessageSystem
import utils.messages.enums.MessageID
import utils.messages.enums.MessageType

object InventorySorter {
    /**
     * Returns `list` sorted in full stacked items.
     * The amount of the ItemStack is increased beyond MaxStackSize, but only one ItemStack exists for each Material.
     * Items on the stacking blacklist don't get their amount increased. They are simply added to the list.
     * So there may be multiple ItemStacks for materials on the stacking blacklist.
     *
     * @param items an ArrayList of ItemStacks you want to sort
     * @return full stacked `list`;
     */
    private fun reduceStacks(items: List<ItemStack?>): List<ItemStack?> {
        // temp list, every item once, amounts get added
        val newList = ArrayList<ItemStack?>()
        for (item in items) {
            if (PluginConfigManager.getBlacklistStacking().contains(item!!.type)) {
                newList.add(item)
            } else {
                val existingItem =
                    newList.stream().filter { tempItem: ItemStack? -> tempItem!!.isSimilar(item) }.findFirst()
                        .orElse(null)
                if (existingItem == null) {
                    newList.add(item)
                } else {
                    existingItem.amount = existingItem.amount + item.amount
                }
            }
        }
        return newList
    }

    /**
     * Returns `list` sorted in maxStackSize ItemStacks.
     * If the amount is larger than maxStackSize, it will create a new ItemStack for that material.
     * Items on the stacking blacklist are simply added to the list.
     */
    private fun expandStacks(items: List<ItemStack?>): List<ItemStack?> {
        val newList = ArrayList<ItemStack?>()
        for (item in items) {
            if (PluginConfigManager.getBlacklistStacking().contains(item!!.type)) {
                newList.add(item)
            } else if (item.type != Material.AIR) {
                while (item.amount > 0) {
                    val amount = item.amount.coerceAtMost(item.maxStackSize)
                    val clone = item.clone()
                    clone.amount = amount
                    newList.add(clone)
                    item.amount = item.amount - amount
                }
            }
        }
        return newList
    }

    fun sortPlayerInventory(p: Player): Boolean {
        return sortInventory(p.inventory, p, getPlayerInventoryList(p))
    }

    /**
     * Sorts any kind of inventory.
     *
     * @param inv the inventory you want to sort.
     */
    @JvmOverloads
    fun sortInventory(
        inv: Inventory?, p: Player?, items: ArrayList<ItemStack>? = InventoryConverter.getArrayListFromInventory(inv)
    ): Boolean {
        var items = items
        val event = items?.let { SortingEvent(p!!, inv!!, it) }
        if (event != null) {
            Bukkit.getPluginManager().callEvent(event)
        }
        if (event != null) {
            if (event.isCancelled) {
                return false
            }
        }
        var categoryNames = PluginConfigManager.getCategoryOrder()
        var pattern = PluginConfigManager.getDefaultPattern()
        if (items != null) {
            if (items.isEmpty()) {
                return false
            }
        }
        if (p != null) {
            if (p.hasPermission(PluginPermissions.CMD_SORTING_CONFIG_CATEGORIES.string)) {
                categoryNames = getCategoryOrder(p)
            }
        }
        if (p != null) {
            if (p.hasPermission(PluginPermissions.CMD_SORTING_CONFIG_PATTERN.string)) {
                pattern = getSortingPattern(p)
            }
        }
        if (!CategorizerManager.validateExists(categoryNames)) {
            MessageSystem.sendMessageToCS(MessageType.ERROR, MessageID.ERROR_CATEGORY_INVALID, p)
            return false
        }
        if (items != null) {
            if (items.size <= 1) {
                InventoryConverter.setItemsOfInventory(inv, items, pattern)
                return true
            }
        }
        items = items?.let { reduceStacks(it) } as ArrayList<ItemStack>?
        items = CategorizerManager.sort(items, categoryNames) as ArrayList<ItemStack>?
        items = items?.let { expandStacks(it) } as ArrayList<ItemStack>?
        InventoryConverter.setItemsOfInventory(inv, items, pattern)
        return true
    }

    /**
     * Checks if the block has an inventory or if it is an enderchest and sorts it.
     *
     * @param b Block you want to get sorted.
     * @param p the player or owner of an enderchest inventory.
     * @return returns true if an inventory got sorted, otherwise false.
     */
    fun sortPlayerBlock(b: Block, p: Player?): Boolean {
        val inv = getInventoryFormBlock(b)
        if (inv != null) {
            return sortInventory(inv, p)
        }
        if (p != null) {
            if (b.blockData.material == Material.ENDER_CHEST) {
                return sortInventory(p.enderChest, p)
            }
        }
        return false
    }

    fun playSortingSound(p: Player) {
        val flag: Boolean = if (containsSortingSound(p)) {
            isSortingSound(p)
        } else {
            PluginConfigManager.getDefaultSortingSoundBoolean()
        }
        if (flag) {
            p.world.playSound(
                p.location,
                PluginConfigManager.getDefaultSortingSound(),
                PluginConfigManager.getDefaultVolume(),
                PluginConfigManager.getDefaultPitch()
            )
        }
    }
}