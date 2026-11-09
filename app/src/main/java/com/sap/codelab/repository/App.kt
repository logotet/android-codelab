package com.sap.codelab.repository

import android.app.Application
import android.util.Log
import com.sap.codelab.Providers
import com.sap.codelab.domain.RestoreRemindersUseCase
import com.sap.codelab.utils.coroutines.ScopeProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Extension of the Android Application class.
 */
internal class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Repository.initialize(this)
        Providers.initialize(this)
        restoreReminders()
    }

    /**
     * The system drops monitored locations e.g. when location services are switched off, or when Google Play services'
     * data is cleared, without telling the app afterwards. Restoring them whenever the app starts keeps reminders
     * working. Registering a monitored location again just replaces it, so this is safe to do every time.
     */
    private fun restoreReminders() {
        ScopeProvider.application.launch(Dispatchers.IO) {
            try {
                RestoreRemindersUseCase()()
            } catch (exception: Exception) {
                Log.e("App", "Could not restore reminders", exception)
            }
        }
    }
}
