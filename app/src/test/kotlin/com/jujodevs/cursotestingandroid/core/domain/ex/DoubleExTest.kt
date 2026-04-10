package com.jujodevs.cursotestingandroid.core.domain.ex

import org.junit.Assert.assertEquals
import org.junit.Test

class DoubleExTest {

    @Test
    fun `given double when round to 2 decimals then rounds up correctly`() {
        val double = 4.6578
        val expectedDouble = 4.66

        val result = double.roundTo2Decimals()

        assertEquals(expectedDouble, result, 0.0)
    }

    @Test
    fun `given double when round to 2 decimals then rounds down correctly`() {
        val double = 1.0148
        val expectedDouble = 1.01

        val result = double.roundTo2Decimals()

        assertEquals(expectedDouble, result, 0.0)
    }
}