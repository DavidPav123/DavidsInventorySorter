package utils.messages.enums

enum class MessageID(val iD: String) {
    COMMON_ERROR("common.error"), COMMON_ERROR_SYNTAX("common.error.syntax"), COMMON_PAGE("common.page"), COMMON_PAGE_NEXT(
        "common.page.next"
    ),
    COMMON_PREFIX("common.prefix"), ERROR_BLACKLIST_INVENTORY("error.blacklist.inventory"), ERROR_BLOCK_NO_INVENTORY("error.block.no.inventory"), ERROR_CATEGORY_BOOK(
        "error.category.book"
    ),
    ERROR_CATEGORY_NAME("error.category.name"), ERROR_CATEGORY_NOT_IN_CONFIG("error.category.notinconfig"), ERROR_CATEGORY_INVALID(
        "error.category.invalid"
    ),
    ERROR_PAGE_NUMBER("error.page.number"), ERROR_PERMISSION("error.permission"), ERROR_PLAYER_NOT_ONLINE("error.player.not.online"), ERROR_VALIDATION_BOOLEAN(
        "error.validation.bool"
    ),
    ERROR_VALIDATION_INTEGER("error.validation.integer"), ERROR_WORLD_NAME("error.world.name"), ERROR_YOU_COOLDOWN_SORTING(
        "error.you.cooldown.sorting"
    ),
    ERROR_YOU_COOLDOWN_GENERIC("error.you.cooldown.generic"), INFO_CATEGORY_REMOVED("info.category.removed"), INFO_VALUE_CHANGED(
        "info.value.changed"
    ),

}
