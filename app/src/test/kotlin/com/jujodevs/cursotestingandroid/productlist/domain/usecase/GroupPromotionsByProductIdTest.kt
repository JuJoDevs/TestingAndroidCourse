package com.jujodevs.cursotestingandroid.productlist.domain.usecase

import com.jujodevs.cursotestingandroid.core.builders.promotion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GroupPromotionsByProductIdTest {
    private lateinit var useCase: GroupPromotionsByProductId

    @Before
    fun setUp() {
        useCase = GroupPromotionsByProductId()
    }

    @Test
    fun `GIVEN empty promotions WHEN invoke THEN returns empty map`() {
        val result = useCase(emptyList())

        assertTrue(result.isEmpty())
    }

    @Test
    fun `GIVEN promotion with single product WHEN invoke THEN returns map with that product id`() {
        val productId = "p1"
        val promo = promotion { withProductsIds(listOf(productId)) }

        val result = useCase(listOf(promo))

        assertEquals(1, result.size)
        assertTrue(result.containsKey(productId))
        assertEquals(listOf(promo), result[productId])
    }

    @Test
    fun `GIVEN promotion with multiple products WHEN invoke THEN returns map with all product ids`() {
        val productIds = listOf("p1", "p2", "p3")
        val promo = promotion { withProductsIds(productIds) }

        val result = useCase(listOf(promo))

        assertEquals(3, result.size)
        productIds.forEach { id ->
            assertEquals(listOf(promo), result[id])
        }
    }

    @Test
    fun `GIVEN multiple promotions for same product WHEN invoke THEN returns map with all promotions for that product`() {
        val productId = "p1"
        val promo1 =
            promotion {
                withId("promo1")
                withProductsIds(listOf(productId))
            }
        val promo2 =
            promotion {
                withId("promo2")
                withProductsIds(listOf(productId))
            }

        val result = useCase(listOf(promo1, promo2))

        assertEquals(1, result.size)
        assertEquals(listOf(promo1, promo2), result[productId])
    }

    @Test
    fun `GIVEN promotions with overlapping products WHEN invoke THEN groups correctly`() {
        val p1 = "p1"
        val p2 = "p2"
        val promo1 =
            promotion {
                withId("promo1")
                withProductsIds(listOf(p1, p2))
            }
        val promo2 =
            promotion {
                withId("promo2")
                withProductsIds(listOf(p2))
            }

        val result = useCase(listOf(promo1, promo2))

        assertEquals(2, result.size)
        assertEquals(listOf(promo1), result[p1])
        assertEquals(listOf(promo1, promo2), result[p2])
    }
}
