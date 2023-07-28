package cooldown

interface CooldownManager {
    fun isOnCooldown(obj: Any?): Boolean
}
