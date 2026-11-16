package com.grievtech.app.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GeoTagReaderTest {

    @Test
    fun geoPoint_validationRules() {
        assertTrue(GeoPoint(12.9716, 77.5946).isValid())
        assertFalse(GeoPoint(0.0, 0.0).isValid())
        assertFalse(GeoPoint(95.0, 77.5946).isValid())
        assertFalse(GeoPoint(12.9716, 200.0).isValid())
    }

    @Test
    fun parseIso6709Location_validString_parsesCoordinates() {
        val point1 = GeoTagReader.parseIso6709Location("+37.4219-122.0840/")
        assertNotNull(point1)
        assertEquals(37.4219, point1!!.latitude, 0.0001)
        assertEquals(-122.0840, point1.longitude, 0.0001)

        val point2 = GeoTagReader.parseIso6709Location("+12.9716+077.5946/")
        assertNotNull(point2)
        assertEquals(12.9716, point2!!.latitude, 0.0001)
        assertEquals(77.5946, point2.longitude, 0.0001)
    }

    @Test
    fun parseIso6709Location_invalidOrNullString_returnsNull() {
        assertNull(GeoTagReader.parseIso6709Location(null))
        assertNull(GeoTagReader.parseIso6709Location(""))
        assertNull(GeoTagReader.parseIso6709Location("invalid location string"))
    }
}
