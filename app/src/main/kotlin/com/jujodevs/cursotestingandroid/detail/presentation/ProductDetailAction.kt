package com.jujodevs.cursotestingandroid.detail.presentation

sealed interface ProductDetailAction {
    data class LoadProduct(val productId: String) : ProductDetailAction
    data object AddToCart : ProductDetailAction
}