package utils

import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BookMeta
import utils.messages.MessageSystem
import utils.messages.enums.MessageID
import utils.messages.enums.MessageType
import kotlin.math.ceil

object StringUtils {
    @JvmStatic
    fun isStringBoolean(sender: CommandSender?, bool: String): Boolean {
        if (isStringNotTrueOrFalse(bool)) {
            MessageSystem.sendMessageToCS(MessageType.ERROR, MessageID.ERROR_VALIDATION_BOOLEAN, sender)
            return false
        }
        return true
    }

    @JvmStatic
    fun isStringNotTrueOrFalse(str: String): Boolean {
        return !str.equals(
            java.lang.Boolean.TRUE.toString(),
            ignoreCase = true
        ) && !str.equals(java.lang.Boolean.FALSE.toString(), ignoreCase = true)
    }

    @JvmStatic
    fun getAsBook(string: String): ItemStack {
        val book = ItemStack(Material.WRITABLE_BOOK)
        val bm = (book.itemMeta as BookMeta)
        bm.pages = separateIntoPages(string)
        book.setItemMeta(bm)
        return book
    }

    private fun separateIntoPages(string: String): List<String> {
        val pages: MutableList<String> = ArrayList()
        var curPage = ""
        val maxLines = 14
        var curPageLines = 0
        // pages
        for (line in string.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
            val lineCount = countLinesNeeded(line)
            if (curPageLines + lineCount <= maxLines) {
                curPageLines += lineCount
                curPage = curPage + line + "\n"
            } else {
                pages.add(curPage)
                curPage = line + "\n"
                curPageLines = lineCount
            }
        }
        pages.add(curPage)
        return pages
    }

    private fun countLinesNeeded(string: String): Int {
        val maxCharsPerLine = 19.0
        var countLines = 1
        var curLineChars = 0
        for (part in string.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
            if (part.length + curLineChars < maxCharsPerLine) {
                curLineChars += part.length + 1 // +1 for the space that belongs between the parts
            } else if (part.length <= maxCharsPerLine) {
                countLines++
                curLineChars = part.length
            } else {
                val lineRatio = part.length / maxCharsPerLine
                countLines = (countLines + ceil(lineRatio)).toInt()
                curLineChars = (part.length - lineRatio.toInt() * maxCharsPerLine).toInt()
            }
        }
        return countLines
    }
}
