package cooldown

import config.PluginConfigManager
import cooldown.CMRegistry.CMIdentifier
import org.bukkit.entity.Player
import utils.PluginPermissions
import utils.messages.MessageSystem
import utils.messages.enums.MessageID
import utils.messages.enums.MessageType
import java.util.*

class PlayerCM(private val msgId: MessageID, private val id: CMIdentifier?) : CooldownManager {
    private val map: MutableMap<UUID, Long>

    init {
        map = HashMap()
        requireNotNull(id) { "The CMId is not allowed to be null." }
    }

    override fun isOnCooldown(obj: Any?): Boolean {
        if (obj == null) {
            return false
        } else if (obj !is Player) {
            return false
        }
        val immune = (!PluginConfigManager.isCooldownActive(id)
                || obj.hasPermission(PluginPermissions.COOLDOWN_IMMUNE.string))
        return if (map.containsKey(obj.uniqueId)) {
            val difference = System.currentTimeMillis() - map[obj.uniqueId]!!
            val cooldown = PluginConfigManager.getCooldown(id)
            if (immune) {
                val immuneTime: Long = 100
                return if (difference >= immuneTime) {
                    map[obj.uniqueId] = System.currentTimeMillis()
                    false
                } else {
                    true
                }
            }
            if (difference >= cooldown) {
                map[obj.uniqueId] = System.currentTimeMillis()
                return false
            }
            MessageSystem.sendMessageToCSWithReplacement(
                MessageType.ERROR,
                msgId,
                obj,
                ((cooldown - difference) / 1000 + 1).toString()
            )
            true
        } else {
            map[obj.uniqueId] = System.currentTimeMillis()
            false
        }
    }
}
