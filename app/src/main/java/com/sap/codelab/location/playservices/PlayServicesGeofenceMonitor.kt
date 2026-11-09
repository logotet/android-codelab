package com.sap.codelab.location.playservices

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.sap.codelab.location.GeofenceMonitor
import com.sap.codelab.location.LocationReminder
import com.sap.codelab.location.REMINDER_RADIUS_METERS
import com.sap.codelab.permission.ReminderPermissions
import kotlinx.coroutines.tasks.await

private const val TAG = "GeofenceMonitor"

/**
 * Google Play services allows at most 100 geofences per app.
 */
private const val MAX_GEOFENCES = 100

/**
 * [GeofenceMonitor] backed by the Google Play services Geofencing API.
 *
 * The system tracks the user's position in a battery efficient way and wakes the app up through
 * [GeofenceBroadcastReceiver] when a geofence is entered, even if the app is not running.
 */
internal class PlayServicesGeofenceMonitor(context: Context) : GeofenceMonitor {

    private val appContext = context.applicationContext
    private val geofencingClient = LocationServices.getGeofencingClient(appContext)

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(appContext, GeofenceBroadcastReceiver::class.java)
        // Has to be mutable: Play services adds the geofencing event to the intent.
        val mutableFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE else 0
        PendingIntent.getBroadcast(appContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or mutableFlag)
    }

    @SuppressLint("MissingPermission") // Checked through ReminderPermissions
    override suspend fun register(reminders: List<LocationReminder>): Result<Unit> {
        if (reminders.isEmpty()) {
            return Result.success(Unit)
        }
        if (!ReminderPermissions.canMonitorLocations(appContext)) {
            Log.w(TAG, "Location permissions missing, reminders are not monitored")
            return Result.failure(SecurityException("Precise and background location permissions are required"))
        }
        if (reminders.size > MAX_GEOFENCES) {
            Log.w(TAG, "${reminders.size} reminders, only the first $MAX_GEOFENCES can be monitored")
        }
        val request = GeofencingRequest.Builder()
            // 0 = no initial trigger: being inside the radius already doesn't fire, only entering it from outside.
            // Otherwise restoring the reminders (e.g. after a reboot) would remind the user again where they are.
            .setInitialTrigger(0)
            .addGeofences(reminders.take(MAX_GEOFENCES).map { it.toGeofence() })
            .build()
        return runGeofencingCall {
            geofencingClient.addGeofences(request, geofencePendingIntent).await()
        }
    }

    override suspend fun unregister(memoId: Long): Result<Unit> = runGeofencingCall {
        geofencingClient.removeGeofences(listOf(memoId.toRequestId())).await()
    }

    private suspend fun runGeofencingCall(call: suspend () -> Unit): Result<Unit> {
        return try {
            call()
            Result.success(Unit)
        } catch (exception: ApiException) {
            // E.g. GEOFENCE_NOT_AVAILABLE when location services are switched off, or GEOFENCE_TOO_MANY_GEOFENCES.
            Log.w(TAG, "Geofencing failed: ${GeofenceStatusCodes.getStatusCodeString(exception.statusCode)}")
            Result.failure(exception)
        } catch (exception: SecurityException) {
            // The user revoked a permission in the meantime.
            Log.w(TAG, "Geofencing failed: permission missing", exception)
            Result.failure(exception)
        }
    }

    private fun LocationReminder.toGeofence(): Geofence {
        return Geofence.Builder()
            .setRequestId(memoId.toRequestId())
            .setCircularRegion(location.latitude, location.longitude, REMINDER_RADIUS_METERS)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .build()
    }
}

/**
 * The geofence request id is the memo id, so a triggered geofence can be mapped back to its memo.
 */
internal fun Long.toRequestId(): String = toString()
