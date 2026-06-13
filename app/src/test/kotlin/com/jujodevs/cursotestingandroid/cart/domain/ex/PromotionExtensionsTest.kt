package com.jujodevs.cursotestingandroid.cart.domain.ex

import com.jujodevs.cursotestingandroid.core.builders.promotion
import com.jujodevs.cursotestingandroid.productlist.domain.model.Promotion
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant

class PromotionExtensionsTest {
    private val now = Instant.parse("2026-04-03T10:00:00Z")

    @Test
    fun `GIVEN future promotion WHEN active at THEN exclude`() {
        val futurePromotion =
            promotion {
                withStartTime(now.plusSeconds(10))
                withEndTime(now.plusSeconds(100))
            }
        val promotions = listOf(futurePromotion)

        val result = promotions.activeAt(now)

        assertEquals(0, result.size)
    }

    @Test
    fun `GIVEN expired promotion WHEN active at THEN exclude`() {
        val expiredPromotion =
            promotion {
                withStartTime(now.minusSeconds(100))
                withEndTime(now.minusSeconds(10))
            }
        val promotions = listOf(expiredPromotion)

        val result = promotions.activeAt(now)

        assertEquals(0, result.size)
    }

    @Test
    fun `GIVEN on going promotion WHEN active at THEN include`() {
        val onGoingPromotion =
            promotion {
                withStartTime(now.minusSeconds(1))
                withEndTime(now.plusSeconds(1))
            }
        val promotions = listOf(onGoingPromotion)

        val result = promotions.activeAt(now)

        assertEquals(1, result.size)
    }

    @Test
    fun `GIVEN exact start time promotion WHEN active at THEN include`() {
        val onGoingPromotion =
            promotion {
                withStartTime(now)
                withEndTime(now.plusSeconds(100))
            }
        val promotions = listOf(onGoingPromotion)

        val result = promotions.activeAt(now)

        assertEquals(1, result.size)
    }

    @Test
    fun `GIVEN exact end time promotion WHEN active at THEN include`() {
        val onGoingPromotion =
            promotion {
                withStartTime(now.minusSeconds(100))
                withEndTime(now)
            }
        val promotions = listOf(onGoingPromotion)

        val result = promotions.activeAt(now)

        assertEquals(1, result.size)
    }

    @Test
    fun `GIVEN empty list WHEN active at THEN return empty`() {
        val promotions = listOf<Promotion>()

        val result = promotions.activeAt(now)

        assertEquals(0, result.size)
    }
}
