package com.sap.codelab.domain

import androidx.annotation.WorkerThread
import com.sap.codelab.Providers
import com.sap.codelab.location.GeofenceMonitor
import com.sap.codelab.model.Memo
import com.sap.codelab.notification.ReminderNotifier
import com.sap.codelab.repository.IMemoRepository
import com.sap.codelab.repository.Repository

/**
 * Marks a memo as done. A done memo no longer reminds the user.
 */
internal class CompleteMemoUseCase(
    private val repository: IMemoRepository = Repository,
    private val geofenceMonitor: GeofenceMonitor = Providers.geofenceMonitor,
    private val reminderNotifier: ReminderNotifier = Providers.reminderNotifier,
) {

    @WorkerThread
    suspend operator fun invoke(memo: Memo) {
        repository.saveMemo(memo.copy(isDone = true))
        geofenceMonitor.unregister(memo.id)
        reminderNotifier.cancel(memo.id)
    }
}
