package commands

import commands.datastructures.CommandTree
import commands.datastructures.CommandTuple
import config.PluginConfigManager
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import utils.messages.MessageSystem
import utils.messages.enums.MessageID
import utils.messages.enums.MessageType
import java.util.*
import java.util.stream.Collectors

/**
 * A command class representing the blacklist command. Blacklist Command
 *
 * @author Tom2208
 */
class BlacklistCommand : CommandExecutor, TabCompleter {
    private val cmdTree: CommandTree = CommandTree(COMMAND_ALIAS)

    // The lineNumbers of a page when the list gets displayed in the in game chat.
    private val LIST_PAGE_LENGTH = 8
    private val stackingSubCommand = "stacking"
    private val inventorySubCommand = "inventory"
    private val autoRefillSubCommand = "autoRefill"

    init {/* subcommands */cmdTree.addPath("/blacklist blacklist", null, BlacklistType::class.java, false)
        // ADD
        cmdTree.addPath("/blacklist blacklist add", { tuple: CommandTuple -> addSubCommand(tuple) }, null, false)
        cmdTree.addPath(
            "/blacklist blacklist add materialId",
            { tuple: CommandTuple -> addSubCommand(tuple) },
            Material::class.java,
            false
        )
        //REMOVE
        cmdTree.addPath("/blacklist blacklist remove", { tuple: CommandTuple -> removeSubCommand(tuple) }, null, false)
        cmdTree.addPath(
            "/blacklist blacklist remove materialId",
            { tuple: CommandTuple -> removeSubCommand(tuple) },
            Material::class.java,
            false
        )
        //LIST
        cmdTree.addPath("/blacklist blacklist list", { tuple: CommandTuple -> listSubCommand(tuple) }, null, false)
        //CLEAR
        cmdTree.addPath("/blacklist blacklist clear", { tuple: CommandTuple -> clearSubCommand(tuple) }, null, false)
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

    private fun getListType(args: Array<String>): BlacklistType? {
        if (args.isNotEmpty()) {
            if (args[0].equals(stackingSubCommand, ignoreCase = true)) {
                return BlacklistType.STACKING
            } else if (args[0].equals(inventorySubCommand, ignoreCase = true)) {
                return BlacklistType.INVENTORY
            } else if (args[0].equals(autoRefillSubCommand, ignoreCase = true)) {
                return BlacklistType.AUTOREFILL
            }
        }
        return null
    }

    private fun getList(args: Array<String>): MutableList<Material?>? {
        if (args.isNotEmpty()) {
            if (args[0].equals(stackingSubCommand, ignoreCase = true)) {
                return PluginConfigManager.getBlacklistStacking()
            } else if (args[0].equals(inventorySubCommand, ignoreCase = true)) {
                return PluginConfigManager.getBlacklistInventory()
            } else if (args[0].equals(autoRefillSubCommand, ignoreCase = true)) {
                return PluginConfigManager.getBlacklistAutoRefill()
            }
        }
        return null
    }

    private fun checkForPlayer(cs: CommandSender): Player? {
        return if (cs is Player) {
            cs
        } else null
    }

    /**
     * A method representing the sub-command **add**
     * that runs all actions with the data of the `tuple` when called.
     *
     * @param tuple the tuple the sub-command should run on.
     */
    private fun addSubCommand(tuple: CommandTuple) {
        val player = checkForPlayer(tuple.sender)
        if (player != null && tuple.args.size == 2) {
            addMaterial(tuple.sender, getListType(tuple.args), getList(tuple.args), getMaterialFromPlayerHand(player))
        } else if (tuple.args.size == 3) {
            addMaterialName(tuple.sender, getListType(tuple.args), getList(tuple.args), tuple.args[2])
        }
    }

    /**
     * A method representing the sub-command **remove**
     * that runs all actions with the data of the `tuple` when called.
     *
     * @param tuple the tuple the sub-command should run on.
     */
    private fun removeSubCommand(tuple: CommandTuple) {
        val player = checkForPlayer(tuple.sender)
        if (player != null && tuple.args.size == 2) {
            removeMaterial(
                tuple.sender,
                getListType(tuple.args),
                Objects.requireNonNull(getList(tuple.args)),
                getMaterialFromPlayerHand(player)
            )
        } else if (tuple.args.size == 3) {
            removeMaterialName(tuple.sender, getListType(tuple.args), getList(tuple.args), tuple.args[2])
        }
    }

    /**
     * A method representing the sub-command **list**
     * that runs all actions with the data of the `tuple` when called.
     *
     * @param tuple the tuple the sub-command should run on.
     */
    private fun listSubCommand(tuple: CommandTuple) {
        if (tuple.args.size == 2) {
            printBlacklist(tuple.sender, "1", Objects.requireNonNull<List<Material?>?>(getList(tuple.args)))
        } else if (tuple.args.size == 3) {
            printBlacklist(tuple.sender, tuple.args[2], Objects.requireNonNull<List<Material?>?>(getList(tuple.args)))
        }
    }

    /**
     * A method representing the sub-command **clear**
     * that runs all actions with the data of the `tuple` when called.
     *
     * @param tuple the tuple the sub-command should run on.
     */
    private fun clearSubCommand(tuple: CommandTuple) {
        clearBlacklist(tuple.sender, getListType(tuple.args))
    }

    private fun addMaterialName(
        sender: CommandSender, type: BlacklistType?, list: MutableList<Material?>?, name: String
    ) {
        val material = Material.getMaterial(name.uppercase(Locale.getDefault()))
        if (material == null) {
            MessageSystem.sendMessageToCSWithReplacement(
                MessageType.ERROR, MessageID.ERROR_MATERIAL_NAME, sender, name
            )
        } else {
            addMaterial(sender, type, list, material)
        }
    }

    /**
     * Adds `material` to the blacklist.
     *
     * @param sender   the sender which executed the command.
     * @param type     the list on which the material gets added.
     * @param list     a list which gets modified and set to the config.
     * @param material the material that gets added to the list.
     */
    private fun addMaterial(
        sender: CommandSender, type: BlacklistType?, list: MutableList<Material?>?, material: Material
    ) {
        if (list == null) {
            MessageSystem.sendMessageToCS(MessageType.ERROR, MessageID.ERROR_BLACKLIST_LIST_NOT_EXIST, sender)
            return
        }
        if (list.contains(material)) {
            MessageSystem.sendMessageToCSWithReplacement(
                MessageType.ERROR,
                MessageID.ERROR_BLACKLIST_EXISTS,
                sender,
                material.name.lowercase(Locale.getDefault())
            )
        } else {
            list.add(material)
            saveList(type, list)
            MessageSystem.sendMessageToCSWithReplacement(
                MessageType.SUCCESS, MessageID.INFO_BLACKLIST_ADD, sender, material.name.lowercase(Locale.getDefault())
            )
        }
    }

    /**
     * Removes a material with the name `name` form a blacklist.
     *
     * @param sender the sender which executed the command.
     * @param type   the type of blacklist form which you want to remove the
     * material.
     * @param list   the list which gets modified and then set to the config.
     * @param name   the name of the material you want to remove.
     */
    private fun removeMaterialName(
        sender: CommandSender, type: BlacklistType?, list: MutableList<Material?>?, name: String
    ) {
        var material = Material.getMaterial(name.uppercase(Locale.getDefault()))
        if (material == null) {
            material = try {
                val index = name.toInt()
                // expect 1 based index input, bc of list and not all players are programmers
                if (index > 0 && index <= list!!.size) {
                    list[index - 1]
                } else {
                    MessageSystem.sendMessageToCSWithReplacement(
                        MessageType.ERROR, MessageID.ERROR_VALIDATION_INDEX_BOUNDS, sender, index.toString()
                    )
                    return
                }
            } catch (ex: NumberFormatException) {
                MessageSystem.sendMessageToCSWithReplacement(
                    MessageType.ERROR, MessageID.ERROR_MATERIAL_NAME, sender, name
                )
                return
            }
        }
        removeMaterial(sender, type, list, material)
    }

    /**
     * Removes a `material` form a blacklist.
     *
     * @param sender   the sender which executed the command.
     * @param type     the type of blacklist form which you want to remove the
     * material.
     * @param list     the list which gets modified and then set to the config.
     * @param material the material you want to remove.
     */
    private fun removeMaterial(
        sender: CommandSender, type: BlacklistType?, list: MutableList<Material?>?, material: Material?
    ) {
        if (!list!!.contains(material)) {
            MessageSystem.sendMessageToCSWithReplacement(
                MessageType.ERROR,
                MessageID.ERROR_BLACKLIST_NOT_EXISTS,
                sender,
                material!!.name.lowercase(Locale.getDefault())
            )
        } else {
            list.remove(material)
            saveList(type, list)
            MessageSystem.sendMessageToCSWithReplacement(
                MessageType.SUCCESS,
                MessageID.INFO_BLACKLIST_DEL,
                sender,
                material!!.name.lowercase(Locale.getDefault())
            )
        }
    }

    /**
     * Sends a page with the page number `pageString` of the list to the
     * `sender`.
     *
     * @param sender     the sender which will receive the the list.
     * @param pageString the page of the list which gets sent.
     * @param list       The list which contains the page/part of the list you want
     * to send.
     */
    private fun printBlacklist(sender: CommandSender, pageString: String, list: List<Material?>?) {
        if (list!!.isEmpty()) {
            MessageSystem.sendMessageToCS(MessageType.ERROR, MessageID.ERROR_BLACKLIST_EMPTY, sender)
        } else {
            val names = list.stream().map { item: Material? -> item!!.name.lowercase(Locale.getDefault()) }
                .collect(Collectors.toList())
            MessageSystem.sendListPageToCS(names, sender, pageString, LIST_PAGE_LENGTH)
        }
    }

    /**
     * Clears the specific blacklist in the config.
     *
     * @param sender the sender which executed the command. It becomes a success
     * message.
     * @param type   the type of the list. It determines the blacklist which gets
     * cleared.
     */
    private fun clearBlacklist(sender: CommandSender, type: BlacklistType?) {
        val list: List<Material?> = ArrayList()
        saveList(type, list)
        MessageSystem.sendMessageToCS(MessageType.SUCCESS, MessageID.INFO_BLACKLIST_CLEARED, sender)
    }

    /**
     * Returns the Material of the item in a hand (prefers the main hand, if it's
     * empty it take the off handF).
     *
     * @param p the player of the hand.
     * @return it returns the material of you main hand if it is not AIR otherwise
     * the Material of your off hand.
     */
    private fun getMaterialFromPlayerHand(p: Player): Material {
        if (p.inventory.itemInMainHand.type == Material.AIR) {
            if (p.inventory.itemInOffHand.type != Material.AIR) {
                return p.inventory.itemInOffHand.type
            }
        }
        return p.inventory.itemInMainHand.type
    }

    /**
     * Saves the list in to the config.yml.
     *
     * @param type  the type of blacklist you want to save.
     * @param items the list which gets set to the config.
     */
    private fun saveList(type: BlacklistType?, items: List<Material?>?) {
        if (type == BlacklistType.STACKING) {
            PluginConfigManager.setBlacklistStacking(items)
        } else if (type == BlacklistType.INVENTORY) {
            PluginConfigManager.setBlacklistInventory(items)
        } else if (type == BlacklistType.AUTOREFILL) {
            PluginConfigManager.setBlacklistAutoRefill(items)
        }
    }

    enum class BlacklistType {
        STACKING, INVENTORY, AUTOREFILL;

        companion object {
            @JvmStatic
            fun getBlackListTypeByString(str: String): BlacklistType? {
                for (type in entries) {
                    if (str.equals(type.toString(), ignoreCase = true)) return type
                }
                return null
            }
        }
    }

    companion object {
        const val COMMAND_ALIAS = "blacklist"
    }
}
