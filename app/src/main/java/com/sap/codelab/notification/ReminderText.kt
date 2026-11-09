package com.sap.codelab.notification

/**
 * Builds the text shown in a reminder.
 */
internal object ReminderText {

    /**
     * The maximum number of characters of the memo text shown in a reminder.
     */
    const val MAX_LENGTH: Int = 140

    /**
     * @return the first [MAX_LENGTH] characters of the given memo text.
     *
     * Characters are counted as Unicode code points, so an emoji counts as one character and is never cut in half.
     */
    fun of(description: String): String {
        if (description.codePointCount(0, description.length) <= MAX_LENGTH) {
            return description
        }
        return description.substring(0, description.offsetByCodePoints(0, MAX_LENGTH))
    }
}
