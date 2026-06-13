package com.jujodevs.cursotestingandroid.detail.domain.usecase

import com.jujodevs.cursotestingandroid.core.builders.product
import com.jujodevs.cursotestingandroid.core.builders.promotion
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
import org.junit.Before
import org.junit.Test

class GetProductDetailWithPromotionUseCaseTest {
    private lateinit var productRepository: FakeProductRepository
    private lateinit var promotionRepository: FakePromotionRepository
    private lateinit var groupPromotionsByProductId: GroupPromotionsByProductId
    private lateinit var getPromotionForProduct: GetPromotionForProduct
    private lateinit var fakeClock: FakeClock

    private lateinit var useCase: GetProductDetailWithPromotionUseCase

    @Before
    fun setUp() {
        productRepository = FakeProductRepository()
        promotionRepository = FakePromotionRepository()
        groupPromotionsByProductId = GroupPromotionsByProductId()
        getPromotionForProduct = GetPromotionForProduct()
        fakeClock = FakeClock()

        useCase =
            GetProductDetailWithPromotionUseCase(
                productRepository = productRepository,
                promotionRepository = promotionRepository,
                groupPromotionsByProductId = groupPromotionsByProductId,
                getPromotionForProduct = getPromotionForProduct,
                clock = fakeClock,
            )
    }

    @Test
    fun `GIVEN non existing product WHEN invokes THEN returns null`() =
        runTest {
            val productId = "non-existing"
            productRepository.setProducts(emptyList())

            val result = useCase(productId).first()

            assertNull(result)
        }

    @Test
    fun `GIVEN existing product with active promotion WHEN invoke THEN returns product with promotion`() =
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
            productRepository.setProducts(listOf(product))
            promotionRepository.setPromotions(listOf(promo))

            val result = useCase(productId).first()

            assertNotNull(result)
            assertEquals(product, result?.product)
            assertNotNull(result?.promotion)
        }

    @Test
    fun `GIVEN existing product without promotion WHEN invoke THEN returns product without promotion`() =
        runTest {
            val productId = "productID"
            val product =
                product {
                    withId(productId)
                }
            productRepository.setProducts(listOf(product))
            promotionRepository.setPromotions(emptyList())

            val result = useCase(productId).first()

            assertNotNull(result)
            assertEquals(product, result?.product)
            assertNull(result?.promotion)
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
            productRepository.setProducts(listOf(product))
            promotionRepository.setPromotions(listOf(endingPromotion))

            val result = useCase(productId).first()

            assertNotNull(result?.promotion)
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
            productRepository.setProducts(listOf(product))
            promotionRepository.setPromotions(listOf(endingPromotion))
            val myUseCaseFlow = useCase(productId)

            val firstResult = myUseCaseFlow.first()
            fakeClock.advanceTime(10)
            val secondResult = myUseCaseFlow.first()

            assertNotNull(firstResult?.promotion)
            assertNull(secondResult?.promotion)
        }
}
