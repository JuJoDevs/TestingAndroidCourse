package com.jujodevs.cursotestingandroid.cart.domain.usecase

import com.jujodevs.cursotestingandroid.core.builders.cartItem
import com.jujodevs.cursotestingandroid.core.builders.product
import com.jujodevs.cursotestingandroid.core.builders.promotion
import com.jujodevs.cursotestingandroid.core.fakes.FakeCartRepository
import com.jujodevs.cursotestingandroid.core.fakes.FakeClock
import com.jujodevs.cursotestingandroid.core.fakes.FakeProductRepository
import com.jujodevs.cursotestingandroid.core.fakes.FakePromotionRepository
import com.jujodevs.cursotestingandroid.productlist.domain.model.PromotionType
import com.jujodevs.cursotestingandroid.productlist.domain.usecase.GetPromotionForProduct
import com.jujodevs.cursotestingandroid.productlist.domain.usecase.GroupPromotionsByProductId
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetCartSummaryUseCaseTest {
    lateinit var cartRepository: FakeCartRepository
    lateinit var productRepository: FakeProductRepository
    lateinit var promotionRepository: FakePromotionRepository
    lateinit var groupPromotionsByProductId: GroupPromotionsByProductId
    lateinit var getPromotionForProduct: GetPromotionForProduct
    lateinit var clock: FakeClock

    lateinit var useCase: GetCartSummaryUseCase

    @Before
    fun setUp() {
        cartRepository = FakeCartRepository()
        productRepository = FakeProductRepository()
        promotionRepository = FakePromotionRepository()
        groupPromotionsByProductId = GroupPromotionsByProductId()
        getPromotionForProduct = GetPromotionForProduct()
        clock = FakeClock()

        useCase = GetCartSummaryUseCase(
            cartRepository = cartRepository,
            productRepository = productRepository,
            promotionRepository = promotionRepository,
            groupPromotionsByProductId = groupPromotionsByProductId,
            getPromotionForProduct = getPromotionForProduct,
            clock = clock
        )
    }

    @Test
    fun `GIVEN percent promotion WHEN invoke THEN calculate correctly`() = runTest {
        val productId = "p1"
        val product = product { withId(productId).withPrice(100.0) }
        val promo = promotion {
            withProductsIds(listOf(productId))
            withType(PromotionType.PERCENT)
            withValue(10.0)
            withStartTime(clock.now().minusSeconds(10))
            withEndTime(clock.now().plusSeconds(10))
        }
        val cartItem = cartItem { withProductId(productId).withQuantity(2) }
        productRepository.setProducts(listOf(product))
        promotionRepository.setPromotions(listOf(promo))
        cartRepository.setCartItems(listOf(cartItem))

        val summary = useCase().first()

        assertEquals(180.0, summary.finalTotal, 0.0)
        assertEquals(20.0, summary.discountTotal, 0.0)
        assertEquals(200.0, summary.subtotal, 0.0)
    }

    @Test
    fun `GIVEN 3 items in 2x1 promotion WHEN invoke THEN only discounts 1 unit`() = runTest {
        val productId = "p1"
        val product = product { withId(productId).withPrice(100.0) }
        val promo = promotion {
            withProductsIds(listOf(productId))
            withType(PromotionType.BUY_X_PAY_Y)
            withBuyQuantity(2)
            withValue(1.0)
            withStartTime(clock.now().minusSeconds(10))
            withEndTime(clock.now().plusSeconds(10))
        }
        val cartItem = cartItem { withProductId(productId).withQuantity(3) }
        productRepository.setProducts(listOf(product))
        promotionRepository.setPromotions(listOf(promo))
        cartRepository.setCartItems(listOf(cartItem))

        val summary = useCase().first()

        assertEquals(200.0, summary.finalTotal, 0.0)
        assertEquals(100.0, summary.discountTotal, 0.0)
        assertEquals(300.0, summary.subtotal, 0.0)
    }

    @Test
    fun `GIVEN multiple products with different promotions WHEN invoke THEN sums all correctly`() = runTest {
        val now = clock.now()
        val p1 = product { withId("p1").withPrice(100.0) }
        val p2 = product { withId("p2").withPrice(50.0) }
        val promoPercent = promotion {
            withProductsIds(listOf("p1")).withType(PromotionType.PERCENT).withValue(10.0)
            withStartTime(now.minusSeconds(10))
            withEndTime(now.plusSeconds(10))
        }
        val cart = listOf(
            cartItem { withProductId("p1").withQuantity(1) },
            cartItem { withProductId("p2").withQuantity(1) },
        )
        productRepository.setProducts(listOf(p1, p2))
        promotionRepository.setPromotions(listOf(promoPercent))
        cartRepository.setCartItems(cart)

        val summary = useCase().first()

        assertEquals(140.0, summary.finalTotal, 0.0)
        assertEquals(10.0, summary.discountTotal, 0.0)
        assertEquals(150.0, summary.subtotal, 0.0)
    }

    @Test
    fun `GIVEN expired promotion WHEN invoke THEN discount is zero`() = runTest {
        val now = clock.now()
        val productId = "p1"
        val product = product { withId(productId).withPrice(100.0) }
        val promo = promotion {
            withProductsIds(listOf(productId))
            withType(PromotionType.PERCENT)
            withValue(10.0)
            withStartTime(now.minusSeconds(10))
            withEndTime(now.minusSeconds(5))
        }
        val cartItem = cartItem { withProductId(productId).withQuantity(1) }
        productRepository.setProducts(listOf(product))
        promotionRepository.setPromotions(listOf(promo))
        cartRepository.setCartItems(listOf(cartItem))

        val summary = useCase().first()

        assertEquals(100.0, summary.finalTotal, 0.0)
        assertEquals(0.0, summary.discountTotal, 0.0)
        assertEquals(100.0, summary.subtotal, 0.0)
    }

    @Test
    fun `GIVEN active promotion WHEN time advance WHEN summary update automatically`() = runTest {
        val now = clock.now()
        val productId = "p1"
        val product = product { withId(productId).withPrice(100.0) }
        val promo = promotion {
            withProductsIds(listOf(productId))
            withType(PromotionType.PERCENT)
            withValue(10.0)
            withStartTime(now.minusSeconds(10))
            withEndTime(now.plusSeconds(5))
        }
        val cartItem = cartItem { withProductId(productId).withQuantity(1) }
        productRepository.setProducts(listOf(product))
        promotionRepository.setPromotions(listOf(promo))
        cartRepository.setCartItems(listOf(cartItem))
        val summaryFlow = useCase()

        val firstSummary = summaryFlow.first()
        clock.advanceTime(10)
        val secondSummary = summaryFlow.first()

        assertEquals(90.0, firstSummary.finalTotal, 0.0)
        assertEquals(10.0, firstSummary.discountTotal, 0.0)
        assertEquals(100.0, firstSummary.subtotal, 0.0)
        assertEquals(100.0, secondSummary.finalTotal, 0.0)
        assertEquals(0.0, secondSummary.discountTotal, 0.0)
        assertEquals(100.0, secondSummary.subtotal, 0.0)
    }
}