package commands

import commands.SortingAdminCommand.RefillType
import commands.datastructures.CommandTree
import commands.datastructures.CommandTuple
import config.PlayerDataManager
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import sorting.CategorizerManager
import sorting.SortingPattern
import sorting.categorizer.Categorizer
import utils.PluginPermissions
import utils.StringUtils
import utils.messages.MessageSystem
import utils.messages.enums.MessageID
import utils.messages.enums.MessageType

/**
 * A command class representing the SortingConfig command. SortingConfig Command
 * explained: https://github.com/tom2208/ChestCleaner/wiki/Command-sortingconfig
 */
class SortingConfigCommand : CommandExecutor, TabCompleter {
    private val MAX_LINES_PER_PAGE = 8

    /* sub-commands */
    private val autosortSubCommand = "autosort"
    private val categoriesSubCommand = "categories"
    private val patternSubCommand = "pattern"
    private val chatNotificationSubCommand = "chatNotification"
    private val sortingSoundSubCommand = "sortingSound"
    private val clickSortSubCommand = "clickSort"
    private val blocksSubCommand = "blocks"
    private val consumablesSubCommand = "consumables"
    private val breakablesSubCommand = "breakables"
    private val autosortProperty = "autosort"
    private val categoriesProperty = "categoryOrder"
    private val patternProperty = "sortingpattern"
    private val chatNotificationProperty = "chat sorting notification"
    private val sortingSoundProperty = "sorting sound"
    private val cmdTree: CommandTree = CommandTree(COMMAND_ALIAS)

    init {

        // autoSort
        cmdTree.addPath("/sortingconfig autosort") { tuple: CommandTuple -> getConfig(tuple) }
        cmdTree.addPath(
            "/sortingconfig autosort true/false",
            { tuple: CommandTuple -> setAutoSort(tuple) },
            Boolean::class.java
        )
        // categories
        cmdTree.addPath("/sortingconfig categories") { tuple: CommandTuple -> getConfig(tuple) }
        cmdTree.addPath("/sortingconfig categories list") { tuple: CommandTuple -> getCategoryList(tuple) }
        cmdTree.addPath(
            "/sortingconfig categories list page",
            { tuple: CommandTuple -> getCategoryList(tuple) },
            Int::class.java
        )
        cmdTree.addPath("/sortingconfig categories reset") { tuple: CommandTuple -> resetCategories(tuple) }
        cmdTree.addPath(
            "/sortingconfig categories set names",
            { tuple: CommandTuple -> setCategories(tuple) },
            Categorizer::class.java,
            true
        )
        // pattern
        cmdTree.addPath("/sortingconfig pattern") { tuple: CommandTuple -> getConfig(tuple) }
        cmdTree.addPath(
            "/sortingconfig pattern pattern",
            { tuple: CommandTuple -> setPattern(tuple) },
            SortingPattern::class.java
        )
        // chatNotification
        cmdTree.addPath("/sortingconfig chatNotification") { tuple: CommandTuple -> getConfig(tuple) }
        cmdTree.addPath(
            "/sortingconfig chatNotification true/false",
            { tuple: CommandTuple -> setChatNotificationBool(tuple) },
            Boolean::class.java
        )
        // refill
        cmdTree.addPath("/sortingconfig refill type", null, RefillType::class.java)
        cmdTree.addPath(
            "/sortingconfig refill type true/false",
            { tuple: CommandTuple -> setRefill(tuple) },
            Boolean::class.java
        )
        cmdTree.addPath(
            "/sortingconfig refill true/false",
            { tuple: CommandTuple -> setAllRefills(tuple) },
            Boolean::class.java
        )
        // sortingSound
        cmdTree.addPath("/sortingconfig sortingSound") { tuple: CommandTuple -> getConfig(tuple) }
        cmdTree.addPath(
            "/sortingconfig sortingSound true/false",
            { tuple: CommandTuple -> setSortingSoundBool(tuple) },
            Boolean::class.java
        )
        // clickSort
        cmdTree.addPath("/sortingconfig clickSort") { tuple: CommandTuple -> getConfig(tuple) }
        cmdTree.addPath(
            "/sortingconfig clickSort true/false",
            { tuple: CommandTuple -> setClickSort(tuple) },
            Boolean::class.java
        )
        // reset
        cmdTree.addPath("/sortingconfig reset") { tuple: CommandTuple -> resetConfiguration(tuple) }
    }

    override fun onCommand(sender: CommandSender, cmd: Command, label: String, args: Array<String>): Boolean {
        var player: Player? = null
        if (sender is Player) {
            player = sender
        }
        if (player == null) {
            MessageSystem.sendMessageToCS(MessageType.ERROR, MessageID.ERROR_YOU_NOT_PLAYER, sender)
            return true
        }
        cmdTree.execute(sender, cmd, label, args)
        return true
    }

    override fun onTabComplete(cs: CommandSender, cmd: Command, label: String, args: Array<String>): List<String>? {
        return cmdTree.getListForTabCompletion(args)
    }

    private fun getConfig(tuple: CommandTuple) {
        val p = tuple.sender as Player
        val command = tuple.args[0]
        var key = ""
        var value = ""
        if (command.equals(autosortSubCommand, ignoreCase = true)) {
            key = autosortProperty
            value = PlayerDataManager.isAutoSort(p).toString()
        } else if (command.equals(categoriesSubCommand, ignoreCase = true)) {
            key = categoriesProperty
            value = PlayerDataManager.getCategoryOrder(p).toString()
        } else if (command.equals(patternSubCommand, ignoreCase = true)) {
            key = patternProperty
            value = PlayerDataManager.getSortingPattern(p).name
        } else if (command.equals(chatNotificationSubCommand, ignoreCase = true)) {
            key = chatNotificationProperty
            value = PlayerDataManager.isNotification(p).toString()
        } else if (command.equals(sortingSoundSubCommand, ignoreCase = true)) {
            key = sortingSoundProperty
            value = PlayerDataManager.isSortingSound(p).toString()
        } else if (command.equals(clickSortSubCommand, ignoreCase = true)) {
            key = clickSortSubCommand
            value = PlayerDataManager.isClickSort(p).toString()
        }
        if (key != "" && value != "") {
            MessageSystem.sendMessageToCSWithReplacement(
                MessageType.SUCCESS, MessageID.INFO_CURRENT_VALUE, p, key,
                value
            )
        }
    }

    private fun resetCategories(tuple: CommandTuple) {
        val player = tuple.sender as Player
        if (checkPermission(player, PluginPermissions.CMD_SORTING_CONFIG_CATEGORIES_RESET)) {
            PlayerDataManager.resetCategories(player)
            MessageSystem.sendMessageToCS(MessageType.SUCCESS, MessageID.INFO_CATEGORY_RESETED, player)
        }
    }

    private fun setClickSort(tuple: CommandTuple) {
        val player = tuple.sender as Player
        val bool = tuple.args[1]
        if (checkPermission(player, PluginPermissions.CMD_SORTING_CONFIG_CLICKSORT)) {
            if (StringUtils.isStringNotTrueOrFalse(bool)) {
                MessageSystem.sendMessageToCS(MessageType.ERROR, MessageID.ERROR_VALIDATION_BOOLEAN, player)
            } else {
                val b = java.lang.Boolean.parseBoolean(bool)
                PlayerDataManager.setClickSort(player, b)
                MessageSystem.sendChangedValue(player, clickSortSubCommand, b.toString())
            }
        }
    }

    /**
     * Sets the configuration for a refill option.
     *
     * @param tuple the tuple the sub-command should run on.
     * @return True if the command can get parsed, otherwise false.
     */
    private fun setRefill(tuple: CommandTuple): Boolean {
        val player = tuple.sender as Player
        val arg = tuple.args[1]
        val bool = tuple.args[2]
        if (StringUtils.isStringBoolean(player, bool)) {
            val b = java.lang.Boolean.parseBoolean(bool)
            val property: String = if (arg.equals(blocksSubCommand, ignoreCase = true) && checkPermission(
                    player,
                    PluginPermissions.CMD_SORTING_CONFIG_REFILL_BLOCKS
                )
            ) {
                PlayerDataManager.setRefillBlocks(player, b)
                blocksSubCommand
            } else if (arg.equals(consumablesSubCommand, ignoreCase = true) && checkPermission(
                    player,
                    PluginPermissions.CMD_SORTING_CONFIG_REFILL_CONSUMABLES
                )
            ) {
                PlayerDataManager.setRefillConumables(player, b)
                consumablesSubCommand
            } else if (arg.equals(breakablesSubCommand, ignoreCase = true) && checkPermission(
                    player,
                    PluginPermissions.CMD_SORTING_CONFIG_REFILL_BREAKABLES
                )
            ) {
                PlayerDataManager.setRefillBreakables(player, b)
                breakablesSubCommand
            } else {
                return false
            }
            MessageSystem.sendMessageToCSWithReplacement(
                MessageType.SUCCESS, MessageID.INFO_CURRENT_VALUE, player,
                property, b
            )
        }
        return true
    }

    private fun setAllRefills(tuple: CommandTuple) {
        val player = tuple.sender as Player
        val bool = tuple.args[1]
        if (StringUtils.isStringNotTrueOrFalse(bool)) {
            MessageSystem.sendMessageToCS(MessageType.ERROR, MessageID.ERROR_VALIDATION_BOOLEAN, player)
        } else {
            var change = false
            val b = java.lang.Boolean.parseBoolean(bool)
            if (player.hasPermission(PluginPermissions.CMD_SORTING_CONFIG_REFILL_BLOCKS.string)) {
                PlayerDataManager.setRefillBlocks(player, b)
                MessageSystem.sendChangedValue(player, blocksSubCommand, b.toString())
                change = true
            }
            if (player.hasPermission(PluginPermissions.CMD_SORTING_CONFIG_REFILL_CONSUMABLES.string)) {
                PlayerDataManager.setRefillConumables(player, b)
                MessageSystem.sendChangedValue(player, consumablesSubCommand, b.toString())
                change = true
            }
            if (player.hasPermission(PluginPermissions.CMD_SORTING_CONFIG_REFILL_BREAKABLES.string)) {
                PlayerDataManager.setRefillBreakables(player, b)
                MessageSystem.sendChangedValue(player, breakablesSubCommand, b.toString())
                change = true
            }
            if (!change) {
                MessageSystem.sendPermissionError(player, PluginPermissions.CMD_SORTING_CONFIG_REFILL_GENERIC)
            }
        }
    }

    private fun resetConfiguration(tuple: CommandTuple) {
        val player = tuple.sender as Player
        if (checkPermission(player, PluginPermissions.CMD_SORTING_CONFIG_RESET)) {
            PlayerDataManager.reset(player)
            MessageSystem.sendMessageToCS(MessageType.SUCCESS, MessageID.INFO_RESET_CONFIG, player)
        }
    }

    private fun setChatNotificationBool(tuple: CommandTuple) {
        val player = tuple.sender as Player
        val bool = tuple.args[1]
        if (checkPermission(player, PluginPermissions.CMD_SORTING_CONFIG_SET_NOTIFICATION_BOOL)) {
            if (StringUtils.isStringNotTrueOrFalse(bool)) {
                MessageSystem.sendMessageToCS(MessageType.ERROR, MessageID.ERROR_VALIDATION_BOOLEAN, player)
            } else {
                val b = java.lang.Boolean.parseBoolean(bool)
                PlayerDataManager.setNotification(player, b)
                MessageSystem.sendChangedValue(player, chatNotificationProperty, b.toString())
            }
        }
    }

    private fun setSortingSoundBool(tuple: CommandTuple) {
        val player = tuple.sender as Player
        val bool = tuple.args[1]
        if (checkPermission(player, PluginPermissions.CMD_SORTING_CONFIG_SET_SOUND_BOOL)) {
            if (StringUtils.isStringNotTrueOrFalse(bool)) {
                MessageSystem.sendMessageToCS(MessageType.ERROR, MessageID.ERROR_VALIDATION_BOOLEAN, player)
            } else {
                val b = java.lang.Boolean.parseBoolean(bool)
                PlayerDataManager.setSortingSound(player, b)
                MessageSystem.sendChangedValue(player, sortingSoundProperty, b.toString())
            }
        }
    }

    private fun getCategoryList(tuple: CommandTuple) {
        val sender = tuple.sender
        var pageString: String? = "1"
        if (tuple.args.size == 3) pageString = tuple.args[2]
        val names = CategorizerManager.getAllNames()
        MessageSystem.sendListPageToCS(names, sender, pageString, MAX_LINES_PER_PAGE)
    }

    private fun setPattern(tuple: CommandTuple) {
        val player = tuple.sender as Player
        val patternName = tuple.args[1]
        if (checkPermission(player, PluginPermissions.CMD_SORTING_CONFIG_PATTERN)) {
            val pattern = SortingPattern.getSortingPatternByName(patternName)
            if (pattern != null) {
                PlayerDataManager.setSortingPattern(player, pattern)
                MessageSystem.sendChangedValue(player, patternProperty, pattern.name)
            } else {
                MessageSystem.sendMessageToCS(MessageType.ERROR, MessageID.ERROR_PATTERN_ID, player)
            }
        }
    }

    private fun setAutoSort(tuple: CommandTuple) {
        val player = tuple.sender as Player
        val bool = tuple.args[1]
        if (checkPermission(player, PluginPermissions.CMD_SORTING_CONFIG_SET_AUTOSORT)) {
            if (StringUtils.isStringNotTrueOrFalse(bool)) {
                MessageSystem.sendMessageToCS(MessageType.ERROR, MessageID.ERROR_VALIDATION_BOOLEAN, player)
            } else {
                val b = java.lang.Boolean.parseBoolean(bool)
                PlayerDataManager.setAutoSort(player, b)
                MessageSystem.sendChangedValue(player, autosortProperty, b.toString())
            }
        }
    }

    private fun checkPermission(player: Player, permission: PluginPermissions): Boolean {
        return if (!player.hasPermission(permission.string)) {
            MessageSystem.sendPermissionError(player, permission)
            false
        } else {
            true
        }
    }

    private fun setCategories(tuple: CommandTuple) {
        val player = tuple.sender as Player
        tuple.args[2]
        val categories = SortingAdminCommand.getCategoriesFromArguments(tuple.args)
        if (!player.hasPermission(PluginPermissions.CMD_SORTING_CONFIG_CATEGORIES.string)) {
            MessageSystem.sendPermissionError(player, PluginPermissions.CMD_SORTING_CONFIG_CATEGORIES)
        } else if (!CategorizerManager.validateExists(categories)) {
            MessageSystem.sendMessageToCS(MessageType.ERROR, MessageID.ERROR_CATEGORY_NAME, player)
        } else {
            PlayerDataManager.setCategoryOrder(player, categories)
            MessageSystem.sendChangedValue(player, categoriesProperty, categories.toString())
        }
    }

    companion object {
        const val COMMAND_ALIAS = "sortingconfig"
    }
}
