package com.sap.codelab.view.pick

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.sap.codelab.Providers
import com.sap.codelab.R
import com.sap.codelab.databinding.ActivityPickLocationBinding
import com.sap.codelab.map.LocationPickerMap
import com.sap.codelab.map.ZOOM_COUNTRY
import com.sap.codelab.map.ZOOM_STREET
import com.sap.codelab.model.GeoLocation
import com.sap.codelab.permission.ReminderPermissions

private const val KEY_SELECTED_LOCATION = "selectedLocation"

/**
 * Shown while neither a picked location nor the user's own position is known: SAP's headquarters in Walldorf.
 */
private val DEFAULT_LOCATION = GeoLocation(latitude = 49.2933, longitude = 8.6420)

/**
 * Activity that lets the user pick a location on a map. Use it through [PickLocationContract].
 */
internal class PickLocation : AppCompatActivity() {

    private lateinit var binding: ActivityPickLocationBinding
    private lateinit var map: LocationPickerMap
    private var selectedLocation: GeoLocation? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPickLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setSubtitle(R.string.pick_location_hint)

        selectedLocation = savedInstanceState?.getGeoLocation(KEY_SELECTED_LOCATION) ?: intent.getGeoLocation()
        setupMap()
    }

    private fun setupMap() {
        map = Providers.mapFactory.create()
        map.attach(binding.mapContainer, lifecycle)
        map.setOnMapTapListener { location ->
            selectedLocation = location
            map.setMarker(location)
            invalidateOptionsMenu()
        }
        val selected = selectedLocation
        map.setMarker(selected)
        if (selected != null) {
            map.moveTo(selected, ZOOM_STREET)
        } else {
            map.moveTo(DEFAULT_LOCATION, ZOOM_COUNTRY)
        }
        if (ReminderPermissions.hasPreciseLocation(this)) {
            map.showMyLocation { myLocation ->
                // Don't move the map away from a location the user already picked
                if (selectedLocation == null) {
                    map.moveTo(myLocation, ZOOM_STREET)
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putGeoLocation(KEY_SELECTED_LOCATION, selectedLocation)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_pick_location, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.findItem(R.id.action_confirm).isEnabled = selectedLocation != null
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_confirm -> {
                setResult(RESULT_OK, Intent().putGeoLocation(selectedLocation))
                finish()
                true
            }

            else                -> super.onOptionsItemSelected(item)
        }
    }
}
