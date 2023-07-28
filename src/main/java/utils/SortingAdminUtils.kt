package utils

import org.bukkit.Sound
import java.util.*
import java.util.stream.Collectors

object SortingAdminUtils {
    @JvmStatic
    fun getSoundByName(name: String?): Sound? {
        val soundList = Arrays.stream(Sound.entries.toTypedArray())
            .filter { s: Sound -> s.toString().equals(name, ignoreCase = true) }
            .collect(Collectors.toList())
        return if (soundList.size > 0) soundList[0] else null
    }
}
