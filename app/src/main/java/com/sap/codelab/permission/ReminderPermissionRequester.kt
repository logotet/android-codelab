package com.sap.codelab.permission

import android.Manifest
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.sap.codelab.R

/**
 * Asks the user for the permissions location reminders need, at the moment they are needed.
 *
 * Android only allows asking for the background location after the foreground location has been granted, so the
 * permissions are requested one after the other: precise location → background location → notifications.
 *
 * Must be created before the activity is started, e.g. as a property initializer of the activity.
 */
internal class ReminderPermissionRequester(private val activity: AppCompatActivity) {

    private var onPreciseLocationDone: (() -> Unit)? = null
    private var onReminderPermissionsDone: ((Boolean) -> Unit)? = null

    private val preciseLocationLauncher =
        activity.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            val callback = onPreciseLocationDone
            onPreciseLocationDone = null
            callback?.invoke()
        }

    private val backgroundLocationLauncher =
        activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) requestNotifications() else finishReminderPermissions()
        }

    private val notificationsLauncher =
        activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            finishReminderPermissions()
        }

    /**
     * Asks for the precise location, unless it has been granted already.
     *
     * @param onDone - called afterwards, whether the permission was granted or not.
     */
    fun requestPreciseLocation(onDone: () -> Unit) {
        if (ReminderPermissions.hasPreciseLocation(activity)) {
            onDone()
            return
        }
        onPreciseLocationDone = onDone
        // The user can still pick "approximate" only; asking for both is required since Android 12.
        preciseLocationLauncher.launch(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        )
    }

    /**
     * Asks for all missing permissions that reminders need.
     *
     * @param onDone - called afterwards with true if reminders can be delivered, false if a permission is missing.
     */
    fun requestReminderPermissions(onDone: (Boolean) -> Unit) {
        onReminderPermissionsDone = onDone
        requestPreciseLocation {
            if (ReminderPermissions.hasPreciseLocation(activity)) requestBackgroundLocation() else finishReminderPermissions()
        }
    }

    private fun requestBackgroundLocation() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q || ReminderPermissions.hasBackgroundLocation(activity)) {
            requestNotifications()
            return
        }
        // Since Android 11 the system shows no dialog for this permission but opens the app's location settings,
        // so the user needs to know beforehand what to choose there.
        AlertDialog.Builder(activity)
            .setTitle(R.string.background_location_rationale_title)
            .setMessage(R.string.background_location_rationale_message)
            .setPositiveButton(R.string.action_continue) { _, _ ->
                backgroundLocationLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            }
            .setNegativeButton(R.string.action_not_now) { _, _ -> finishReminderPermissions() }
            .setOnCancelListener { finishReminderPermissions() }
            .show()
    }

    private fun requestNotifications() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || ReminderPermissions.canPostNotifications(activity)) {
            finishReminderPermissions()
            return
        }
        notificationsLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    private fun finishReminderPermissions() {
        val callback = onReminderPermissionsDone
        onReminderPermissionsDone = null
        callback?.invoke(ReminderPermissions.canDeliverReminders(activity))
    }
}
