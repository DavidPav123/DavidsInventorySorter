package utils

import config.PluginConfigManager
import cooldown.CMRegistry
import cooldown.CMRegistry.Companion.isOnCooldown
import org.bukkit.block.Block
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import sorting.InventorySorter
import utils.messages.MessageSystem
import utils.messages.enums.MessageID
import utils.messages.enums.MessageType

object SortingUtils {
    fun sortPlayerInvWithEffects(player: Player?): Boolean {
        if (!isOnCooldown(CMRegistry.CMIdentifier.SORTING, player!!)) {
            if (InventorySorter.sortPlayerInventory(player)) {
                InventorySorter.playSortingSound(player)
                return true
            }
        }
        return false
    }

    fun isOnInventoryBlacklist(block: Block, sender: CommandSender?): Boolean {
        if (PluginConfigManager.getBlacklistInventory().contains(block.type)) {
            MessageSystem.sendMessageToCS(MessageType.ERROR, MessageID.ERROR_BLACKLIST_INVENTORY, sender)
            return true
        }
        return false
    }

    fun sortInventoryWithEffects(inv: Inventory?, player: Player?): Boolean {
        if (!isOnCooldown(CMRegistry.CMIdentifier.SORTING, player!!)) {
            if (InventorySorter.sortInventory(inv, player)) {
                InventorySorter.playSortingSound(player)
                return true
            }
        }
        return false
    }
}
