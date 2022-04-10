package io.github.wykopmobilny.utils

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class ExtensionsTest {

    @Test
    fun `test youtubeTimestampToMsOrNull`() {
        val format1 = "5" // 5 seconds = 5.000ms
        val format2 = "3h3m30s" // 11.010.000ms
        val format3 = "1m10s" // 70.000ms
        val format4 = "25s" // 25.000ms
        val format5 = "malformed_time"

        assertEquals(5_000L, format1.youtubeTimestampToMsOrNull())
        assertEquals(11_010_000L, format2.youtubeTimestampToMsOrNull())
        assertEquals(70_000L, format3.youtubeTimestampToMsOrNull())
        assertEquals(25_000L, format4.youtubeTimestampToMsOrNull())
        assertNull(format5.youtubeTimestampToMsOrNull())
    }
}
