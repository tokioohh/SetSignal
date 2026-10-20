package com.example.setsignal.platform

import android.os.SystemClock

interface TimeSource {
    /** Monotonic time used for countdowns. Keeps running while the device sleeps. */
    fun elapsedRealtimeMillis(): Long

    /** Wall-clock time used for persisted session timestamps. */
    fun epochMillis(): Long
}

object SystemTimeSource : TimeSource {
    override fun elapsedRealtimeMillis(): Long = SystemClock.elapsedRealtime()
    override fun epochMillis(): Long = System.currentTimeMillis()
}
