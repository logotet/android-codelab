package com.sap.codelab.location.playservices

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import com.sap.codelab.Providers
import com.sap.codelab.repository.Repository
import com.sap.codelab.utils.coroutines.ScopeProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val TAG = "GeofenceReceiver"

/**
 * Receives the geofence transitions from Google Play services. The system starts the app process for this broadcast
 * if needed, so reminders also work while the app is not running.
 */
internal class GeofenceBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val event = GeofencingEvent.fromIntent(intent) ?: return
        if (event.hasError()) {
            // E.g. GEOFENCE_NOT_AVAILABLE: location services were switched off and the system dropped all geofences.
            // They are restored the next time the app starts.
            Log.w(TAG, "Geofencing error: ${GeofenceStatusCodes.getStatusCodeString(event.errorCode)}")
            return
        }
        if (event.geofenceTransition != Geofence.GEOFENCE_TRANSITION_ENTER) {
            return
        }
        val memoIds = event.triggeringGeofences.orEmpty().mapNotNull { it.requestId.toLongOrNull() }
        if (memoIds.isEmpty()) {
            return
        }
        // Keeps the receiver alive while the memos are read from the database.
        val pendingResult = goAsync()
        ScopeProvider.application.launch(Dispatchers.IO) {
            try {
                memoIds.forEach { memoId ->
                    val memo = Repository.getMemoById(memoId)
                    // Done memos don't remind anymore
                    if (memo != null && !memo.isDone) {
                        Providers.reminderNotifier.show(memo)
                    }
                }
            } catch (exception: Exception) {
                Log.e(TAG, "Could not show reminders for memos $memoIds", exception)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
