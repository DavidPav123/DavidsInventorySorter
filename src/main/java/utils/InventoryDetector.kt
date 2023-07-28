package utils

import org.bukkit.Location
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack

object InventoryDetector {
    /**
     * **Returns the inventory of the block `b`. If there is no inventory it
     * will return `null`.** This method checks if the object of the block
     * is an instance of the class org.bukkit.block.Container and returns its
     * inventory.
     *
     * @param b The Block you want to get the inventory form.
     * @return Returns the inventory of the container of the block, if its has no
     * container it returns `null`.
     */
    @JvmStatic
    fun getInventoryFormBlock(b: Block): Inventory? {
        if (b.state is InventoryHolder) {
            val h = b.state as InventoryHolder
            return h.inventory
        }
        return null
    }

    fun hasInventoryHolder(b: Block): Boolean {
        return b.state is InventoryHolder
    }

    /**
     * **Returns the inventory of the block on the location `location` in the
     * world `world`. If the block has no inventory it will return
     * `null`.** This method checks if the object of the block is an
     * instance of the class org.bukkit.block.Container and returns its inventory.
     *
     * @param location The location of the block you want to get the inventory from.
     * @param world    The world of the block you want to get the inventory form.
     * @return Returns the inventory of the container of the block, if its has no
     * container it returns `null`.
     */
    fun getInventoryFormLocation(location: Location?, world: World): Inventory? {
        return getInventoryFormBlock(world.getBlockAt(location!!))
    }

    /**
     * **Return the main part of the player inventory, that means a list of all
     * ItemStacks form the slots with the index 9 (including) to index 35
     * (including).**That means the hotbar, armor slots or second hand slot are
     * getting avoided.
     *
     *
     * @param p The owner of the inventory.
     * @return A list of all items form the inventory of `p` (form index 9
     * (including) to index 35 (including))
     * @throws IllegalArgumentException if `player` is null.
     */
    @JvmStatic
    fun getPlayerInventoryList(p: Player?): ArrayList<ItemStack> {
        requireNotNull(p)
        val items = ArrayList<ItemStack>()
        for (i in 9..35) {
            if (p.inventory.getItem(i) != null) items.add(p.inventory.getItem(i)!!.clone())
        }
        return items
    }

    fun getFullInventory(inv: Inventory?): Array<ItemStack?> {
        requireNotNull(inv)
        val items = arrayOfNulls<ItemStack>(36)
        for (i in items.indices) {
            items[i] = inv.getItem(i)
        }
        return items
    }
}