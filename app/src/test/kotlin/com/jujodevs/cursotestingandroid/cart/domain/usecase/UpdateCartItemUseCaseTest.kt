package com.jujodevs.cursotestingandroid.cart.domain.usecase

import com.jujodevs.cursotestingandroid.core.builders.cartItem
import com.jujodevs.cursotestingandroid.core.builders.product
import com.jujodevs.cursotestingandroid.core.domain.model.AppError
import com.jujodevs.cursotestingandroid.core.fakes.FakeCartRepository
import com.jujodevs.cursotestingandroid.core.fakes.FakeProductRepository
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UpdateCartItemUseCaseTest {
    lateinit var fakeCartRepository: FakeCartRepository
    lateinit var fakeProductRepository: FakeProductRepository
    lateinit var useCase: UpdateCartItemUseCase

    @Before
    fun setUp() {
        fakeCartRepository = FakeCartRepository()
        fakeProductRepository = FakeProductRepository()
        useCase = UpdateCartItemUseCase(fakeCartRepository, fakeProductRepository)
    }

    @Test
    fun given_negative_quantity_when_invokes_then_throws_quantity_must_be_positive() =
        runTest {
            // When
            val exception = runCatching { useCase("id", -1) }.exceptionOrNull()

            // Then
            assertTrue(exception is AppError.Validation.QuantityMustBePositive)
        }

    @Test
    fun given_zero_quantity_when_invoke_the_removes_items_from_cart() =
        runTest {
            // Given
            val productId = "id1"
            val product =
                product {
                    withId(productId)
                }
            val cartItem =
                cartItem {
                    withProductId(productId)
                    withQuantity(3)
                }
            fakeProductRepository.setProducts(listOf(product))
            fakeCartRepository.setCartItems(listOf(cartItem))

            // When
            useCase(productId, 0)

            // Then
            val items = fakeCartRepository.getCartItems().firstOrNull()
            assertEquals(0, items?.size)
        }

    @Test
    fun given_missing_product_when_invoke_then_throws_not_found() =
        runTest {
            fakeProductRepository.setProducts(emptyList())

            val exception = runCatching { useCase("not", 1) }.exceptionOrNull()

            assertTrue(exception is AppError.NotFoundError)
        }

    @Test
    fun given_requested_quantity_greater_than_stock_when_invoke_then_throws_insufficient_stock() =
        runTest {
            val productId = "product-id"
            val product =
                product {
                    withId(productId)
                    withStock(3)
                }
            val cartItem =
                cartItem {
                    withProductId(productId)
                    withQuantity(1)
                }
            fakeProductRepository.setProducts(listOf(product))
            fakeCartRepository.setCartItems(listOf(cartItem))

            val ex = runCatching { useCase(productId, 5) }.exceptionOrNull()

            assertTrue(ex is AppError.Validation.InsufficientStock)
        }

    @Test
    fun given_valid_product_and_quantity_when_invoke_then_updates_cart_item() =
        runTest {
            val productId = "product-id"
            val product =
                product {
                    withId(productId)
                    withStock(20)
                }
            val cartItem =
                cartItem {
                    withProductId(productId)
                    withQuantity(1)
                }
            fakeProductRepository.setProducts(listOf(product))
            fakeCartRepository.setCartItems(listOf(cartItem))

            useCase(productId, 5)

            val items = fakeCartRepository.getCartItems().firstOrNull()
            assertEquals(1, items?.size)
            assertEquals(5, items?.firstOrNull()?.quantity)
        }
}
