package com.jujodevs.cursotestingandroid.cart.presentation

sealed interface CartAction {
    data class UpdateCartItem(val productId: String, val quantity: Int) : CartAction
    data class RemoveFromCart(val productId: String) : CartAction
    data class IncreaseQuantity(val productId: String, val currentQuantity: Int) : CartAction
    data class DecreaseQuantity(val productId: String, val currentQuantity: Int) : CartAction
}