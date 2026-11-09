package com.sap.codelab.map

import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import com.sap.codelab.model.GeoLocation

/**
 * Zoom level that shows a whole country.
 */
internal const val ZOOM_COUNTRY: Double = 6.0

/**
 * Zoom level that shows a few streets, which is precise enough to pick a point within the reminder radius.
 */
internal const val ZOOM_STREET: Double = 16.0

/**
 * Creates maps. Implementations wrap a concrete map library, so it can be swapped without touching the screens.
 */
internal interface MapFactory {

    fun create(): LocationPickerMap
}

/**
 * An interactive map on which the user can pick a location.
 */
internal interface LocationPickerMap {

    /**
     * Creates the map view inside the given container and ties it to the given lifecycle.
     * Must be called once, before any other method.
     */
    fun attach(container: ViewGroup, lifecycle: Lifecycle)

    /**
     * Shows a marker at the given location, or removes the marker if the location is null.
     */
    fun setMarker(location: GeoLocation?)

    /**
     * Moves the visible area so that it is centered on the given location.
     */
    fun moveTo(location: GeoLocation, zoom: Double)

    /**
     * Sets the listener that is called with the tapped location whenever the user taps the map.
     */
    fun setOnMapTapListener(listener: ((GeoLocation) -> Unit)?)

    /**
     * Shows the user's own position on the map. Requires the location permission.
     *
     * @param onFirstFix - called on the main thread once the user's position is known for the first time.
     */
    fun showMyLocation(onFirstFix: ((GeoLocation) -> Unit)?)
}
