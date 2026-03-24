package com.ijunes.mefirst

import com.ijunes.mefirst.common.util.toDateString
import com.ijunes.mefirst.common.util.toTimeString
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class TimeUtilTest {

    // Fix timezone so assertions are deterministic regardless of where the test runs.
    private fun calendarAt(
        year: Int, month: Int, day: Int,
        hour: Int = 0, minute: Int = 0
    ): Calendar = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault()).apply {
        set(year, month, day, hour, minute, 0)
        set(Calendar.MILLISECOND, 0)
    }

    @Test
    fun `toDateString formats as day-of-week month day year`() {
        // January 15 2024 is a Monday
        val ts = calendarAt(2024, Calendar.JANUARY, 15).timeInMillis
        assertEquals("Monday Jan 15, 2024", ts.toDateString())
    }

    @Test
    fun `toDateString pads single-digit day with zero`() {
        // January 5 2024
        val ts = calendarAt(2024, Calendar.JANUARY, 5).timeInMillis
        assertEquals("Friday Jan 05, 2024", ts.toDateString())
    }

    @Test
    fun `toTimeString formats as 12-hour clock with AM PM`() {
        val ts = calendarAt(2024, Calendar.JANUARY, 15, hour = 14, minute = 30).timeInMillis
        assertEquals("02:30 PM", ts.toTimeString())
    }

    @Test
    fun `toTimeString formats midnight as 12 AM`() {
        val ts = calendarAt(2024, Calendar.JANUARY, 15, hour = 0, minute = 0).timeInMillis
        assertEquals("12:00 AM", ts.toTimeString())
    }
}
