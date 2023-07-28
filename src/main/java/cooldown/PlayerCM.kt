package cooldown;

import config.PluginConfigManager;
import utils.PluginPermissions;
import utils.messages.MessageSystem;
import utils.messages.enums.MessageID;
import utils.messages.enums.MessageType;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerCM implements CooldownManager {

    private MessageID msgId;
    private Map<UUID, Long> map;
    private CMRegistry.CMIdentifier id;
    private final long immuneTime = 100;

    public PlayerCM(MessageID msgId, CMRegistry.CMIdentifier id) {
        this.msgId = msgId;
        this.id = id;
        map = new HashMap<>();
        if (id == null) throw new IllegalArgumentException("The CMId is not allowed to be null.");
    }

    @Override
    public boolean isOnCooldown(Object obj) {

        if (obj == null) {
            return false;
        } else if (!(obj instanceof Player)) {
            return false;
        }

        Player player = (Player) obj;

        boolean immune = !PluginConfigManager.isCooldownActive(id)
                || player.hasPermission(PluginPermissions.COOLDOWN_IMMUNE.getString());

        if (map.containsKey(player.getUniqueId())) {
            long difference = System.currentTimeMillis() - map.get(player.getUniqueId());
            int cooldown = PluginConfigManager.getCooldown(id);

            if (immune) {
                if (difference >= immuneTime) {
                    map.put(player.getUniqueId(), System.currentTimeMillis());
                    return false;
                } else {
                    return true;
                }
            }

            if (difference >= cooldown) {
                map.put(player.getUniqueId(), System.currentTimeMillis());
                return false;
            }

            MessageSystem.sendMessageToCSWithReplacement(MessageType.ERROR, msgId, player,
                    String.valueOf((cooldown - difference) / 1000 + 1));
            return true;

        } else {
            map.put(player.getUniqueId(), System.currentTimeMillis());
            return false;
        }

    }
}
