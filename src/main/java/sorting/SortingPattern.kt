package sorting

enum class SortingPattern {
    LEFT_TO_RIGHT_TOP_TO_BOTTOM,
    RIGHT_TO_LEFT_BOTTOM_TO_TOP,
    TOP_TO_BOTTOM_LEFT_TO_RIGHT,
    BOTTOM_TO_TOP_LEFT_TO_RIGHT;

    companion object {
        /**
         * Returns the enum object if it is equal to an existing entry.
         *
         * @param str the name of the enum entry.
         * @return the enum entry object or `null` if it does not exist.
         */
		@JvmStatic
		fun getSortingPatternByName(str: String?): SortingPattern? {
            if (str == null) return null
            if (str.equals(BOTTOM_TO_TOP_LEFT_TO_RIGHT.name, ignoreCase = true)) {
                return BOTTOM_TO_TOP_LEFT_TO_RIGHT
            } else if (str.equals(LEFT_TO_RIGHT_TOP_TO_BOTTOM.name, ignoreCase = true)) {
                return LEFT_TO_RIGHT_TOP_TO_BOTTOM
            } else if (str.equals(RIGHT_TO_LEFT_BOTTOM_TO_TOP.name, ignoreCase = true)) {
                return RIGHT_TO_LEFT_BOTTOM_TO_TOP
            } else if (str.equals(TOP_TO_BOTTOM_LEFT_TO_RIGHT.name, ignoreCase = true)) {
                return TOP_TO_BOTTOM_LEFT_TO_RIGHT
            }
            return null
        }
    }
}
