package com.jujodevs.cursotestingandroid.core.fakes

import com.jujodevs.cursotestingandroid.core.domain.time.Clock
import java.time.Instant

class FakeClock(): Clock {

    private var currentTime: Instant = Instant.now()

    fun setTime(time: Instant) {
        currentTime = time
    }

    fun advanceTime(seconds: Long) {
        currentTime = currentTime.plusSeconds(seconds)
    }

    override fun now(): Instant = currentTime
}