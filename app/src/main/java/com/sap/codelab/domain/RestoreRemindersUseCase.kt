package com.sap.codelab.domain

import androidx.annotation.WorkerThread
import com.sap.codelab.Providers
import com.sap.codelab.location.GeofenceMonitor
import com.sap.codelab.location.LocationReminder
import com.sap.codelab.model.location
import com.sap.codelab.repository.IMemoRepository
import com.sap.codelab.repository.Repository

/**
 * Monitors the locations of all open memos again.
 *
 * The system drops monitored locations e.g. after a reboot, an app update or when location services were switched
 * off, so they need to be restored from the database.
 */
internal class RestoreRemindersUseCase(
    private val repository: IMemoRepository = Repository,
    private val geofenceMonitor: GeofenceMonitor = Providers.geofenceMonitor,
) {

    @WorkerThread
    suspend operator fun invoke(): Result<Unit> {
        val reminders = repository.getOpenWithLocation().mapNotNull { memo ->
            memo.location?.let { location -> LocationReminder(memo.id, location) }
        }
        return geofenceMonitor.register(reminders)
    }
}
