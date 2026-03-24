package com.jujodevs.cursotestingandroid.cart.presentation

import com.jujodevs.cursotestingandroid.cart.domain.model.CartSummary
import com.jujodevs.cursotestingandroid.cart.presentation.model.CartItemWithPromotion

sealed interface CartUiState {

    data class Success(
        val summary: CartSummary,
        val cartItems: List<CartItemWithPromotion>,
        val isLoading: Boolean,
    ) : CartUiState

    data object  Loading : CartUiState
}
