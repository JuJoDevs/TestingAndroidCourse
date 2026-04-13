package com.jujodevs.cursotestingandroid.productlist.domain.usecase

import com.jujodevs.cursotestingandroid.core.builders.product
import com.jujodevs.cursotestingandroid.core.builders.promotion
import com.jujodevs.cursotestingandroid.productlist.domain.model.ProductPromotion
import com.jujodevs.cursotestingandroid.productlist.domain.model.PromotionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GetPromotionForProductTest {

    private val useCase = GetPromotionForProduct()

    @Test
    fun given_no_promotions_when_invoke_then_returns_null() {
        // Given
        val product = product()

        // When
        val result = useCase.invoke(product, emptyMap())

        // Then
        assertNull(result)
    }

    @Test
    fun give_percent_promotion_when_invoke_then_returns_discounted_price_rounded_to_2_decimals() {
        // Given
        val productId = "product-id"
        val product = product {
            withId(productId)
            withPrice(10.0)
        }
        val promotion = promotion {
            withProductsIds(listOf(productId))
            withType(PromotionType.PERCENT)
            withValue(15.0)
        }

        // When
        val response = useCase(product, mapOf(productId to listOf(promotion)))

        // Then
        assertTrue(response is ProductPromotion.Percent)
        response as ProductPromotion.Percent
        assertEquals(8.50, response.discountedPrice, 0.0)
        assertEquals(15.0, response.percent, 0.0)
    }

    @Test
    fun given_buy_x_pay_y_and_percent_promotions_when_invoke_then_prioritizes_buy_x_pay_y() {
        // Given
        val productId = "product-id"
        val product = product {
            withId(productId)
            withPrice(10.0)
        }
        val promotionPercent = promotion {
            withProductsIds(listOf(productId))
            withType(PromotionType.PERCENT)
            withValue(15.0)
        }
        val promotionBuyXPayY = promotion {
            withProductsIds(listOf(productId))
            withType(PromotionType.BUY_X_PAY_Y)
            withValue(2.0)
            withBuyQuantity(3)
        }

        // When
        val response = useCase(product, mapOf(productId to listOf(promotionPercent, promotionBuyXPayY)))

        // Then
        assertTrue(response is ProductPromotion.BuyXPayY)
        response as ProductPromotion.BuyXPayY
        assertEquals(3, response.buy)
        assertEquals(2, response.pay)
        assertEquals("3x2", response.label)
    }

    @Test
    fun given_various_buy_x_pay_y_when_invoke_then_prioritizes_minor_value() {
        // Given
        val productId = "product-id"
        val product = product {
            withId(productId)
            withPrice(10.0)
        }
        val promotionBuyXPayY1 = promotion {
            withProductsIds(listOf(productId))
            withType(PromotionType.BUY_X_PAY_Y)
            withValue(4.0)
            withBuyQuantity(6)
        }
        val promotionBuyXPayY2 = promotion {
            withProductsIds(listOf(productId))
            withType(PromotionType.BUY_X_PAY_Y)
            withValue(2.0)
            withBuyQuantity(3)
        }

        // When
        val response = useCase(product, mapOf(productId to listOf(promotionBuyXPayY1, promotionBuyXPayY2)))

        // Then
        assertTrue(response is ProductPromotion.BuyXPayY)
        response as ProductPromotion.BuyXPayY
        assertEquals(3, response.buy)
        assertEquals(2, response.pay)
        assertEquals("3x2", response.label)
    }

    @Test
    fun given_multiple_percent_promotions_then_invoke_then_returns_highest_discount() {
        // Given
        val productId = "product-id"
        val product = product {
            withId(productId)
            withPrice(10.0)
        }
        val promotionLow = promotion {
            withProductsIds(listOf(productId))
            withType(PromotionType.PERCENT)
            withValue(5.0)
        }
        val promotionHigh = promotion {
            withProductsIds(listOf(productId))
            withType(PromotionType.PERCENT)
            withValue(50.0)
        }

        // When
        val response = useCase(product, mapOf(productId to listOf(promotionLow, promotionHigh)))

        // Then
        assertTrue(response is ProductPromotion.Percent)
        response as ProductPromotion.Percent
        assertEquals(5.0, response.discountedPrice, 0.0)
        assertEquals(50.0, response.percent, 0.0)
    }

    @Test
    fun given_buy_x_pay_y_without_buy_quantity_when_invoke_then_returns_other_promotion() {
        // Given
        val productId = "product-id"
        val product = product {
            withId(productId)
            withPrice(10.0)
        }
        val promotion = promotion {
            withProductsIds(listOf(productId))
            withType(PromotionType.PERCENT)
            withValue(5.0)
        }
        val brokenBuyXPromotion = promotion {
            withProductsIds(listOf(productId))
            withType(PromotionType.BUY_X_PAY_Y)
            withBuyQuantity(null)
        }

        // When
        val response = useCase(product, mapOf(productId to listOf(promotion, brokenBuyXPromotion)))

        // Then
        assertTrue(response is ProductPromotion.Percent)
        response as ProductPromotion.Percent
        assertEquals(9.5, response.discountedPrice, 0.0)
        assertEquals(5.0, response.percent, 0.0)
    }
}