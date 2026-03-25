package com.jujodevs.cursotestingandroid.productlist.presentation

sealed interface ProductListEvent {
    data class ShowMessage(val message: String): ProductListEvent
    data object NavigateToSettings: ProductListEvent
    data class NavigateToProductDetail(val productId: String): ProductListEvent
    data object NavigateToCart: ProductListEvent
}