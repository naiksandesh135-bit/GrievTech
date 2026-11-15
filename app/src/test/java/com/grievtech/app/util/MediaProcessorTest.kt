package com.grievtech.app.util

import org.junit.Assert.assertEquals
import org.junit.Test

class MediaProcessorTest {

    @Test
    fun constants_verifyMediaLimits() {
        assertEquals(10 * 1024 * 1024L, MediaProcessor.MAX_IMAGE_SIZE_BYTES)
        assertEquals(50 * 1024 * 1024L, MediaProcessor.MAX_VIDEO_SIZE_BYTES)
        assertEquals(10 * 1024 * 1024L, MediaProcessor.MAX_PDF_SIZE_BYTES)
        assertEquals(1600, MediaProcessor.MAX_IMAGE_DIMENSION)
    }
}
