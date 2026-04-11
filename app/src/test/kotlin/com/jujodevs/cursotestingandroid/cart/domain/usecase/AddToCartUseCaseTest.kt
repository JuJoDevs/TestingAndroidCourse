package com.jujodevs.cursotestingandroid.cart.domain.usecase

import com.jujodevs.cursotestingandroid.cart.domain.repository.CartRepository
import com.jujodevs.cursotestingandroid.core.domain.model.AppError
import com.jujodevs.cursotestingandroid.core.fakes.FakeCartRepository
import com.jujodevs.cursotestingandroid.core.fakes.FakeProductRepository
import com.jujodevs.cursotestingandroid.productlist.domain.repository.ProductRepository
import kotlinx.coroutines.test.runTest
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
}