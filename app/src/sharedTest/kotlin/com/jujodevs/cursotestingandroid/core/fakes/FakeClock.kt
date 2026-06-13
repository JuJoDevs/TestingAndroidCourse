package com.jujodevs.cursotestingandroid.core.fakes

import com.jujodevs.cursotestingandroid.core.domain.time.Clock
import java.time.Instant

class FakeClock : Clock {
    private var currentTime: Instant = Instant.parse("2026-04-03T10:00:00Z")

    fun setTime(time: Instant) {
        currentTime = time
    }

    fun advanceTime(seconds: Long) {
        currentTime = currentTime.plusSeconds(seconds)
    }

    override fun now(): Instant = currentTime
}
