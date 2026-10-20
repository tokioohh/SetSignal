package com.example.setsignal.ui

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Locale
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class FormattingTest {

    private lateinit var originalLocale: Locale

    @Before
    fun setUp() {
        originalLocale = Locale.getDefault()
        Locale.setDefault(Locale.US)
    }

    @After
    fun tearDown() {
        Locale.setDefault(originalLocale)
    }

    @Test
    fun `elapsed time omits hours until needed`() {
        assertEquals("0:00", formatElapsed(0))
        assertEquals("0:09", formatElapsed(9))
        assertEquals("0:59", formatElapsed(59))
        assertEquals("1:00", formatElapsed(60))
        assertEquals("59:59", formatElapsed(3_599))
    }

    @Test
    fun `elapsed time includes hours after sixty minutes`() {
        assertEquals("1:00:00", formatElapsed(3_600))
        assertEquals("1:01:01", formatElapsed(3_661))
        assertEquals("10:00:00", formatElapsed(36_000))
    }

    @Test
    fun `elapsed time clamps negative values to zero`() {
        assertEquals("0:00", formatElapsed(-1))
        assertEquals("0:00", formatElapsed(Int.MIN_VALUE))
    }

    @Test
    fun `clock time is formatted in the requested zone`() {
        val zone = ZoneId.of("Europe/Madrid")
        val epochMillis = ZonedDateTime.of(2026, 5, 9, 14, 5, 0, 0, zone)
            .toInstant()
            .toEpochMilli()

        assertEquals("14:05", formatClockTime(epochMillis, zone))
        assertEquals("12:05", formatClockTime(epochMillis, ZoneId.of("UTC")))
    }

    @Test
    fun `session date contains day month and time`() {
        val zone = ZoneId.of("UTC")
        val epochMillis = ZonedDateTime.of(2026, 5, 9, 14, 5, 0, 0, zone)
            .toInstant()
            .toEpochMilli()

        assertEquals("9 May · 14:05", formatSessionDate(epochMillis, zone))
    }

    @Test
    fun `history day labels today and yesterday`() {
        val zone = ZoneId.of("UTC")
        val today = LocalDate.now(zone).atTime(12, 0).atZone(zone)
        val yesterday = today.minusDays(1)

        assertEquals("Hoy", formatHistoryDay(today.toInstant().toEpochMilli(), zone))
        assertEquals("Ayer", formatHistoryDay(yesterday.toInstant().toEpochMilli(), zone))
    }

    @Test
    fun `older history day uses a short date`() {
        val zone = ZoneId.of("UTC")
        val older = LocalDateTime.of(2020, 1, 2, 18, 30).atZone(zone)

        assertEquals("2 Jan", formatHistoryDay(older.toInstant().toEpochMilli(), zone))
    }

    @Test
    fun `set ratio always exposes completed and planned counts`() {
        assertEquals("0/3", formatSetRatio(0, 3))
        assertEquals("3/3", formatSetRatio(3, 3))
    }
}
