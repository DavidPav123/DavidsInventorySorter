package sorting

import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

/**
 * This event gets triggered if an inventory is going to be sorted. It can be canceled.
 */
class SortingEvent
/**
 * Constructor for the SortingEvent.
 * @param player the player who sorts. Can be null.
 * @param inventory the inventory that gets sorted.
 * @param list the list of Items that gets sorted. This list will be sorted and inserted into inventory.
 */(
    /**
     * Returns the player who is going to sort.
     * @return the player
     */
    val player: Player,
    /**
     * Returns the inventory that should get sorted.
     * @return the inventory
     */
    val inventory: Inventory,
    /**
     * Returns the list of items that is taken form the inventory which should be sorted. In the sorting process only
     * this list gets modified and at the end it will replace the stacks in the real inventory.
     * @return the list
     */
    val list: List<ItemStack?>
) : Event(), Cancellable {
    private var isCanceled = false

    override fun getHandlers(): HandlerList {
        return HANDLERS
    }

    override fun isCancelled(): Boolean {
        return isCanceled
    }

    override fun setCancelled(b: Boolean) {
        isCanceled = b
    }

    companion object {
        private val HANDLERS = HandlerList()
    }
}