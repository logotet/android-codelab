package com.sap.codelab.permission

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

/**
 * Checks the permissions that location reminders depend on.
 */
internal object ReminderPermissions {

    /**
     * Location monitoring needs the precise location; with only the approximate one the 200 m radius is not reliable.
     */
    fun hasPreciseLocation(context: Context): Boolean = isGranted(context, Manifest.permission.ACCESS_FINE_LOCATION)

    /**
     * Needed to monitor locations while the app is in the background or not running. Before Android 10 this is
     * included in the foreground location permission.
     */
    fun hasBackgroundLocation(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            isGranted(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        } else {
            hasPreciseLocation(context)
        }
    }

    /**
     * Before Android 13 notifications don't need a runtime permission, but the user can still turn them off.
     */
    fun canPostNotifications(context: Context): Boolean {
        val permissionGranted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                isGranted(context, Manifest.permission.POST_NOTIFICATIONS)
        return permissionGranted && NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    /**
     * @return true if the app can monitor memo locations in the background.
     */
    fun canMonitorLocations(context: Context): Boolean = hasPreciseLocation(context) && hasBackgroundLocation(context)

    /**
     * @return true if everything is in place for the user to get reminded when reaching a memo location.
     */
    fun canDeliverReminders(context: Context): Boolean = canMonitorLocations(context) && canPostNotifications(context)

    private fun isGranted(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }
}
