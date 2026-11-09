package com.sap.codelab.map.osmdroid

import android.content.Context
import com.sap.codelab.map.LocationPickerMap
import com.sap.codelab.map.MapFactory
import org.osmdroid.config.Configuration
import org.osmdroid.config.IConfigurationProvider
import java.io.File

/**
 * [MapFactory] for OpenStreetMap maps rendered by osmdroid. Needs no API key.
 */
internal class OsmdroidMapFactory(context: Context) : MapFactory {

    private val appContext = context.applicationContext

    /**
     * osmdroid is configured once, when the first map is created, so app start-up doesn't pay for it.
     */
    private val configuration: IConfigurationProvider by lazy {
        Configuration.getInstance().apply {
            load(appContext, appContext.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
            // The OpenStreetMap tile usage policy requires a user agent that identifies the app.
            userAgentValue = appContext.packageName
            // Keep the tile cache in app-private storage, so no storage permission is needed.
            osmdroidBasePath = File(appContext.cacheDir, "osmdroid")
            osmdroidTileCache = File(osmdroidBasePath, "tiles")
        }
    }

    override fun create(): LocationPickerMap {
        configuration
        return OsmdroidLocationPickerMap()
    }
}
