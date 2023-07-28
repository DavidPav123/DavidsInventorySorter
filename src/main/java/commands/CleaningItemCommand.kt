package commands

import commands.datastructures.CommandTree
import commands.datastructures.CommandTuple
import config.PluginConfigManager
import cooldown.CMRegistry
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import org.bukkit.inventory.meta.Damageable
import utils.PluginPermissions
import utils.StringUtils
import utils.messages.MessageSystem
import utils.messages.enums.MessageID
import utils.messages.enums.MessageType
import java.util.*

/**
 * A command class representing the CleaningItem command. CleaningItem Command
 *
 * @author Tom2208
 */
class CleaningItemCommand : CommandExecutor, TabCompleter {
    private val cmdTree: CommandTree = CommandTree(COMMAND_ALIAS)
    private val command = "cleaningitem"

    /* sub-commands */
    private val nameSubCommand = "name"
    private val loreSubCommand = "lore"
    private val activeSubCommand = "active"
    private val durabilityLossSubCommand = "durabilityLoss"
    private val openEventSubCommand = "openEvent"
    private val nameProperty = "$command $nameSubCommand"
    private val loreProperty = "$command $loreSubCommand"
    private val activeProperty = "$command $activeSubCommand"
    private val durabilityProperty = "$command $durabilityLossSubCommand"
    private val openEventProperty = openEventSubCommand

    init {
        cmdTree.addPath("/cleaningitem get", { tuple: CommandTuple -> getCleaningItem(tuple) }, null, false)
        cmdTree.addPath("/cleaningitem give @a", { tuple: CommandTuple -> giveCleaningItem(tuple) }, null, false)
        cmdTree.addPath(
            "/cleaningitem give player", { tuple: CommandTuple -> giveCleaningItem(tuple) }, Player::class.java, false
        )
        cmdTree.addPath("/cleaningitem set", { tuple: CommandTuple -> setCleaningItem(tuple) }, null, false)
        cmdTree.addPath("/cleaningitem name", { tuple: CommandTuple -> getConfig(tuple) }, null, false)
        cmdTree.addPath("/cleaningitem lore", { tuple: CommandTuple -> getConfig(tuple) }, null, false)
        cmdTree.addPath("/cleaningitem active", { tuple: CommandTuple -> getConfig(tuple) }, null, false)
        cmdTree.addPath("/cleaningitem durabilityLoss", { tuple: CommandTuple -> getConfig(tuple) }, null, false)
        cmdTree.addPath("/cleaningitem openEvent", { tuple: CommandTuple -> getConfig(tuple) }, null, false)
        cmdTree.addPath(
            "/cleaningitem name name", { tuple: CommandTuple -> setItemName(tuple) }, String::class.java, true
        )
        cmdTree.addPath(
            "/cleaningitem lore lore", { tuple: CommandTuple -> setItemLore(tuple) }, String::class.java, true
        )
        cmdTree.addPath(
            "/cleaningitem active true/false",
            { tuple: CommandTuple -> setCleaningItemActive(tuple) },
            Boolean::class.java,
            false
        )
        cmdTree.addPath(
            "/cleaningitem durabilityLoss true/false",
            { tuple: CommandTuple -> setDurabilityLoss(tuple) },
            Boolean::class.java,
            false
        )
        cmdTree.addPath(
            "/cleaningitem openEvent true/false",
            { tuple: CommandTuple -> setOpenEventMode(tuple) },
            Boolean::class.java,
            false
        )
    }

    override fun onCommand(sender: CommandSender, command: Command, alias: String, args: Array<String>): Boolean {
        cmdTree.execute(sender, command, alias, args)
        return true
    }

    override fun onTabComplete(
        sender: CommandSender, command: Command, alias: String, args: Array<String>
    ): List<String>? {
        return cmdTree.getListForTabCompletion(args)
    }

    /**
     * Sends a value change message of the state of the `command` form the
     * config to the `sender`.
     *
     * @param tuple the tuple the sub-command should run on.
     */
    private fun getConfig(tuple: CommandTuple) {
        val command = tuple.args[0]
        var key = ""
        var value = ""
        if (nameSubCommand.equals(command, ignoreCase = true)) {
            key = nameProperty
            value = if (Objects.requireNonNull(PluginConfigManager.getCleaningItem().itemMeta)
                    .hasDisplayName()
            ) PluginConfigManager.getCleaningItem().itemMeta.displayName else "<null>"
        } else if (loreSubCommand.equals(command, ignoreCase = true)) {
            key = loreProperty
            value = if (Objects.requireNonNull(PluginConfigManager.getCleaningItem().itemMeta)
                    .hasLore()
            ) Objects.requireNonNull(PluginConfigManager.getCleaningItem().itemMeta.lore).toString() else "<null>"
        } else if (activeSubCommand.equals(command, ignoreCase = true)) {
            key = activeProperty
            value = PluginConfigManager.isCleaningItemActive().toString()
        } else if (durabilityLossSubCommand.equals(command, ignoreCase = true)) {
            key = durabilityProperty
            value = PluginConfigManager.isDurabilityLossActive().toString()
        } else if (openEventSubCommand.equals(command, ignoreCase = true)) {
            key = openEventProperty
            value = PluginConfigManager.isOpenEvent().toString()
        }
        MessageSystem.sendMessageToCSWithReplacement(
            MessageType.SUCCESS, MessageID.INFO_CURRENT_VALUE, tuple.sender, key, value
        )
    }

    private fun checkPlayer(sender: CommandSender): Boolean {
        return if (sender is Player) {
            true
        } else {
            MessageSystem.sendMessageToCS(MessageType.ERROR, MessageID.ERROR_YOU_NOT_PLAYER, sender)
            false
        }
    }

    /**
     * Gives the `player` the cleaning item if he has the permission for that.
     *
     * @param tuple the tuple the sub-command should run on.
     */
    private fun getCleaningItem(tuple: CommandTuple) {
        if (checkPlayer(tuple.sender)) {
            val player = tuple.sender as Player
            if (!CMRegistry.isOnCooldown(CMRegistry.CMIdentifier.CLEANING_ITEM_GET, player)) {
                if (!player.hasPermission(PluginPermissions.CMD_CLEANING_ITEM_GET.string)) {
                    MessageSystem.sendPermissionError(player, PluginPermissions.CMD_CLEANING_ITEM_GET)
                } else {
                    player.inventory.addItem(PluginConfigManager.getCleaningItem())
                    MessageSystem.sendMessageToCS(MessageType.SUCCESS, MessageID.INFO_CLEANITEM_YOU_GET, player)
                }
            }
        }
    }

    /**
     * Sets the cleaning item to the item the player is holding if the
     * `player` has the correct permission.
     *
     * @param tuple the tuple the sub-command should run on.
     */
    private fun setCleaningItem(tuple: CommandTuple) {
        if (checkPlayer(tuple.sender)) {
            val player = tuple.sender as Player
            if (!player.hasPermission(PluginPermissions.CMD_ADMIN_ITEM_SET.string)) {
                MessageSystem.sendPermissionError(player, PluginPermissions.CMD_ADMIN_ITEM_SET)
            } else {
                val item = player.inventory.itemInMainHand.clone()
                if (item.type == Material.AIR) {
                    MessageSystem.sendMessageToCS(MessageType.ERROR, MessageID.ERROR_YOU_HOLD_ITEM, player)
                } else {
                    val itemMeta = item.itemMeta
                    val damageable = (itemMeta as Damageable)
                    damageable.damage = 0
                    item.setItemMeta(itemMeta)
                    item.amount = 1
                    PluginConfigManager.setCleaningItem(item)
                    MessageSystem.sendChangedValue(player, command, item.toString())
                }
            }
        }
    }

    /**
     * Gives the player with the name `playerName` a cleaning item.
     *
     * @param tuple the tuple the sub-command should run on.
     */
    private fun giveCleaningItem(tuple: CommandTuple) {
        val sender = tuple.sender
        val playerName = tuple.args[1]
        if (!sender.hasPermission(PluginPermissions.CMD_CLEANING_ITEM_GIVE.string)) {
            MessageSystem.sendPermissionError(sender, PluginPermissions.CMD_CLEANING_ITEM_GIVE)
        } else {
            val player2 = Bukkit.getPlayer(playerName)
            if (player2 != null) {
                player2.inventory.addItem(PluginConfigManager.getCleaningItem())
                MessageSystem.sendMessageToCSWithReplacement(
                    MessageType.SUCCESS, MessageID.INFO_CLEANITEM_PLAYER_GET, sender, player2.name
                )
            } else {
                if (playerName.equals("@a", ignoreCase = true)) {
                    val players: Array<Any> = Bukkit.getOnlinePlayers().toTypedArray()
                    for (p in players) {
                        val pl = p as Player
                        pl.inventory.addItem(PluginConfigManager.getCleaningItem())
                        MessageSystem.sendMessageToCSWithReplacement(
                            MessageType.SUCCESS, MessageID.INFO_CLEANITEM_PLAYER_GET, sender, pl.name
                        )
                    }
                } else {
                    MessageSystem.sendMessageToCSWithReplacement(
                        MessageType.ERROR, MessageID.ERROR_PLAYER_NOT_ONLINE, sender, playerName
                    )
                }
            }
        }
    }

    /**
     * Activates or deactivates the cleaning item depending on the String
     * `value` if the `sender` has the correct permission.
     *
     * @param tuple the tuple the sub-command should run on.
     */
    private fun setCleaningItemActive(tuple: CommandTuple) {
        val sender = tuple.sender
        val value = tuple.args[1]
        if (!sender.hasPermission(PluginPermissions.CMD_ADMIN_ITEM_SET_ACTIVE.string)) {
            MessageSystem.sendPermissionError(sender, PluginPermissions.CMD_ADMIN_ITEM_SET_ACTIVE)
        } else if (StringUtils.isStringNotTrueOrFalse(value)) {
            MessageSystem.sendMessageToCS(MessageType.ERROR, MessageID.ERROR_VALIDATION_BOOLEAN, sender)
        } else {
            val b = java.lang.Boolean.parseBoolean(value)
            PluginConfigManager.setCleaningItemActive(b)
            MessageSystem.sendChangedValue(sender, activeProperty, b.toString())
        }
    }

    /**
     * Activates or deactivates the durability loss for the cleaning item depending
     * on the String `value` if the `sender` has the correct permission.
     *
     * @param tuple the tuple the sub-command should run on.
     */
    private fun setDurabilityLoss(tuple: CommandTuple) {
        val sender = tuple.sender
        val value = tuple.args[1]
        if (!sender.hasPermission(PluginPermissions.CMD_ADMIN_ITEM_SET_DURABILITYLOSS.string)) {
            MessageSystem.sendPermissionError(sender, PluginPermissions.CMD_ADMIN_ITEM_SET_DURABILITYLOSS)
        } else if (StringUtils.isStringNotTrueOrFalse(value)) {
            MessageSystem.sendMessageToCS(MessageType.ERROR, MessageID.ERROR_VALIDATION_BOOLEAN, sender)
        } else {
            val b = java.lang.Boolean.parseBoolean(value)
            PluginConfigManager.setDurabilityLossActive(b)
            MessageSystem.sendChangedValue(sender, durabilityProperty, b.toString())
        }
    }

    /**
     * Activates or deactivates the open event mode for the cleaning item depending
     * on the String `value` if the `sender` has the correct permission.
     *
     * @param tuple the tuple the sub-command should run on.
     */
    private fun setOpenEventMode(tuple: CommandTuple) {
        val sender = tuple.sender
        val value = tuple.args[1]
        if (!sender.hasPermission(PluginPermissions.CMD_ADMIN_ITEM_SET_EVENT_MODE.string)) {
            MessageSystem.sendPermissionError(sender, PluginPermissions.CMD_ADMIN_ITEM_SET_EVENT_MODE)
        } else {
            val b = java.lang.Boolean.parseBoolean(value)
            PluginConfigManager.setOpenEvent(b)
            MessageSystem.sendChangedValue(sender, openEventProperty, b.toString())
        }
    }

    /**
     * Sets the lore for the cleaning item the `sender` has the correct
     * permission.
     *
     * @param tuple the tuple the sub-command should run on.
     */
    private fun setItemLore(tuple: CommandTuple) {
        val sender = tuple.sender
        val args = tuple.args
        if (!sender.hasPermission(PluginPermissions.CMD_ADMIN_ITEM_SET_LORE.string)) {
            MessageSystem.sendPermissionError(sender, PluginPermissions.CMD_ADMIN_ITEM_SET_LORE)
        } else {
            var lore = args[1]
            for (i in 2 until args.size) {
                lore = lore + " " + args[i]
            }
            val lorearray = lore.split("/n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val lorelist = ArrayList<String>()
            for (obj in lorearray) {
                lorelist.add(obj.replace("&", "§"))
            }
            val `is` = PluginConfigManager.getCleaningItem()
            val im = `is`.itemMeta!!
            im.lore = lorelist
            `is`.setItemMeta(im)
            PluginConfigManager.setCleaningItem(`is`)
            MessageSystem.sendChangedValue(sender, loreProperty, lorelist.toString())
        }
    }

    /**
     * Sets the name of the cleaning item if the `sender` has the correct
     * permission.
     *
     * @param tuple the tuple the sub-command should run on.
     */
    private fun setItemName(tuple: CommandTuple) {
        val sender = tuple.sender
        val args = tuple.args
        if (!sender.hasPermission(PluginPermissions.CMD_ADMIN_ITEM_RENAME.string)) {
            MessageSystem.sendPermissionError(sender, PluginPermissions.CMD_ADMIN_ITEM_RENAME)
        } else {
            var newname = args[1]
            for (i in 2 until args.size) {
                newname = newname + " " + args[i]
            }
            newname = newname.replace("&", "§")
            val `is` = PluginConfigManager.getCleaningItem()
            val im = `is`.itemMeta!!
            im.setDisplayName(newname)
            `is`.setItemMeta(im)
            PluginConfigManager.setCleaningItem(`is`)
            MessageSystem.sendChangedValue(sender, nameProperty, newname)
        }
    }

    companion object {
        const val COMMAND_ALIAS = "cleaningitem"
    }
}
