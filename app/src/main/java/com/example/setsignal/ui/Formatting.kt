package com.example.setsignal.ui

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

fun formatElapsed(totalSeconds: Int): String {
    val safe = totalSeconds.coerceAtLeast(0)
    val hours = safe / 3600
    val minutes = (safe % 3600) / 60
    val seconds = safe % 60
    return if (hours > 0) {
        "%d:%02d:%02d".format(Locale.US, hours, minutes, seconds)
    } else {
        "%d:%02d".format(Locale.US, minutes, seconds)
    }
}

fun formatClockTime(epochMillis: Long, zoneId: ZoneId = ZoneId.systemDefault()): String =
    Instant.ofEpochMilli(epochMillis)
        .atZone(zoneId)
        .format(DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault()))

fun formatSessionDate(epochMillis: Long, zoneId: ZoneId = ZoneId.systemDefault()): String =
    Instant.ofEpochMilli(epochMillis)
        .atZone(zoneId)
        .format(DateTimeFormatter.ofPattern("d MMM · HH:mm", Locale.getDefault()))

fun formatHistoryDay(epochMillis: Long, zoneId: ZoneId = ZoneId.systemDefault()): String {
    val date = Instant.ofEpochMilli(epochMillis).atZone(zoneId).toLocalDate()
    val today = LocalDate.now(zoneId)
    return when (date) {
        today -> "Hoy"
        today.minusDays(1) -> "Ayer"
        else -> date.format(DateTimeFormatter.ofPattern("d MMM", Locale.getDefault()))
    }
}

fun formatSetRatio(completed: Int, planned: Int): String = "$completed/$planned"
