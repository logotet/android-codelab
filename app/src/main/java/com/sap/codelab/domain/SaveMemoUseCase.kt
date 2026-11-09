package com.sap.codelab.domain

import androidx.annotation.WorkerThread
import com.sap.codelab.Providers
import com.sap.codelab.location.GeofenceMonitor
import com.sap.codelab.location.LocationReminder
import com.sap.codelab.model.Memo
import com.sap.codelab.model.location
import com.sap.codelab.repository.IMemoRepository
import com.sap.codelab.repository.Repository

/**
 * Saves a new memo and starts monitoring its location, so the user gets reminded once they get there.
 */
internal class SaveMemoUseCase(
    private val repository: IMemoRepository = Repository,
    private val geofenceMonitor: GeofenceMonitor = Providers.geofenceMonitor,
) {

    /**
     * @return true if the location reminder is active; false if the memo has no location or monitoring could not be
     * started, e.g. because a permission is missing. The memo is saved either way.
     */
    @WorkerThread
    suspend operator fun invoke(memo: Memo): Boolean {
        val memoId = repository.saveMemo(memo)
        val location = memo.location ?: return false
        return geofenceMonitor.register(listOf(LocationReminder(memoId, location))).isSuccess
    }
}
