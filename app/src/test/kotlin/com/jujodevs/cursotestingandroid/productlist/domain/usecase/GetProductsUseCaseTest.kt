package com.jujodevs.cursotestingandroid.productlist.domain.usecase

import com.jujodevs.cursotestingandroid.core.builders.product
import com.jujodevs.cursotestingandroid.core.builders.promotion
import com.jujodevs.cursotestingandroid.core.fakes.FakeClock
import com.jujodevs.cursotestingandroid.core.fakes.FakeProductRepository
import com.jujodevs.cursotestingandroid.core.fakes.FakePromotionRepository
import com.jujodevs.cursotestingandroid.core.fakes.FakeSettingsRepository
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant

class GetProductsUseCaseTest {
    lateinit var fakeProductRepository: FakeProductRepository
    lateinit var fakePromotionRepository: FakePromotionRepository
    lateinit var fakeSettingsRepository: FakeSettingsRepository
    lateinit var getPromotionForProduct: GetPromotionForProduct
    lateinit var groupPromotionsByProductId: GroupPromotionsByProductId
    lateinit var fakeClock: FakeClock

    lateinit var useCase: GetProductsUseCase

    @Before
    fun setUp() {
        fakeProductRepository = FakeProductRepository()
        fakePromotionRepository = FakePromotionRepository()
        fakeSettingsRepository = FakeSettingsRepository()
        getPromotionForProduct = GetPromotionForProduct()
        groupPromotionsByProductId = GroupPromotionsByProductId()
        fakeClock = FakeClock()
        useCase =
            GetProductsUseCase(
                productRepository = fakeProductRepository,
                promotionRepository = fakePromotionRepository,
                getPromotionForProduct = getPromotionForProduct,
                groupPromotionsByProductId = groupPromotionsByProductId,
                settingsRepository = fakeSettingsRepository,
                clock = fakeClock,
            )
    }

    @Test
    fun `GIVEN promotion ending WHEN invoke THEN it should be included`() =
        runTest {
            val now = Instant.parse("2026-04-03T10:00:00Z")
            val productId = "product-Id"
            val product =
                product {
                    withId(productId)
                }
            val promo =
                promotion {
                    withProductsIds(listOf(productId))
                    withStartTime(now.minusSeconds(60))
                    withEndTime(now)
                }
            fakeClock.setTime(now)
            fakeProductRepository.setProducts(listOf(product))
            fakePromotionRepository.setPromotions(listOf(promo))

            val result = useCase().firstOrNull()

            assertNotNull(result?.firstOrNull()?.promotion)
        }

    @Test
    fun `GIVEN active promotion WHEN time advances THEN it should no be longer be returned`() =
        runTest {
            val now = Instant.parse("2026-04-03T10:00:00Z")
            val productId = "product-Id"
            val product =
                product {
                    withId(productId)
                }
            val promo =
                promotion {
                    withProductsIds(listOf(productId))
                    withStartTime(now)
                    withEndTime(now.plusSeconds(5))
                }
            fakeClock.setTime(now)
            fakeProductRepository.setProducts(listOf(product))
            fakePromotionRepository.setPromotions(listOf(promo))

            val firstResult = useCase().firstOrNull()
            fakeClock.advanceTime(6)
            val secondResult = useCase().firstOrNull()

            assertNotNull(firstResult?.firstOrNull()?.promotion)
            assertTrue(secondResult?.firstOrNull()?.promotion == null)
        }

    @Test
    fun `GIVEN inStockOnly enabled WHEN product goes out of stock THEN it should be filtered`() =
        runTest {
            val productId = "product-Id"
            val product =
                product {
                    withId(productId)
                    withStock(0)
                }
            fakeSettingsRepository.setInStockOnly(true)
            fakeProductRepository.setProducts(listOf(product))

            val result = useCase().firstOrNull()

            assertTrue(result?.isEmpty() == true)
        }
}
