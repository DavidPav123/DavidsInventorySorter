package commands.datastructures

import org.bukkit.command.Command
import org.bukkit.command.CommandSender

class CommandTuple(
    @JvmField var sender: CommandSender,
    var cmd: Command,
    var label: String,
    @JvmField var args: Array<String>
)
