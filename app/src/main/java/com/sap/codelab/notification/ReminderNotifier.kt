package com.sap.codelab.notification

import com.sap.codelab.model.Memo

/**
 * Tells the user that they reached the location of a memo.
 */
internal interface ReminderNotifier {

    /**
     * Shows the reminder for the given memo. Showing it again for the same memo replaces the previous one.
     */
    fun show(memo: Memo)

    /**
     * Removes the reminder of the given memo, if it is showing.
     */
    fun cancel(memoId: Long)
}
