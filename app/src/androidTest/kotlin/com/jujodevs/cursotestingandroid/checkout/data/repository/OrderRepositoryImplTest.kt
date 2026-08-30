package com.jujodevs.cursotestingandroid.checkout.data.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.jujodevs.cursotestingandroid.checkout.domain.repository.OrderRepository
import com.jujodevs.cursotestingandroid.core.domain.model.AppError
import com.jujodevs.cursotestingandroid.core.mockwebserver.MockWebServerUrlHolder
import com.jujodevs.cursotestingandroid.core.mockwebserver.rules.MockWebServerRule
import com.jujodevs.cursotestingandroid.core.utils.asAsset
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.SocketPolicy
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

/**
 * EXAMEN — Tests de INTEGRACIÓN del repositorio de pedidos.
 *
 * Completa cada test siguiendo Given-When-Then. No modifiques producción.
 * SUT: [OrderRepositoryImpl] sobre RemoteDataSource real + MockWebServer
 * (200 -> OrderConfirmation mapeada; 404 -> AppError.NotFoundError; red caída -> AppError.NetworkError).
 * Pistas: encola respuestas en `mockWebServer.server`, inyecta con Hilt (`hilt.inject()`).
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class OrderRepositoryImplTest {
    @get:Rule(order = 0)
    val mockWebServer = MockWebServerRule()

    @get:Rule(order = 1)
    val hiltRule = HiltAndroidRule(this)

    @Inject lateinit var orderRepository: OrderRepository

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @After
    fun tearDown() {
        MockWebServerUrlHolder.baseUrl = ""
    }

    @Test
    fun givenSuccessfulResponse_whenPlaceOrder_thenReturnsOrderConfirmation() =
        runTest {
            mockWebServer.server.enqueue(
                MockResponse()
                    .setResponseCode(200)
                    .setBody("order_confirmation_default.json".asAsset()),
            )

            val result = orderRepository.placeOrder()

            assertTrue(result.isSuccess)
            assertEquals("order-1", result.getOrNull()?.orderId)
            assertEquals(30, result.getOrNull()?.etaMinutes)
            assertEquals(25.0, result.getOrNull()?.total)
        }

    @Test
    fun given404Response_whenPlaceOrder_thenThrowsNotFoundError() =
        runTest {
            mockWebServer.server.enqueue(MockResponse().setResponseCode(404))

            val result = orderRepository.placeOrder()

            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is AppError.NotFoundError)
        }

    @Test
    fun givenNetworkFailure_whenPlaceOrder_thenThrowsNetworkError() =
        runTest {
            mockWebServer.server.enqueue(
                MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START),
            )

            val result = orderRepository.placeOrder()

            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is AppError.NetworkError)
        }
}
