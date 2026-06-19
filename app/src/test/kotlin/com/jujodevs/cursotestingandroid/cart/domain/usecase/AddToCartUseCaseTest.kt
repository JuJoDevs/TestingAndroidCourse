package com.jujodevs.cursotestingandroid.cart.domain.usecase

import com.jujodevs.cursotestingandroid.cart.domain.repository.CartRepository
import com.jujodevs.cursotestingandroid.core.builders.product
import com.jujodevs.cursotestingandroid.core.domain.model.AppError
import com.jujodevs.cursotestingandroid.core.fakes.FakeCartRepository
import com.jujodevs.cursotestingandroid.core.fakes.FakeProductRepository
import com.jujodevs.cursotestingandroid.productlist.domain.repository.ProductRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOf
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
        useCase =
            AddToCartUseCase(
                cartRepository = cartRepository,
                productRepository = productRepository,
            )
    }

    @Test
    fun zero_quantity_throws_QuantityMustBePositive() =
        runTest {
            // When
            val exception = runCatching { useCase("id", 0) }.exceptionOrNull()

            // Then
            assertTrue(exception is AppError.Validation.QuantityMustBePositive)
        }

    @Test
    fun negative_quantity_throws_QuantityMustBePositive() =
        runTest {
            // When
            val exception = runCatching { useCase("id", -2) }.exceptionOrNull()

            // Then
            assertTrue(exception is AppError.Validation.QuantityMustBePositive)
        }

    @Test
    fun non_existing_product_throws_NotFoundError() =
        runTest {
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
    fun insufficient_stock_throws_InsufficientStock() =
        runTest {
            // Given
            val productId = "id-test-1"
            val product =
                product {
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
    fun successful_case_adds_item_ti_cart() =
        runTest {
            // Given
            val productId = "id-test-1"
            val product =
                product {
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
    fun default_quantity_adds_one_item() =
        runTest {
            // Given
            val productId = "id-test-1"
            val product =
                product {
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

    @Test
    fun zero_quantity_does_not_call_any_repository() =
        runTest {
            // Given
            val productRepository = mockk<ProductRepository>()
            val cartRepository = mockk<CartRepository>()
            val useCase =
                AddToCartUseCase(
                    cartRepository = cartRepository,
                    productRepository = productRepository,
                )

            // When
            runCatching { useCase("id", 0) }.exceptionOrNull()

            // Then
            coVerify(exactly = 0) { productRepository.getProductById(any()) }
            coVerify(exactly = 0) { cartRepository.getCartItemById(any()) }
            coVerify(exactly = 0) { cartRepository.addToCart(any(), any()) }
        }

    @Test
    fun valid_product_calls_addToCart_with_expect_values() =
        runTest {
            // Given
            val productRepository = mockk<ProductRepository>()
            val cartRepository = mockk<CartRepository>()
            val useCase =
                AddToCartUseCase(
                    cartRepository = cartRepository,
                    productRepository = productRepository,
                )
            val productId = "custom-id"
            val product =
                product {
                    withId(productId)
                    withStock(10)
                }
            coEvery { productRepository.getProductById(productId) } returns flowOf(product)
            coEvery { cartRepository.getCartItemById(productId) } returns null
            coEvery { cartRepository.addToCart(productId, 3) } just runs

            // When
            useCase(productId, 3)

            // Then
            coVerify(exactly = 1) { productRepository.getProductById(productId) }
            coVerify(exactly = 1) { cartRepository.getCartItemById(productId) }
            coVerify(exactly = 1) { cartRepository.addToCart(productId, 3) }
        }
}
