package com.sap.codelab.location

import com.sap.codelab.model.GeoLocation

/**
 * The user "reached" a memo's location once they are within this distance of it.
 */
internal const val REMINDER_RADIUS_METERS: Float = 200f

/**
 * A memo location the user wants to be reminded about.
 */
internal data class LocationReminder(val memoId: Long, val location: GeoLocation)

/**
 * Watches the user's position and reports when they come within [REMINDER_RADIUS_METERS] of a [LocationReminder],
 * also while the app is in the background or not running.
 *
 * Implementations wrap a concrete location library, so it can be swapped without touching the rest of the app.
 */
internal interface GeofenceMonitor {

    /**
     * Starts monitoring the given reminders. A reminder fires when the user arrives, i.e. moves from outside its radius
     * to inside. Registering a reminder again replaces the previous registration.
     */
    suspend fun register(reminders: List<LocationReminder>): Result<Unit>

    /**
     * Stops monitoring the reminder of the given memo.
     */
    suspend fun unregister(memoId: Long): Result<Unit>
}
