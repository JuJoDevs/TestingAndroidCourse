package com.jujodevs.cursotestingandroid.cart.data.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.jujodevs.cursotestingandroid.cart.domain.repository.CartRepository
import com.jujodevs.cursotestingandroid.core.domain.model.AppError
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class CartRepositoryImplTest {
    @get:Rule val hiltRule = HiltAndroidRule(this)

    @Inject lateinit var cartRepository: CartRepository

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun givenEmptyCart_whenGetCartItemsIsCalled_thenFlowEmitsEmptyList() =
        runTest {
            val items = cartRepository.getCartItems().first()

            assertTrue(items.isEmpty())
        }

    @Test
    fun givenCartWithItems_whenAddToCartIsCalled_thenItemIsAdded() =
        runTest {
            val productId = "p1"
            val quantity = 2

            cartRepository.addToCart(productId, quantity)

            val items = cartRepository.getCartItems().first()
            assertEquals(1, items.size)
            assertEquals(productId, items[0].productId)
            assertEquals(quantity, items[0].quantity)
        }

    @Test
    fun givenCartWithItem_whenAddToCartSameProductIsCalled_thenQuantityIsUpdated() =
        runTest {
            val productId = "p1"
            cartRepository.addToCart(productId, 2)
            cartRepository.addToCart(productId, 3)

            val items = cartRepository.getCartItems().first()
            assertEquals(1, items.size)
            assertEquals(5, items[0].quantity)
        }

    @Test
    fun givenCartWithItems_whenRemoveFromCartIsCalled_thenItemIsRemoved() =
        runTest {
            val productId = "p1"
            cartRepository.addToCart(productId, 1)

            cartRepository.removeFromCart(productId)

            val items = cartRepository.getCartItems().first()
            assertTrue(items.isEmpty())
        }

    @Test(expected = AppError.NotFoundError::class)
    fun givenCartWithoutItem_whenRemoveFromCartIsCalled_thenThrowNotFoundError() =
        runTest {
            cartRepository.removeFromCart("non-existent")
        }

    @Test
    fun givenCartWithItems_whenUpdateQuantityIsCalled_thenQuantityIsUpdated() =
        runTest {
            val productId = "p1"
            cartRepository.addToCart(productId, 1)

            cartRepository.updateQuantity(productId, 10)

            val item = cartRepository.getCartItemById(productId)
            assertEquals(10, item?.quantity)
        }

    @Test(expected = AppError.NotFoundError::class)
    fun givenCartWithoutItem_whenUpdateQuantityIsCalled_thenThrowNotFoundError() =
        runTest {
            cartRepository.updateQuantity("non-existent", 5)
        }

    @Test
    fun givenCartWithItems_whenClearCartIsCalled_thenCartIsEmpty() =
        runTest {
            cartRepository.addToCart("p1", 1)
            cartRepository.addToCart("p2", 2)

            cartRepository.clearCart()

            val items = cartRepository.getCartItems().first()
            assertTrue(items.isEmpty())
        }

    @Test
    fun givenCartWithItems_whenGetCartItemByIdIsCalled_thenReturnCorrectItem() =
        runTest {
            val productId = "p1"
            cartRepository.addToCart(productId, 5)

            val item = cartRepository.getCartItemById(productId)

            assertEquals(productId, item?.productId)
            assertEquals(5, item?.quantity)
        }

    @Test
    fun givenCartWithoutItem_whenGetCartItemByIdIsCalled_thenReturnNull() =
        runTest {
            val item = cartRepository.getCartItemById("non-existent")

            assertNull(item)
        }
}
