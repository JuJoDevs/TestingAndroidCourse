package com.jujodevs.cursotestingandroid.checkout.domain.usecase

import com.jujodevs.cursotestingandroid.checkout.domain.model.OrderConfirmation
import com.jujodevs.cursotestingandroid.core.builders.cartItem
import com.jujodevs.cursotestingandroid.core.fakes.FakeCartRepository
import com.jujodevs.cursotestingandroid.core.fakes.FakeOrderRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * EXAMEN — Tests UNITARIOS del caso de uso de realizar pedido.
 *
 * Completa cada test siguiendo Given-When-Then. No modifiques producción.
 * SUT: [PlaceOrderUseCase] (éxito vacía el carrito; fallo NO lo vacía).
 * Pista: necesitarás un fake de OrderRepository y FakeCartItemRepository.
 */
class PlaceOrderUseCaseTest {
    lateinit var orderRepository: FakeOrderRepository
    lateinit var cartRepository: FakeCartRepository
    lateinit var useCase: PlaceOrderUseCase

    @Before
    fun setUp() {
        orderRepository = FakeOrderRepository()
        cartRepository = FakeCartRepository()
        useCase = createUseCase()
    }

    @Test
    fun `given successful order when invoke then returns success and clears cart`() =
        runTest {
            val confirmation = OrderConfirmation("order-1", 30, 25.0)
            orderRepository = FakeOrderRepository(Result.success(confirmation))
            cartRepository.setCartItems(listOf(cartItem()))
            useCase = createUseCase()

            val result = useCase()

            assertTrue(result.isSuccess)
            assertEquals(confirmation, result.getOrNull())
            assertTrue(cartRepository.getCartItems().first().isEmpty())
        }

    @Test
    fun `given repository throws when invoke then returns failure`() =
        runTest {
            val error = IllegalStateException("Order failed")
            orderRepository = FakeOrderRepository(Result.failure(error))
            useCase = createUseCase()

            val result = useCase()

            assertTrue(result.isFailure)
            assertEquals(error, result.exceptionOrNull())
        }

    @Test
    fun `given repository throws when invoke then does not clear cart`() =
        runTest {
            orderRepository =
                FakeOrderRepository(
                    Result.failure(IllegalStateException("Order failed")),
                )
            cartRepository.setCartItems(listOf(cartItem()))
            useCase = createUseCase()

            useCase()

            assertFalse(cartRepository.getCartItems().first().isEmpty())
        }

    private fun createUseCase(
        orderRepository: FakeOrderRepository = this.orderRepository,
        cartRepository: FakeCartRepository = this.cartRepository,
    ): PlaceOrderUseCase =
        PlaceOrderUseCase(
            orderRepository = orderRepository,
            cartRepository = cartRepository,
        )
}
