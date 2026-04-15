package com.jujodevs.cursotestingandroid.core.data.time

import com.jujodevs.cursotestingandroid.core.domain.time.Clock
import java.time.Instant
import javax.inject.Inject

class SystemClock @Inject constructor(): Clock {
    override fun now(): Instant = Instant.now()
}