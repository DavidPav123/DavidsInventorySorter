package listeners

import config.PlayerDataManager.isAutoSort
import config.PlayerDataManager.isClickSort
import config.PluginConfigManager
import cooldown.CMRegistry
import cooldown.CMRegistry.Companion.isOnCooldown
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.inventory.*
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import sorting.InventorySorter
import utils.BlockDetector
import utils.InventoryDetector
import utils.PluginPermissions
import utils.SortingUtils

/**
 * @author Tom2208
 */
class SortingListener : Listener {
    private val usableAnimalInventories = arrayOf("CraftMule", "CraftLlama", "CraftHorse")
    private val invTypeWhiteList = arrayOf(
        InventoryType.CHEST,
        InventoryType.ENDER_CHEST,
        InventoryType.BARREL,
        InventoryType.SHULKER_BOX,
        InventoryType.PLAYER,
        InventoryType.CREATIVE,
        InventoryType.DISPENSER,
        InventoryType.DROPPER,
        InventoryType.HOPPER
    )

    @EventHandler
    private fun onRightClick(e: PlayerInteractEvent) {
        val player = e.player
        if (e.action == Action.RIGHT_CLICK_AIR || e.action == Action.RIGHT_CLICK_BLOCK) {
            if (PluginConfigManager.isCleaningItemActive() && isPlayerHoldingACleaningItem(player)) {
                if (isPlayerAllowedToCleanOwnInv(player) && SortingUtils.sortPlayerInvWithEffects(player)) {
                    damageItem(player)
                    e.isCancelled = true
                } else if (!PluginConfigManager.isOpenEvent()
                    && !isOnCooldown(CMRegistry.CMIdentifier.SORTING, player)
                    && player.hasPermission(PluginPermissions.CLEANING_ITEM_USE.string)
                ) {
                    val b = BlockDetector.getTargetBlock(player)
                    if (!InventoryDetector.hasInventoryHolder(b)
                        || PluginConfigManager.getBlacklistInventory().contains(b.type)
                    ) return
                    if (InventorySorter.sortPlayerBlock(b, player)) {
                        damageItem(player)
                        InventorySorter.playSortingSound(player)
                        e.isCancelled = true
                    }
                }
            }
        }
    }

    @EventHandler
    private fun onOpenInventory(e: InventoryOpenEvent) {
        if (PluginConfigManager.isCleaningItemActive() && PluginConfigManager.isOpenEvent()) {
            val player = e.player as Player
            if (player.hasPermission(PluginPermissions.CLEANING_ITEM_USE.string)
                && isPlayerHoldingACleaningItem(player) && SortingUtils.sortInventoryWithEffects(e.inventory, player)
            ) {
                damageItem(player)
                e.isCancelled = true
            }
        }
    }

    @EventHandler
    private fun onCloseInventory(e: InventoryCloseEvent) {
        // Doing the auto sorting here
        if (e.inventory.type == InventoryType.ENDER_CHEST || e.inventory.type == InventoryType.CHEST) {
            val player = e.player as Player
            if (isAutoSort(player)) {
                SortingUtils.sortInventoryWithEffects(e.inventory, player)
            }
        }
    }

    @EventHandler
    private fun onClick(e: InventoryClickEvent) {
        if (e.click == ClickType.MIDDLE || isNewClick(e)) {
            if (e.whoClicked.hasPermission(PluginPermissions.CLICK_SORT.string)) {
                if (e.slot == -999) {
                    if (e.currentItem == null) {
                        val player = Bukkit.getServer().getPlayer(e.whoClicked.name)
                        if (isClickSort(player)) {
                            sortInventoryOnClick(player, e)
                        }
                        e.isCancelled = true
                    }
                }
            }
        }
    }

    private fun isNewClick(e: InventoryClickEvent): Boolean {
        return e.click == ClickType.RIGHT && e.currentItem == null
    }

    private fun sortInventoryOnClick(player: Player?, e: InventoryClickEvent) {
        var flag = false
        for (type in invTypeWhiteList) {
            if (e.inventory.type == type) {
                flag = true
                break
            }
        }
        var animalInv = false
        if (e.inventory.type == InventoryType.CHEST) {
            for (holder in usableAnimalInventories) {
                if (e.inventory.holder != null && e.inventory.holder.toString().contains(holder)) {
                    animalInv = true
                    break
                }
            }
        }
        if (!flag || animalInv) {
            SortingUtils.sortPlayerInvWithEffects(player)
        } else {
            SortingUtils.sortInventoryWithEffects(e.inventory, player)
        }
    }

    private fun isPlayerHoldingACleaningItem(player: Player): Boolean {
        return isPlayerHoldingCleaningItemInMainHand(player) || isPlayerHoldingCleaningItemInOffHand(player)
    }

    private fun getComparableItem(item: ItemStack): ItemStack {
        val compItem = item.clone()
        val itemMeta = compItem.itemMeta
        val damageable = itemMeta as Damageable
        damageable.damage = 0
        compItem.setItemMeta(itemMeta)
        return compItem
    }

    private fun isPlayerHoldingCleaningItemInMainHand(player: Player): Boolean {
        val item = player.inventory.itemInMainHand
        return if (item.type == Material.AIR) {
            false
        } else getComparableItem(item).isSimilar(PluginConfigManager.getCleaningItem())
    }

    private fun isPlayerHoldingCleaningItemInOffHand(player: Player): Boolean {
        val item = player.inventory.itemInOffHand
        return if (item.type == Material.AIR) {
            false
        } else getComparableItem(item).isSimilar(PluginConfigManager.getCleaningItem())
    }

    private fun isPlayerAllowedToCleanOwnInv(player: Player): Boolean {
        return player.hasPermission(PluginPermissions.CLEANING_ITEM_USE_OWN_INV.string) && player.isSneaking
    }

    /**
     * Damages the item in the Hand of the `player` (using
     * player.getItemInHand()), if the `durability` (in class Main) is true.
     * Damaging means, that stackable items (maxStackSize > 1) get reduced in amount
     * by one, not stackable items get damaged and removed, if they reach the
     * highest durability .
     *
     * @param player the player who is holding the item, that you want to get
     * damaged, in hand.
     */
    private fun damageItem(player: Player) {
        if (PluginConfigManager.isDurabilityLossActive()) {
            val item: ItemStack = if (isPlayerHoldingCleaningItemInMainHand(player)) {
                player.inventory.itemInMainHand
            } else {
                player.inventory.itemInOffHand
            }
            val itemMeta = item.itemMeta
            val damageable = itemMeta as Damageable
            if (damageable.damage + 1 < item.type.maxDurability) {
                damageable.damage = damageable.damage + 1
            } else {
                item.amount = item.amount - 1
            }
            item.setItemMeta(itemMeta)
        }
    }
}
