package com.sap.codelab.view.pick

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContract
import com.sap.codelab.model.GeoLocation

private const val EXTRA_LOCATION = "location"

/**
 * Opens [PickLocation] and returns the location the user picked, or null if they went back without picking one.
 * The input is the location to show as already selected, if any.
 */
internal class PickLocationContract : ActivityResultContract<GeoLocation?, GeoLocation?>() {

    override fun createIntent(context: Context, input: GeoLocation?): Intent {
        return Intent(context, PickLocation::class.java).putGeoLocation(input)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): GeoLocation? {
        return if (resultCode == Activity.RESULT_OK) intent?.getGeoLocation() else null
    }
}

internal fun Intent.putGeoLocation(location: GeoLocation?): Intent {
    return if (location == null) this else putExtra(EXTRA_LOCATION, location.toArray())
}

internal fun Intent.getGeoLocation(): GeoLocation? = getDoubleArrayExtra(EXTRA_LOCATION)?.toGeoLocation()

internal fun Bundle.putGeoLocation(key: String, location: GeoLocation?) {
    putDoubleArray(key, location?.toArray())
}

internal fun Bundle.getGeoLocation(key: String): GeoLocation? = getDoubleArray(key)?.toGeoLocation()

private fun GeoLocation.toArray(): DoubleArray = doubleArrayOf(latitude, longitude)

private fun DoubleArray.toGeoLocation(): GeoLocation = GeoLocation(latitude = this[0], longitude = this[1])
