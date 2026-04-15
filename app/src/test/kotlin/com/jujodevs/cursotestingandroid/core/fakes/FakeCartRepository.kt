package com.jujodevs.cursotestingandroid.core.fakes

import com.jujodevs.cursotestingandroid.cart.domain.model.CartItem
import com.jujodevs.cursotestingandroid.cart.domain.repository.CartRepository
import com.jujodevs.cursotestingandroid.core.domain.model.AppError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class FakeCartRepository : CartRepository {

    private val _cartItems = MutableStateFlow(emptyList<CartItem>())

    fun setCartItems(items: List<CartItem>) {
        _cartItems.value = items
    }

    override fun getCartItems(): Flow<List<CartItem>> = _cartItems.asStateFlow()

    override suspend fun addToCart(
        productId: String,
        quantity: Int,
    ) {
        val currentItems = _cartItems.value.toMutableList()
        val existingIndex = currentItems.indexOfFirst { it.productId == productId }

        if (existingIndex >= 0) {
            val currentItem = currentItems[existingIndex]
            currentItems[existingIndex] = currentItem.copy(quantity = currentItem.quantity + quantity)
        } else {
            currentItems.add(CartItem(productId, quantity))
        }
        _cartItems.update { currentItems }
    }

    override suspend fun removeFromCart(productId: String) {
        val currentItems = _cartItems.value.toMutableList()
        val existingIndex = currentItems.indexOfFirst { it.productId == productId }

        if (existingIndex >= 0) {
            currentItems.removeAt(existingIndex)
            _cartItems.update { currentItems }
        } else {
            throw AppError.NotFoundError
        }
    }

    override suspend fun updateQuantity(
        productId: String,
        quantity: Int,
    ) {
        val currentItems = _cartItems.value.toMutableList()
        val existingIndex = currentItems.indexOfFirst { it.productId == productId }

        if (existingIndex >= 0) {
            currentItems[existingIndex] = currentItems[existingIndex].copy(quantity = quantity)
        } else {
            throw AppError.NotFoundError
        }
        _cartItems.update { currentItems }
    }

    override suspend fun clearCart() {
        _cartItems.update { emptyList() }
    }

    override suspend fun getCartItemById(productId: String): CartItem? {
        return _cartItems.value.find { it.productId == productId }
    }
}