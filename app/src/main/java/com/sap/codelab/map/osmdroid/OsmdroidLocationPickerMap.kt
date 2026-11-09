package com.sap.codelab.map.osmdroid

import android.view.ViewGroup
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.sap.codelab.map.LocationPickerMap
import com.sap.codelab.model.GeoLocation
import org.osmdroid.api.IGeoPoint
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.CopyrightOverlay
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

/**
 * Below this zoom level the world map gets smaller than the screen.
 */
private const val MIN_ZOOM = 3.0

/**
 * [LocationPickerMap] backed by an osmdroid [MapView] showing OpenStreetMap tiles.
 */
internal class OsmdroidLocationPickerMap : LocationPickerMap, DefaultLifecycleObserver {

    private var mapView: MapView? = null
    private var lifecycle: Lifecycle? = null
    private var marker: Marker? = null
    private var myLocationOverlay: MyLocationNewOverlay? = null
    private var tapListener: ((GeoLocation) -> Unit)? = null

    override fun attach(container: ViewGroup, lifecycle: Lifecycle) {
        check(mapView == null) { "The map is already attached" }
        val map = MapView(container.context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            setTilesScaledToDpi(true)
            setMinZoomLevel(MIN_ZOOM)
            setHorizontalMapRepetitionEnabled(false)
            setVerticalMapRepetitionEnabled(false)
            // At the bottom, so that taps on other overlays (e.g. the marker) are handled by those first
            overlays.add(0, MapEventsOverlay(TapReceiver()))
            // "© OpenStreetMap contributors", required by the OpenStreetMap tile usage policy
            overlays.add(CopyrightOverlay(container.context))
        }
        container.addView(map, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        mapView = map
        this.lifecycle = lifecycle
        lifecycle.addObserver(this)
    }

    override fun setMarker(location: GeoLocation?) {
        val map = mapView ?: return
        if (location == null) {
            marker?.let { map.overlays.remove(it) }
            marker = null
        } else {
            val currentMarker = marker ?: Marker(map).apply {
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                // No info window; a tap on the marker falls through to the map, which moves the marker there.
                setOnMarkerClickListener { _, _ -> false }
                map.overlays.add(this)
            }
            currentMarker.position = location.toGeoPoint()
            marker = currentMarker
        }
        map.invalidate()
    }

    override fun moveTo(location: GeoLocation, zoom: Double) {
        mapView?.controller?.apply {
            setZoom(zoom)
            setCenter(location.toGeoPoint())
        }
    }

    override fun setOnMapTapListener(listener: ((GeoLocation) -> Unit)?) {
        tapListener = listener
    }

    override fun showMyLocation(onFirstFix: ((GeoLocation) -> Unit)?) {
        val map = mapView ?: return
        if (myLocationOverlay != null) {
            return
        }
        val overlay = MyLocationNewOverlay(GpsMyLocationProvider(map.context), map)
        if (onFirstFix != null) {
            // Called on a background thread
            overlay.runOnFirstFix {
                val fix = overlay.myLocation ?: return@runOnFirstFix
                map.post { onFirstFix(fix.toGeoLocation()) }
            }
        }
        if (lifecycle?.currentState?.isAtLeast(Lifecycle.State.RESUMED) == true) {
            overlay.enableMyLocation()
        }
        map.overlays.add(overlay)
        myLocationOverlay = overlay
    }

    override fun onResume(owner: LifecycleOwner) {
        mapView?.onResume()
        myLocationOverlay?.enableMyLocation()
    }

    override fun onPause(owner: LifecycleOwner) {
        myLocationOverlay?.disableMyLocation()
        mapView?.onPause()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        owner.lifecycle.removeObserver(this)
        mapView?.onDetach()
        mapView = null
        lifecycle = null
        marker = null
        myLocationOverlay = null
        tapListener = null
    }

    private inner class TapReceiver : MapEventsReceiver {

        override fun singleTapConfirmedHelper(point: GeoPoint): Boolean {
            val listener = tapListener ?: return false
            listener(point.toGeoLocation())
            return true
        }

        override fun longPressHelper(point: GeoPoint): Boolean = false
    }
}

private fun GeoLocation.toGeoPoint(): GeoPoint = GeoPoint(latitude, longitude)

private fun IGeoPoint.toGeoLocation(): GeoLocation {
    // Map projections can return longitudes outside of -180..180 when the map is scrolled around the globe.
    val normalizedLongitude = ((longitude + 180.0) % 360.0 + 360.0) % 360.0 - 180.0
    return GeoLocation(latitude.coerceIn(-90.0, 90.0), normalizedLongitude)
}
