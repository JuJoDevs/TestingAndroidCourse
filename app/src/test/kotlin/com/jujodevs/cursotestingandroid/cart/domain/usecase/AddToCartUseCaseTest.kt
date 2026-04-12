package com.jujodevs.cursotestingandroid.cart.domain.usecase

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

class AddToCartUseCaseTest {

    lateinit var cartRepository: FakeCartRepository
    lateinit var productRepository: FakeProductRepository
    lateinit var useCase: AddToCartUseCase


    @Before
    fun setUp() {
        cartRepository = FakeCartRepository()
        productRepository = FakeProductRepository()
        useCase = AddToCartUseCase(
            cartRepository = cartRepository,
            productRepository = productRepository
        )
    }

    @Test
    fun zero_quantity_throws_QuantityMustBePositive() = runTest {
        // When
        val exception = runCatching { useCase("id", 0) }.exceptionOrNull()

        // Then
        assertTrue(exception is AppError.Validation.QuantityMustBePositive)
    }

    @Test
    fun negative_quantity_throws_QuantityMustBePositive() = runTest {
        // When
        val exception = runCatching { useCase("id", -2) }.exceptionOrNull()

        // Then
        assertTrue(exception is AppError.Validation.QuantityMustBePositive)
    }

    @Test
    fun non_existing_product_throws_NotFoundError() = runTest {
        // Given
        productRepository.apply {
            setProducts(emptyList())
        }

        // When
        val exception = runCatching { useCase("id", 1) }.exceptionOrNull()

        // Then
        assertTrue(exception is AppError.NotFoundError)
    }

    @Test
    fun insufficient_stock_throws_InsufficientStock() = runTest {
        // Given
        val productId = "id-test-1"
        val product = product {
            withId(productId)
            withStock(2)
        }
        productRepository.setProducts(listOf(product))

        // When
        val exception = runCatching { useCase(productId, 5) }.exceptionOrNull()

        // Then
        assertTrue(exception is AppError.Validation.InsufficientStock)
        assertEquals(2, (exception as? AppError.Validation.InsufficientStock)?.available)
    }

    @Test
    fun successful_case_adds_item_ti_cart() = runTest {
        // Given
        val productId = "id-test-1"
        val product = product {
            withId(productId)
            withStock(10)
        }
        productRepository.setProducts(listOf(product))

        // When
        useCase(productId, 3)

        // Then
        val items = cartRepository.getCartItems().firstOrNull()
        assertEquals(productId, items?.firstOrNull()?.productId)
        assertEquals(3, items?.firstOrNull()?.quantity)
        assertEquals(1, items?.size)
    }

    @Test
    fun default_quantity_adds_one_item() = runTest {
        // Given
        val productId = "id-test-1"
        val product = product {
            withId(productId)
            withStock(10)
        }
        productRepository.setProducts(listOf(product))

        // When
        useCase(productId)

        // Then
        val items = cartRepository.getCartItems().firstOrNull()
        assertEquals(1, items?.firstOrNull()?.quantity)
        assertEquals(1, items?.size)
    }
}