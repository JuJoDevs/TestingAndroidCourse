package com.jujodevs.cursotestingandroid.core.domain.time

import java.time.Instant

interface Clock {
    fun now(): Instant
}
