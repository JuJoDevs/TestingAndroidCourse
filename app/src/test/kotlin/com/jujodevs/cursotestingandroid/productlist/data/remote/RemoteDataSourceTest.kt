package com.jujodevs.cursotestingandroid.productlist.data.remote

import com.jujodevs.cursotestingandroid.core.domain.model.AppError
import com.jujodevs.cursotestingandroid.di.NetworkModule
import com.jujodevs.cursotestingandroid.productlist.data.remote.response.ProductResponse
import com.jujodevs.cursotestingandroid.productlist.data.remote.response.ProductsResponse
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RemoteDataSourceTest {
    private val server = MockWebServer()
    private lateinit var json: Json
    private lateinit var remoteDataSource: RemoteDataSource

    @Before
    fun setUp() {
        server.start()
        json = NetworkModule.provideJson()
        val baseUrl = server.url("/").toString()
        val retrofit =
            NetworkModule.providesRetrofit(
                okHttpClient = OkHttpClient(),
                json = json,
                baseUrl = baseUrl,
            )
        val api = NetworkModule.provideMiniMarketApiService(retrofit)

        remoteDataSource = RemoteDataSource(api)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `GIVEN empty json response WHEN getProducts THEN returns empty list`() =
        runTest {
            server.enqueue(MockResponse().setBody("""{"products": []}""").setResponseCode(200))

            val result = remoteDataSource.getProducts()

            assertTrue(result.isSuccess)
            assertTrue(result.getOrThrow().isEmpty())
        }

    @Test
    fun `GIVEN valid json file WHEN getProducts THEN returns mapped dtos`() =
        runTest {
            val jsonResource = ClassLoader.getSystemResource("products_success.json").readText()
            server.enqueue(MockResponse().setBody(jsonResource).setResponseCode(200))

            val result = remoteDataSource.getProducts()

            assertTrue(result.isSuccess)
            assertTrue(result.getOrThrow().size == 40)
        }

    @Test
    fun `GIVEN serialized products WHEN getProducts THEN data matches original object`() =
        runTest {
            val productResponse =
                ProductResponse(
                    id = "id1",
                    name = "pan",
                    priceCents = 100,
                    category = "bread",
                    stock = 5,
                )
            val jsonString = json.encodeToString(ProductsResponse(listOf(productResponse)))
            server.enqueue(MockResponse().setBody(jsonString).setResponseCode(200))

            val result = remoteDataSource.getProducts()

            assertTrue(result.isSuccess)
            assertTrue(result.getOrThrow().size == 1)
            assertTrue(result.getOrThrow().first().id == productResponse.id)
        }

    @Test
    fun `GIVEN 404 response WHEN getProducts THEN returns NotFoundError`() =
        runTest {
            server.enqueue(MockResponse().setResponseCode(404))

            val result = remoteDataSource.getProducts()

            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is AppError.NotFoundError)
        }

    @Test
    fun `GIVEN malformed json WHEN getProducts THEN returns Unknown error`() =
        runTest {
            server.enqueue(MockResponse().setBody("{error").setResponseCode(200))

            val result = remoteDataSource.getProducts()

            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is AppError.UnknownError)
        }

    @Test
    fun `GIVEN promotions request WHEN getPromotions THEN calls correct endpoint`() =
        runTest {
            server.enqueue(MockResponse().setBody("""{"promotions":[]}""").setResponseCode(200))
            remoteDataSource.getPromotions()

            val result = server.takeRequest()

            assertTrue(result.path == "/data/promotions.json")
            assertTrue(result.method == "GET")
        }
}
