package com.jujodevs.cursotestingandroid.productlist.data.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.jujodevs.cursotestingandroid.core.mockwebserver.MockWebServerUrlHolder
import com.jujodevs.cursotestingandroid.core.mockwebserver.rules.MockWebServerRule
import com.jujodevs.cursotestingandroid.productlist.domain.repository.ProductRepository
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ProductRepositoryImplTest {

    @get:Rule(order = 0) val mockWebServer = MockWebServerRule()
    @get:Rule(order = 1) val hiltRule = HiltAndroidRule(this)

    @Inject lateinit var productRepository: ProductRepository

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @After
    fun tearDown() {
        MockWebServerUrlHolder.baseUrl = ""
    }

    private val productsJson = """
        {"products":[
            {"id":"p1","name":"Bread","description":"Fresh bread","priceCents":150,"category":"Food","stock":10},
            {"id":"p2","name":"Milk","description":"Full milk","priceCents":200,"category":"Dairy","stock":20}
        ]}
    """.trimIndent()

    @Test
    fun givenValidProductsJson_whenRefreshIsCalled_theDatabaseEmitProductsFromRoom() = runTest {
        mockWebServer.server.enqueue(MockResponse().setBody(productsJson).setResponseCode(200))

        productRepository.refreshProducts()

        val products = productRepository.getProducts().first()
        assertTrue(products.isNotEmpty())
        assertTrue(products.size == 2)
        assertTrue(products.find { it.id == "p1" }?.name == "Bread")
        assertTrue(products.find { it.id == "p2" }?.name == "Milk")
    }

    @Test
    fun givenEmptyProductsJson_whenRefreshIsCalled_thenGetProductsEmitsEmptyList() = runTest {
        mockWebServer.server.enqueue(MockResponse().setBody("""{"products":[]}""").setResponseCode(200))

        productRepository.refreshProducts()

        val products = productRepository.getProducts().first()
        assertTrue(products.isEmpty())
    }

    @Test
    fun givenProductsJson_whenRefreshAndGetProductById_theReturnCorrectProduct() = runTest {
        mockWebServer.server.enqueue(MockResponse().setBody(productsJson).setResponseCode(200))

        productRepository.refreshProducts()

        val product = productRepository.getProductById("p1").first()
        assertNotNull(product)
        assertEquals("Bread", product?.name)
    }

    @Test(expected = Exception::class)
    fun givenServerReturns500_whenRefreshIsCalled_thenInThrowException() = runTest {
        mockWebServer.server.enqueue(MockResponse().setResponseCode(500))

        productRepository.refreshProducts()
    }

    @Test
    fun givenCachedProducts_whenRefreshWithNewProducts_thenFlowEmitsUpdatedData() = runTest {
        val productsJsonUpdated = """
            {"products":[
                {"id":"p1","name":"Integral Bread","description":"Fresh bread","priceCents":450,"category":"Food","stock":10}
            ]}
        """.trimIndent()
        mockWebServer.server.enqueue(MockResponse().setBody(productsJson).setResponseCode(200))
        productRepository.refreshProducts()
        mockWebServer.server.enqueue(MockResponse().setBody(productsJsonUpdated).setResponseCode(200))

        productRepository.refreshProducts()

        val products = productRepository.getProducts().first()
        assertTrue(products.isNotEmpty())
        assertTrue(products.size == 1)
        assertTrue(products.find { it.id == "p1" }?.name == "Integral Bread")
        assertTrue(products.find { it.id == "p1" }?.price == 4.5)
    }

    @Test
    fun givenProductsEndpoint_whenRefreshIsCalled_thenRequestIsGetCorrectPath() = runTest {
        mockWebServer.server.enqueue(MockResponse().setBody(productsJson).setResponseCode(200))
        productRepository.refreshProducts()

        val request = mockWebServer.server.takeRequest()

        assertEquals("GET", request.method)
        assertTrue(request.path?.contains("data/products.json") == true)
    }
}