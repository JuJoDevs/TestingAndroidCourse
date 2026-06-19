package com.jujodevs.cursotestingandroid.core

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.jujodevs.cursotestingandroid.core.data.local.database.MiniMarketDatabase
import com.jujodevs.cursotestingandroid.core.domain.model.AppError
import com.jujodevs.cursotestingandroid.core.mockwebserver.MiniMarketApiDispatcher
import com.jujodevs.cursotestingandroid.core.mockwebserver.ProductErrorDispatcher
import com.jujodevs.cursotestingandroid.core.mockwebserver.rules.MockWebServerRule
import com.jujodevs.cursotestingandroid.core.utils.asAsset
import com.jujodevs.cursotestingandroid.productlist.domain.repository.ProductRepository
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject
import kotlin.test.assertFailsWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class OfflineFirstIntegrationTest {
    private companion object {
        private const val DEFAULT_PRODUCT_ASSET = "product_list_default.json"
        private const val UPDATED_PRODUCT_ASSET = "product_list_updated.json"
        private const val DEFAULT_SIZE = 3
        private const val UPDATED_SIZE = 1
    }

    @get:Rule(order = 0)
    val mockWebServer = MockWebServerRule()

    @get:Rule(order = 1)
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var db: MiniMarketDatabase

    @Inject
    lateinit var productRepository: ProductRepository

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun givenSuccessfulRefresh_whenGetProducts_thenRoomContainsRemoteProducts() =
        runTest {
            serverProductsFromAsset(DEFAULT_PRODUCT_ASSET)
            productRepository.refreshProducts()

            val cachedProducts = productRepository.getProducts().first { it.size == DEFAULT_SIZE }

            assertEquals(DEFAULT_SIZE, cachedProducts.size)
        }

    @Test
    fun givenEmptyCacheAndFailedRefresh_whenGetProducts_theEmitsEmptyList() =
        runTest {
            serveProductsError()

            assertFailsWith<AppError.NetworkError> { productRepository.refreshProducts() }

            val products = productRepository.getProducts().first { it.isEmpty() }
            assertTrue(products.isEmpty())
        }

    @Test
    fun givenCachedProductsAndFailedRefresh_whenGetProducts_thenReturnsPreviousCache() =
        runTest {
            serverProductsFromAsset(DEFAULT_PRODUCT_ASSET)
            productRepository.refreshProducts()
            productRepository.getProducts().first { it.size == DEFAULT_SIZE }
            serveProductsError()

            assertFailsWith<AppError.NetworkError> { productRepository.refreshProducts() }

            val cachedProducts = productRepository.getProducts().first { it.size == DEFAULT_SIZE }

            assertEquals(DEFAULT_SIZE, cachedProducts.size)
        }

    @Test
    fun givenCachedProducts_whenRefreshWithNewPayload_thenContainsOnlyLatestProducts() =
        runTest {
            serverProductsFromAsset(DEFAULT_PRODUCT_ASSET)
            productRepository.refreshProducts()
            productRepository.getProducts().first { it.size == DEFAULT_SIZE }
            serverProductsFromAsset(UPDATED_PRODUCT_ASSET)
            productRepository.refreshProducts()

            val updatedProducts = productRepository.getProducts().first { it.size == UPDATED_SIZE }

            assertEquals(UPDATED_SIZE, updatedProducts.size)
            assertEquals("updated-p1", updatedProducts.first().id)
            assertEquals("Pan integral", updatedProducts.first().name)
        }

    private fun serverProductsFromAsset(assetName: String) {
        mockWebServer.server.dispatcher =
            MiniMarketApiDispatcher(
                productJson = assetName.asAsset(),
            )
    }

    private fun serveProductsError() {
        mockWebServer.server.dispatcher = ProductErrorDispatcher()
    }
}
