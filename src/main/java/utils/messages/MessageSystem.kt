package utils.messages

import com.github.davidpav123.inventorysorter.InventorySorter
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.command.CommandSender
import utils.PluginPermissions
import utils.messages.enums.MessageID
import utils.messages.enums.MessageType
import java.util.*

object MessageSystem {
    @JvmStatic
    fun sendMessageToCS(type: MessageType, arg: String, cs: CommandSender?) {
        if (cs != null) {
            cs.sendMessage(getMessageString(type, arg))
        } else {
            assert(InventorySorter.main != null)
            InventorySorter.main!!.server.consoleSender.sendMessage(getMessageString(type, arg))
        }
    }

    @JvmStatic
    fun sendMessageToCS(type: MessageType, messageID: MessageID, cs: CommandSender?) {
        assert(InventorySorter.main != null)
        Objects.requireNonNull(InventorySorter.main!!.rB)?.let { sendMessageToCS(type, it.getString(messageID.iD), cs) }
    }

    @JvmStatic
    fun sendConsoleMessage(type: MessageType, messageID: MessageID) {
        sendMessageToCS(type, messageID, null)
    }

    /**
     * Sends a message with the MessageID `messageID` and the MessageType
     * `messageType` to the CommandSender `cs` (player or console)
     * replacing placeholder using java's String.format(str, args)
     *
     * @param type        the MessageType of the message.
     * @param messageID   the MessageID of the Message.
     * @param cs          the player who should receive the message.
     * @param replacement the replacement variables
     */
    @JvmStatic
    fun sendMessageToCSWithReplacement(
        type: MessageType, messageID: MessageID, cs: CommandSender?, vararg replacement: Any?
    ) {
        assert(InventorySorter.main != null)
        val message = Objects.requireNonNull(InventorySorter.main!!.rB)!!.getString(messageID.iD)
        sendMessageToCS(type, String.format(message, *replacement), cs)
    }

    fun sendPermissionError(sender: CommandSender?, permission: PluginPermissions) {
        sendMessageToCS(MessageType.MISSING_PERMISSION, permission.string, sender)
    }

    private fun getMessageString(type: MessageType, arg: String): Component {
        assert(InventorySorter.main != null)
        var out: Component = Component.text(
            Objects.requireNonNull(
                InventorySorter.main!!.rB
            )!!.getString(MessageID.COMMON_PREFIX.iD) + " "
        )
        out = when (type) {
            MessageType.SYNTAX_ERROR -> out.append(
                Component.text(
                    InventorySorter.main!!.rB!!.getString(MessageID.COMMON_ERROR_SYNTAX.iD) + ": " + arg
                ).color(NamedTextColor.RED)
            )

            MessageType.ERROR -> out.append(
                Component.text(
                    InventorySorter.main!!.rB!!.getString(
                        MessageID.COMMON_ERROR.iD
                    ) + ": " + arg
                ).color(NamedTextColor.RED)
            )

            MessageType.SUCCESS -> out.append(Component.text(arg)).color(NamedTextColor.GREEN)

            MessageType.MISSING_PERMISSION -> out.append(
                Component.text(
                    InventorySorter.main!!.rB!!.getString(MessageID.ERROR_PERMISSION.iD) + " (" + arg + ")"
                ).color(NamedTextColor.RED)
            )

            MessageType.UNHEADED_INFORMATION -> out.append(Component.text(arg)).color(NamedTextColor.GRAY)

        }
        return out
    }
}
