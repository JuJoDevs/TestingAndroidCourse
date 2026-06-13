package com.jujodevs.cursotestingandroid.cart.domain.usecase

import com.jujodevs.cursotestingandroid.core.builders.cartItem
import com.jujodevs.cursotestingandroid.core.builders.product
import com.jujodevs.cursotestingandroid.core.builders.promotion
import com.jujodevs.cursotestingandroid.core.fakes.FakeCartRepository
import com.jujodevs.cursotestingandroid.core.fakes.FakeClock
import com.jujodevs.cursotestingandroid.core.fakes.FakeProductRepository
import com.jujodevs.cursotestingandroid.core.fakes.FakePromotionRepository
import com.jujodevs.cursotestingandroid.productlist.domain.usecase.GetPromotionForProduct
import com.jujodevs.cursotestingandroid.productlist.domain.usecase.GroupPromotionsByProductId
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetCartItemWithPromotionUseCaseTest {
    lateinit var cartRepository: FakeCartRepository
    lateinit var productRepository: FakeProductRepository
    lateinit var promotionRepository: FakePromotionRepository
    lateinit var groupPromotionsByProductId: GroupPromotionsByProductId
    lateinit var getPromotionForProduct: GetPromotionForProduct
    lateinit var fakeClock: FakeClock

    lateinit var useCase: GetCartItemWithPromotionUseCase

    @Before
    fun setUp() {
        cartRepository = FakeCartRepository()
        productRepository = FakeProductRepository()
        promotionRepository = FakePromotionRepository()
        groupPromotionsByProductId = GroupPromotionsByProductId()
        getPromotionForProduct = GetPromotionForProduct()
        fakeClock = FakeClock()

        useCase =
            GetCartItemWithPromotionUseCase(
                cartRepository = cartRepository,
                productRepository = productRepository,
                promotionRepository = promotionRepository,
                groupPromotionsByProductId = groupPromotionsByProductId,
                getPromotionForProduct = getPromotionForProduct,
                clock = fakeClock,
            )
    }

    @Test
    fun `GIVEN empty cart WHEN invokes THEN returns empty list`() =
        runTest {
            cartRepository.setCartItems(emptyList())

            val result = useCase().first()

            assertTrue(result.isEmpty())
        }

    @Test
    fun `GIVEN existing cart item with active promotion when invoke THEN returns item with promotion`() =
        runTest {
            val productId = "productID"
            val product =
                product {
                    withId(productId)
                }
            val now = fakeClock.now()
            val promo =
                promotion {
                    withProductsIds(listOf(productId))
                    withStartTime(now.minusSeconds(10))
                    withEndTime(now.plusSeconds(10))
                }
            val cartItem =
                cartItem {
                    withProductId(productId)
                    withQuantity(2)
                }
            cartRepository.setCartItems(listOf(cartItem))
            productRepository.setProducts(listOf(product))
            promotionRepository.setPromotions(listOf(promo))

            val result = useCase().first()

            assertEquals(1, result.size)
            assertNotNull(result.first().item.promotion)
        }

    @Test
    fun `GIVEN cart item without matching product when invoke WHEN invoke THEN skip item`() =
        runTest {
            cartRepository.setCartItems(listOf(cartItem { withProductId("ghost-id") }))
            productRepository.setProducts(listOf(product { withId("other-id") }))

            val result = useCase().first()

            assertTrue(result.isEmpty())
        }

    @Test
    fun `GIVEN promotion ending exactly now WHEN invoke THEN it must be include`() =
        runTest {
            val now = fakeClock.now()
            val productId = "productID"
            val product =
                product {
                    withId(productId)
                }
            val endingPromotion =
                promotion {
                    withProductsIds(listOf(productId))
                    withStartTime(now.minusSeconds(100))
                    withEndTime(now)
                }
            cartRepository.setCartItems(listOf(cartItem { withProductId(productId) }))
            productRepository.setProducts(listOf(product))
            promotionRepository.setPromotions(listOf(endingPromotion))

            val result = useCase().first()

            assertNotNull(result.first().item.promotion)
        }

    @Test
    fun `GIVEN expired promotion WHEN invoke THEN items remains but without promotion`() =
        runTest {
            val now = fakeClock.now()
            val productId = "productID"
            val product =
                product {
                    withId(productId)
                }
            val endingPromotion =
                promotion {
                    withProductsIds(listOf(productId))
                    withStartTime(now.minusSeconds(100))
                    withEndTime(now.minusSeconds(1))
                }
            cartRepository.setCartItems(listOf(cartItem { withProductId(productId) }))
            productRepository.setProducts(listOf(product))
            promotionRepository.setPromotions(listOf(endingPromotion))

            val result = useCase().first()

            assertEquals(1, result.size)
            assertNull(result.first().item.promotion)
        }

    @Test
    fun `GIVEN active promotion WHEN time advances THEN flow update list without promotion`() =
        runTest {
            val now = fakeClock.now()
            val productId = "productID"
            val product =
                product {
                    withId(productId)
                }
            val endingPromotion =
                promotion {
                    withProductsIds(listOf(productId))
                    withStartTime(now.minusSeconds(100))
                    withEndTime(now.plusSeconds(5))
                }
            cartRepository.setCartItems(listOf(cartItem { withProductId(productId) }))
            productRepository.setProducts(listOf(product))
            promotionRepository.setPromotions(listOf(endingPromotion))
            val myUseCase = useCase()

            val firstResult = myUseCase.first()
            fakeClock.advanceTime(100)
            val secondResult = myUseCase.first()

            assertNotNull(firstResult.first().item.promotion)
            assertNull(secondResult.first().item.promotion)
        }
}
