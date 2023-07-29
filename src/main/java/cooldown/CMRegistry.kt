package cooldown

import utils.messages.enums.MessageID
import java.util.*

/**
 * This class organizes the CooldownManager. It is used to make multiple CooldownManger instances handy to use.
 */
open class CMRegistry protected constructor() {
    private val cmMap: MutableMap<CMIdentifier, CooldownManager>

    init {
        cmMap = EnumMap(CMIdentifier::class.java)
        register(CMIdentifier.SORTING, PlayerCM(MessageID.ERROR_YOU_COOLDOWN_SORTING, CMIdentifier.SORTING))
        register(
            CMIdentifier.CLEANING_ITEM_GET,
            PlayerCM(MessageID.ERROR_YOU_COOLDOWN_GENERIC, CMIdentifier.CLEANING_ITEM_GET)
        )
    }

    /**
     * Registers a `CooldownManager` with an associated `CMIdentifier`.
     *
     * @param id      the id which you want to associate with the manager.
     * @param manager the manager which you want to associate with the id.
     */
    private fun register(id: CMIdentifier, manager: CooldownManager) {
        cmMap[id] = manager
    }

    private fun isObjOnCooldown(id: CMIdentifier, obj: Any): Boolean {
        val manger = cmMap[id]
        return manger?.isOnCooldown(obj) ?: false
    }

    enum class CMIdentifier {
        SORTING,
        CLEANING_ITEM_GET
    }

    companion object {
        private var instance: CMRegistry? = null
            /**
             * Returns the instance of this singleton class.
             *
             * @return the instance of this singleton class.
             */
            get() {
                if (field == null) {
                    field = CMRegistry()
                }
                return field
            }

        /**
         * Returns the result of the associated `CooldownManager`. If the manager is null it returns false.
         *
         * @param id  the id associated with the `CooldownManager`.
         * @param obj the object the method hands over to the isOnCooldown(Object) method of the `CooldownManager`.
         * @return the result of the isOnCooldown(Object) method of the `CooldownManager` or false if the manager is null.
         */
        @JvmStatic
        fun isOnCooldown(id: CMIdentifier, obj: Any): Boolean {
            return instance!!.isObjOnCooldown(id, obj)
        }
    }
}
