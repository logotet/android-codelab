package com.sap.codelab.model

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

class GeoLocationTest {

    @Test
    fun `display string uses a dot as decimal separator in every locale`() {
        val defaultLocale = Locale.getDefault()
        try {
            Locale.setDefault(Locale.GERMANY)

            assertEquals("49.29330, 8.64200", GeoLocation(49.2933, 8.642).toDisplayString())
        } finally {
            Locale.setDefault(defaultLocale)
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `rejects an invalid latitude`() {
        GeoLocation(latitude = 90.1, longitude = 0.0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `rejects an invalid longitude`() {
        GeoLocation(latitude = 0.0, longitude = -180.1)
    }

    @Test
    fun `a memo without coordinates has no location`() {
        val memo = Memo(1, "Title", "Text", 0, reminderLatitude = null, reminderLongitude = null)

        assertEquals(null, memo.location)
    }
}
