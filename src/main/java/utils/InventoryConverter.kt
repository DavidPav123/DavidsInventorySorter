package utils

import config.PluginConfigManager
import org.bukkit.Material
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import sorting.SortingPattern

object InventoryConverter {
    /**
     * **Converts an inventory into a ArrayList of ItemStacks and returns it.**
     * Air will get removed.
     *
     * @param inv The inventory you want to convert into an ArrayList.
     * @return Returns an ArrayList of ItemStacks you got from the inventory. If the
     * Inventory is null it returns null.
     */
    fun getArrayListFromInventory(inv: Inventory?): ArrayList<ItemStack>? {
        if (inv == null) {
            return null
        }
        val list = ArrayList<ItemStack>()
        for (item in inv) {
            if (item != null) {
                if (item.type != Material.AIR) {
                    list.add(item)
                }
            }
        }
        return list
    }

    /**
     * **Sets the items of the `inventory` to the ItemStacks of the ArrayList
     * `items`.** The method clears the inventory before putting the items
     * into the inventory.
     *
     * @param inv   The inventory you want to put the items in.
     * @param items The list of items you want to put into the cleared inventory.
     * @throws IllegalArgumentException throws if the argument ItemStack
     * `items` or the Inventory `inv`
     * is equal to null.
     */
    fun setItemsOfInventory(inv: Inventory?, items: List<ItemStack?>?, pattern: SortingPattern?) {
        var pattern = pattern
        require(!(items == null || inv == null))
        val isPlayer = inv.type == InventoryType.PLAYER
        if (!isPlayer) inv.clear() else {
            for (i in 9..35) {
                inv.clear(i)
            }
        }
        var shift = 0
        var height = inv.size / 9
        var done = false
        if (isPlayer) {
            shift = 9
            height--
        }
        if (pattern == null) pattern = PluginConfigManager.getDefaultPattern()
        when (pattern) {
            SortingPattern.LEFT_TO_RIGHT_TOP_TO_BOTTOM -> {
                for (i in items.indices) {
                    inv.setItem(i + shift, items[i])
                }
            }

            SortingPattern.TOP_TO_BOTTOM_LEFT_TO_RIGHT -> {
                for (x in 0..8) {
                    for (y in 0 until height) {
                        if (x * height + y >= items.size) {
                            done = true
                            break
                        }
                        inv.setItem(y * 9 + x + shift, items[x * height + y])
                    }
                    if (done) break
                }
            }

            SortingPattern.RIGHT_TO_LEFT_BOTTOM_TO_TOP -> {
                val begin = if (isPlayer) 35 else inv.size - 1
                var i = 0
                while (i < items.size) {
                    inv.setItem(begin - i, items[i])
                    i++
                }
            }

            SortingPattern.BOTTOM_TO_TOP_LEFT_TO_RIGHT -> {
                var itemCounter = 0
                for (x in 0..8) {
                    for (y in height - 1 downTo 0) {
                        if (itemCounter >= items.size) {
                            done = true
                            break
                        }
                        inv.setItem(y * 9 + x + shift, items[itemCounter])
                        itemCounter++
                    }
                    if (done) break
                }
            }

            null -> TODO()
        }
    }
}