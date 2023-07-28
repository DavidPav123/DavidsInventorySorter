package commands

import commands.datastructures.CommandTree
import commands.datastructures.CommandTuple
import cooldown.CMRegistry
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import sorting.InventorySorter
import utils.BlockDetector
import utils.InventoryDetector
import utils.PluginPermissions
import utils.SortingUtils
import utils.messages.MessageSystem
import utils.messages.enums.MessageID
import utils.messages.enums.MessageType

/**
 * A command class representing the CleanInventory command. CleanInventory
 * Command explained:
 * https://github.com/tom2208/ChestCleaner/wiki/Command--cleaninventory
 *
 * @author Tom2208
 */
class CleanInventoryCommand : CommandExecutor, TabCompleter {
    private val ownSubCommand = "own"
    private val cmdTree: CommandTree = CommandTree(COMMAND_ALIAS)

    init {
        cmdTree.addPath("/cleaninventory x", null, Int::class.java)
        cmdTree.addPath("/cleaninventory x y", null, Int::class.java)
        cmdTree.addPath("/cleaninventory x y z", { tuple: CommandTuple -> sortInvAtLocation(tuple) }, Int::class.java)
        cmdTree.addPath(
            "/cleaninventory x y z world",
            { tuple: CommandTuple -> sortInvInWorld(tuple) },
            String::class.java
        )
        cmdTree.addPath("/cleaninventory") { tuple: CommandTuple -> sortInvForPlayer(tuple) }
        cmdTree.addPath("/cleaninventory $ownSubCommand") { tuple: CommandTuple -> sortPlayerInventory(tuple) }
        cmdTree.addPath(
            "/cleaninventory player",
            { tuple: CommandTuple -> sortPlayerInventory(tuple) },
            Player::class.java
        )
    }

    override fun onCommand(sender: CommandSender, cmd: Command, label: String, args: Array<String>): Boolean {
        if (!sender.hasPermission(PluginPermissions.CMD_INV_CLEAN.string)) {
            MessageSystem.sendPermissionError(sender, PluginPermissions.CMD_INV_CLEAN)
            return true
        }
        cmdTree.execute(sender, cmd, label, args)
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<String>
    ): List<String>? {
        return cmdTree.getListForTabCompletion(args)
    }

    /**
     * The player `player` sorts the inventory of the player with the name
     * `playerName`. He needs the correct permissions.
     *
     * @param tuple the tuple the sub-command should run on.
     */
    private fun sortPlayerInventory(tuple: CommandTuple) {
        val player = getPlayer(tuple.sender) ?: return
        val playerName = tuple.args[0]
        if (playerName.equals(ownSubCommand, ignoreCase = true) || playerName.equals(player.name, ignoreCase = true)) {
            if (!player.hasPermission(PluginPermissions.CMD_INV_CLEAN_OWN.string)) {
                MessageSystem.sendPermissionError(player, PluginPermissions.CMD_INV_CLEAN_OWN)
            }
            if (InventorySorter.sortPlayerInventory(player)) {
                InventorySorter.playSortingSound(player)
            }
        } else {
            if (!player.hasPermission(PluginPermissions.CMD_INV_CLEAN_OTHERS.string)) {
                MessageSystem.sendPermissionError(player, PluginPermissions.CMD_INV_CLEAN_OTHERS)
            }
            val player2 = Bukkit.getPlayer(playerName)
            if (player2 == null) {
                MessageSystem.sendMessageToCS(MessageType.ERROR, MessageID.ERROR_PLAYER_NOT_ONLINE, player)
            } else {
                if (InventorySorter.sortInventory(
                        player2.inventory, player,
                        InventoryDetector.getPlayerInventoryList(player2)
                    )
                ) {
                    InventorySorter.playSortingSound(player)
                    InventorySorter.playSortingSound(player2)
                }
            }
        }
    }

    private fun getPlayer(sender: CommandSender): Player? {
        return if (sender is Player) {
            sender
        } else null
    }

    /**
     * The player `p` sorts a blocks inventory.
     *
     * @param tuple the tuple the sub-command should run on.
     */
    private fun sortInvForPlayer(tuple: CommandTuple) {
        val p = getPlayer(tuple.sender)
        val block = BlockDetector.getTargetBlock(p)
        sortBlock(block, p, p)
    }

    /**
     * Sorts an inventory of a block if it has one.
     *
     * @param tuple the tuple the sub-command should run on.
     */
    private fun sortInvInWorld(tuple: CommandTuple) {
        val worldStr = tuple.args[3]
        val cs = tuple.sender
        val world = Bukkit.getWorld(worldStr)
        if (world == null) {
            MessageSystem.sendMessageToCSWithReplacement(MessageType.ERROR, MessageID.ERROR_WORLD_NAME, cs, worldStr)
        } else {
            sortInvAtLocation(tuple)
        }
    }

    /**
     * Sorts an inventory of a block if it has one.
     *
     * @param tuple the tuple the sub-command should run on.
     */
    private fun sortInvAtLocation(tuple: CommandTuple) {
        val xStr = tuple.args[0]
        val yStr = tuple.args[1]
        val zStr = tuple.args[2]
        val sender = tuple.sender
        val player = getPlayer(sender)
        val world: World? = if (tuple.args.size >= 4) {
            Bukkit.getWorld(tuple.args[3])
        } else {
            player!!.world
        }
        val x = xStr.toDouble().toInt()
        val y = yStr.toDouble().toInt()
        val z = zStr.toDouble().toInt()
        val block = BlockDetector.getBlockByLocation(Location(world, x.toDouble(), y.toDouble(), z.toDouble()))
        sortBlock(block, player, sender)
    }

    /**
     * Sorts the inventory of a block if it has one.
     *
     * @param block  the block which may have an inventory.
     * @param p      the player who is sorting.
     * @param sender the sender which executed the command.
     */
    private fun sortBlock(block: Block, p: Player?, sender: CommandSender?) {
        if (!SortingUtils.isOnInventoryBlacklist(block, sender)) {
            if (!p?.let { CMRegistry.isOnCooldown(CMRegistry.CMIdentifier.SORTING, it) }!!) {
                if (InventorySorter.sortPlayerBlock(block, p)) {
                } else {
                    MessageSystem.sendMessageToCSWithReplacement(
                        MessageType.ERROR, MessageID.ERROR_BLOCK_NO_INVENTORY, sender,
                        "(" + block.x + " / " + block.y + " / " + block.z + ", " + block.type.name
                                + ")"
                    )
                }
            }
        }
    }

    companion object {
        const val COMMAND_ALIAS = "cleaninventory"
    }
}
