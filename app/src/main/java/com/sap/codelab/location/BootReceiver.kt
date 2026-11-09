package com.sap.codelab.location

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.sap.codelab.domain.RestoreRemindersUseCase
import com.sap.codelab.utils.coroutines.ScopeProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val TAG = "BootReceiver"

/**
 * Monitored locations are dropped by the system when the device reboots or the app is updated.
 * This receiver restores them, so reminders keep working without the user opening the app.
 */
internal class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED && intent.action != Intent.ACTION_MY_PACKAGE_REPLACED) {
            return
        }
        val pendingResult = goAsync()
        ScopeProvider.application.launch(Dispatchers.IO) {
            try {
                RestoreRemindersUseCase()()
            } catch (exception: Exception) {
                Log.e(TAG, "Could not restore reminders", exception)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
