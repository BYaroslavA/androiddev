package com.example.myapplication123

import android.graphics.ColorMatrix
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ImageProcessorTest {

    @Test
    fun testAllFiltersAvailable() {
        val filters = FilterType.values()
        assertTrue("Should have at least 10 filters", filters.size >= 10)
        assertNotNull(FilterType.valueOf("ORIGINAL"))
        assertNotNull(FilterType.valueOf("BW"))
        assertNotNull(FilterType.valueOf("SEPIA"))
        assertNotNull(FilterType.valueOf("INVERT"))
        assertNotNull(FilterType.valueOf("VINTAGE"))
        assertNotNull(FilterType.valueOf("CYBERPUNK"))
    }

    @Test
    fun testFilterDisplayNamesNonEmpty() {
        for (filter in FilterType.values()) {
            assertTrue("Display name should not be blank for ${filter.name}", filter.displayName.isNotBlank())
        }
    }
}
