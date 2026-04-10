package com.jujodevs.cursotestingandroid.detail.presentation

sealed interface ProductDetailAction {
    data object AddToCart : ProductDetailAction
}