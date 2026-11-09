package com.sap.codelab.repository

import android.app.Application
import com.sap.codelab.Providers

/**
 * Extension of the Android Application class.
 */
internal class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Repository.initialize(this)
        Providers.initialize(this)
    }
}