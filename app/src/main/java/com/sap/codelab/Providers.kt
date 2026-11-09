package com.sap.codelab

import android.content.Context
import com.sap.codelab.map.MapFactory
import com.sap.codelab.map.osmdroid.OsmdroidMapFactory

/**
 * Provides the implementations of the abstractions around 3rd party libraries.
 *
 * This is the only place that decides which library is used: plugging in another one (e.g. Google Maps instead of
 * osmdroid) means writing a new implementation of the interface and changing one line here.
 */
internal object Providers {

    lateinit var mapFactory: MapFactory
        private set

    fun initialize(applicationContext: Context) {
        mapFactory = OsmdroidMapFactory(applicationContext)
    }
}
