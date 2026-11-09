package com.sap.codelab.model

import java.util.Locale

/**
 * A point on earth in WGS84 coordinates.
 *
 * This is the app's own location type: map and location libraries convert to and from it at their boundary,
 * so none of their types leak into the rest of the code base.
 */
internal data class GeoLocation(val latitude: Double, val longitude: Double) {

    init {
        require(latitude in -90.0..90.0) { "Latitude out of range: $latitude" }
        require(longitude in -180.0..180.0) { "Longitude out of range: $longitude" }
    }

    /**
     * @return a locale independent, human readable representation, e.g. "49.29330, 8.64200".
     */
    fun toDisplayString(): String = String.format(Locale.ROOT, "%.5f, %.5f", latitude, longitude)
}
